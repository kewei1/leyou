package com.leyou.common.enuums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum  ExceptionEnum {

    PRICE_CANNOT_BE_NULL(400,"价格不能为空"),
    CATEGORY_NOT_FOUND(404,"没查到"),
    INTERNAL_SERVER_ERROR(500,"服务器异常"),
    BRAND_NOT_FOUND(404,"没有查到品牌"),
    GOODS_SKU_NOT_FOUND (404,"没有查到SKU"),
    SPEC_parm_NOT_FOUND(404,"没有查到SPEC"),
    ABNORMAL_BRAND_AGGREGATION(404,"品牌聚合出现异常"),
    ABNORMAL_CATEGORY_AGGREGATION(404,"分类聚合出现异常"),
    GAUGE_AGGREGATE_ANOMALY(500,"规格聚合出现异常"),
    ;


    private int code;
    private String msg;



}
