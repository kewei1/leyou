package com.leyou.FeignClient;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-item-service")
public interface SpecificationClient extends SpecificationApi {
}