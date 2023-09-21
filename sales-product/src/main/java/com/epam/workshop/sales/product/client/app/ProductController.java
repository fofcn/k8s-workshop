package com.epam.workshop.sales.product.client.app;

import com.epam.workshop.sales.product.infrastructure.feign.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @GetMapping("/{productId}/name")
    public String getOrderProductName(@PathVariable("productId")String productId) {
        return productId + "-product-name";
    }

    @GetMapping("/{productId}/stock")
    public String getProductStock(@PathVariable("productId")String productId) {
        return productId + ": stock: 2";
    }

}
