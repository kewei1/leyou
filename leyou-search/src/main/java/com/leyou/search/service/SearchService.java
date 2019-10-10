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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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


    private ObjectMapper mapper = new ObjectMapper();


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
                searchSpec.get(parm.getId().toString());
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
        if (StringUtils.isBlank(key)) {
            return null;
        }

        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 1、对key进行全文检索查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND));

        // 2、通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id", "skus", "subTitle"}, null));

        // 3、分页
        // 准备分页参数
        int page = request.getPage() ;
        int size = request.getSize();
        System.out.println(size);
        queryBuilder.withPageable(PageRequest.of( page , size ));

        // 4、查询，获取结果
        Page<Goods> result = this.goodsRepository.search(queryBuilder.build());

        // 5. 解析结果
        Long total = result.getTotalElements();

        List<Goods> goodsList = result.getContent();

        // 封装结果并返回
        return new PageResult<>( total , (total + 20 -1) / 20, goodsList);
    }




}

