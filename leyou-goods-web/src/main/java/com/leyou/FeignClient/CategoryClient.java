package com.leyou.FeignClient;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-item-service")
public interface CategoryClient extends CategoryApi {
}