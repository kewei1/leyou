package com.leyou.cart.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-item-service")
public interface GoodsClient extends GoodsApi {
}