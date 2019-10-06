package com.leyou.search.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.enuums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<Sku> skus = this.goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }


        Set<Long> priceSet = skus.stream().map(Sku::getPrice).collect(Collectors.toSet());

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
        goods.setSpecs(null); //TODO  所有所搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

}

