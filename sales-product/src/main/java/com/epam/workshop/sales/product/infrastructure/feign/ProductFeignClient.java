package com.epam.workshop.sales.product.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "product", url = "${product.url}")
public interface ProductFeignClient {

    @GetMapping("/{productId}/name")
    String getProductName(@PathVariable("productId") Long productId);

}
