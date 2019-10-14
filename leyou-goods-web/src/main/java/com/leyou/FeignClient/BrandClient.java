package com.leyou.FeignClient;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-item-service")
public interface BrandClient extends BrandApi {
}

