package com.epam.workshop.sales.product.client.dto;

public class OrderProductNameDto {

    private String productName;

    public OrderProductNameDto(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
