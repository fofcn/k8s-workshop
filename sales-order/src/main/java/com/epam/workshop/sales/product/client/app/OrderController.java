package com.epam.workshop.sales.product.client.app;

import com.epam.workshop.sales.product.client.dto.OrderProductNameDto;
import com.epam.workshop.sales.product.infrastructure.feign.ProductFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @GetMapping("/{orderNo}/product/name")
    public OrderProductNameDto getOrderProductName(@PathVariable("orderNo")String orderNo) {
        log.info("This is my MDC test");
        String productName = productFeignClient.getProductName(1L);
        return new OrderProductNameDto(productName);
    }

}
