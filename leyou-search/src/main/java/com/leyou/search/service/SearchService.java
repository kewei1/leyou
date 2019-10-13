package com.leyou.search.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.enuums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.*;
import com.leyou.repository.GoodsRepository;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;



    private ObjectMapper mapper = new ObjectMapper();

    private MatchQueryBuilder basicQuery =null;


    public Goods buildGoods(Spu spu) throws IOException {

        // 查询商品分类名称
        List<String> names = categoryClient.queryNameByIds(Arrays.asList((Long) spu.getCid1(), (Long) spu.getCid2(), (Long) spu.getCid3()));

        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //搜索字段
        String all = spu.getTitle() + StringUtils.join(names, "") + brand.getName();


        // 查询sku
        List<Sku> skuList = this.goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //对Sku进行处理
        List<Map<String, Object>> skus = new ArrayList<>();
        Set<Long> priceSet = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            skus.add(map);
            priceSet.add(sku.getPrice());
        }


        // 查询规格参数
        List<Specparm> params = this.specificationClient.querySpecificationByCategoryGId(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.SPEC_parm_NOT_FOUND);
        }

        // 查询详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spu.getId());

        // 获取通用规格参数
        Map<String, String> genericSpecs = JsonUtils.ToMap(spuDetail.getGenericSpec());

        // 获取特有规格参数
        Map<String, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<String>>>() {
        });

        // 获取可搜索的规格参数
        Map<String, Object> searchSpec = new HashMap<>();
        for (Specparm parm : params) {
            //规格名称
            String key = parm.getName();
            //规格的值
            Object value = "";

            //判断是否是通用规格
            if (parm.getGeneric()) {

                value = genericSpecs.get(parm.getId().toString());

                if (parm.getNumeric()) {

                    value = chooseSegment(value.toString(), parm);

                }
            } else {
                specialSpec.get(parm.getId().toString());
            }
            //存入Map
            searchSpec.put(key, value);
        }


        //构建goods对象
        Goods goods = new Goods();
        goods.setId(spu.getId());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);  // 搜索字段，包含标题，分类，品牌，规格等
        goods.setPrice(priceSet); // 所有Sku价格集合
        goods.setSkus(mapper.writeValueAsString(skus));  // 所有Sku集合的json格式

        goods.setSpecs(searchSpec); // 所有所搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String chooseSegment(String value, Specparm p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    @Autowired
    private GoodsRepository goodsRepository;

    public PageResult<Goods> search(SearchRequest request) {
        String key = request.getKey();

        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (StringUtils.isBlank(request.getKey())) {
            return null;
        }

        // 1、构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();


        // 1.1、基本查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", request.getKey()));
        // 通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id", "skus", "subTitle"}, null));

        // 1.2.分页排序
        searchWithPageAndSort(queryBuilder,request,key);


        // 1.3、聚合
        String categoryAggName = "category"; // 商品分类聚合名称
        String brandAggName = "brand"; // 品牌聚合名称
        // 对商品分类进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 对品牌进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 2、查询，获取结果
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

        // 3、解析查询结果
        // 3.1、分页信息
        Long total = pageInfo.getTotalElements();
        int totalPage = (total.intValue() + request.getSize() - 1) / request.getSize();
        // 3.2、商品分类的聚合结果
        List<Category> categories =
                getCategoryAggResult(pageInfo.getAggregation(categoryAggName));
        // 3.3、品牌的聚合结果
        List<Brand> brands = getBrandAggResult(pageInfo.getAggregation(brandAggName));



        // 根据商品分类判断是否需要聚合
        List<Map<String, Object>> specs = new ArrayList<>();
        if (categories.size() == 1) {
            // 如果商品分类只有一个才进行聚合，并根据分类与基本查询条件聚合
            specs = getSpec(categories.get(0).getId(), basicQuery);
        }



        Long totalpage = new Long((long) (pageInfo.getTotalElements() + 20 - 1) / 20);
        // 返回结果
        System.out.println(brands);
        System.out.println(categories);
        System.out.println(pageInfo.getContent());
        SearchResult searchResult = new SearchResult();
        searchResult.setItems(pageInfo.getContent());
        searchResult.setBrands(brands);
        searchResult.setCategories(categories);
        searchResult.setTotal(pageInfo.getTotalElements());
        searchResult.setTotalPage(totalpage);
        searchResult.setSpecs(specs);

        //TODO 不知为何构造带参函数返回时失败
        //return new SearchResult(pageInfo.getTotalElements(), totalpage, pageInfo.getContent(), categories, brands);
        return  searchResult ;
    }



    // 解析品牌聚合结果
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        try {
            LongTerms brandAgg = (LongTerms) aggregation;
            List<Long> bids = new ArrayList<>();
            for (LongTerms.Bucket bucket : brandAgg.getBuckets()) {
                bids.add(bucket.getKeyAsNumber().longValue());
            }
            // 根据id查询品牌
            return this.brandClient.queryBrandByIds(bids);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.ABNORMAL_BRAND_AGGREGATION);

        }
    }

    // 解析商品分类聚合结果
    private List<Category> getCategoryAggResult(Aggregation aggregation) {
        try {
            List<Category> categories = new ArrayList<>();
            LongTerms categoryAgg = (LongTerms) aggregation;
            List<Long> cids = new ArrayList<>();
            for (LongTerms.Bucket bucket : categoryAgg.getBuckets()) {
                cids.add(bucket.getKeyAsNumber().longValue());
            }
            // 根据id查询分类名称
            List<String> names = this.categoryClient.queryNameByIds(cids);

            for (int i = 0; i < names.size(); i++) {
                Category c = new Category();
                c.setId(cids.get(i));
                c.setName(names.get(i));
                categories.add(c);
            }
            return categories;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.ABNORMAL_CATEGORY_AGGREGATION);
        }
    }

    // 构建基本查询条件
    private void searchWithPageAndSort(NativeSearchQueryBuilder queryBuilder, SearchRequest request ,String key) {
        // 准备分页参数
        int page = request.getPage();
        int size = request.getSize();

        // 1、分页
        queryBuilder.withPageable(PageRequest.of(page - 1, size));

        MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);

        // 2、排序
        String sortBy = request.getSortBy();
        Boolean desc = request.getDescending();
        if (StringUtils.isNotBlank(sortBy)) {
            // 如果不为空，则进行排序
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }
    }




    /**
     * 聚合出规格参数
     *
     * @param cid
     * @param query
     * @return
     */
    private List<Map<String, Object>> getSpec(Long cid, QueryBuilder query) {
        try {
            // 不管是全局参数还是sku参数，只要是搜索参数，都根据分类id查询出来
            List<Specparm> params =this.specificationClient.querySpecificationByCategoryGId(null, cid, true);
            List<Map<String, Object>> specs = new ArrayList<>();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(query);

            // 聚合规格参数
            params.forEach(p -> {
                String key = p.getName();
                queryBuilder.addAggregation(AggregationBuilders.terms(key).field("specs." + key + ".keyword"));

            });

            // 查询
            Map<String, Aggregation> aggs = this.elasticsearchTemplate.query(queryBuilder.build(),
                    SearchResponse::getAggregations).asMap();

            // 解析聚合结果
            params.forEach(param -> {
                Map<String, Object> spec = new HashMap<>();
                String key = param.getName();
                spec.put("k", key);
                StringTerms terms = (StringTerms) aggs.get(key);
                spec.put("options", terms.getBuckets().stream().map(StringTerms.Bucket::getKeyAsString));
                specs.add(spec);
            });

            return specs;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.GAUGE_AGGREGATE_ANOMALY);

        }

    }



}

