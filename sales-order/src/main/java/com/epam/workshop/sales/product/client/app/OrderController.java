package com.epam.workshop.sales.product.client.app;

import com.epam.workshop.sales.product.client.dto.OrderProductNameDto;
import com.epam.workshop.sales.product.infrastructure.feign.ProductFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

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

    @GetMapping("/version")
    public String getOrderProductName() {
        return "sale-order-v0";
    }

    @GetMapping("/timeout")
    public String getTimeout() throws InterruptedException {
        log.info("I'm going to sleep after I returned.");
        TimeUnit.SECONDS.sleep(11);
        log.info("As you can see, I have timeout if your timeout configuration is 10s.");
        return "timeout";
    }

}
