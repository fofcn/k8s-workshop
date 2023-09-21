package com.epam.workshop.sales.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SalesProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesProductApplication.class, args);
	}

}
