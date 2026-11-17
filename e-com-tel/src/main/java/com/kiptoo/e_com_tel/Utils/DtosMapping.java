package com.kiptoo.e_com_tel.Utils;

import com.kiptoo.e_com_tel.Dtos.ProductsDto;
import com.kiptoo.e_com_tel.models.Products;

public class DtosMapping {

    public static ProductsDto mapEntityProductToDto(Products products) {
        ProductsDto productsDto = new ProductsDto();

        productsDto.setName(products.getName());
        productsDto.setPrice(products.getPrice());
        productsDto.setCategory(products.getCategory());
        productsDto.setAvailable(products.isAvailable());
        productsDto.setQuantity(products.getQuantity());
        productsDto.setExpirationDate(products.getExpirationDate());
        productsDto.setDescription(products.getDescription());

        return productsDto;
    }
}
