package com.kiptoo.e_com_tel.service;

import com.kiptoo.e_com_tel.Dtos.ProductsDto;
import com.kiptoo.e_com_tel.Response.ProductResponse;

import java.io.IOException;

public interface IProductService {
    ProductResponse getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(ProductsDto productsDto, Long id) throws IOException;
    ProductResponse createProduct(ProductsDto productsDto) ;
    void deleteProduct(Long id);

}
