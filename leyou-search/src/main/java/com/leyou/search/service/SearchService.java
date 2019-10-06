package com.leyou.search.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.enuums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
        List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand==null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //搜索字段
        String all = spu.getTitle()+ StringUtils.join(names,"")+brand.getName();


        // 查询sku
        List<Sku> skuList = this.goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //对Sku进行处理
        List<Map<String, Object>> skus = new ArrayList<>();
        Set<Long> priceSet =new HashSet<>();
        for (Sku sku : skuList ) {
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
        if (CollectionUtils.isEmpty(params)){
            throw new LyException(ExceptionEnum.SPEC_parm_NOT_FOUND);
        }

        // 查询详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spu.getId());

        // 获取通用规格参数
        Map<String, String> genericSpecs = JsonUtils.ToMap(spuDetail.getGenericSpec());

        // 获取特有规格参数
       Map<String,List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<String>>>() {});

        // 获取可搜索的规格参数
        Map<String, Object> searchSpec = new HashMap<>();
        for (Specparm parm:params) {
            //规格名称
            String key =parm.getName();
            Object value = "";

            //判断是否是通用规格
            if (parm.getGeneric()){
                value =genericSpecs.get(parm.getId()) ;
            }else {
                searchSpec.get(parm.getId());
            }
            //存入Map
            searchSpec.put(key,value);
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

}

