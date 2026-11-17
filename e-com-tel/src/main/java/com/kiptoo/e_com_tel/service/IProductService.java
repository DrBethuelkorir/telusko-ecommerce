package com.kiptoo.e_com_tel.service;

import com.kiptoo.e_com_tel.Dtos.ProductsDto;
import com.kiptoo.e_com_tel.Response.ProductResponse;
import com.kiptoo.e_com_tel.models.Products;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProductService {
    ProductResponse getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(ProductsDto productsDto, Long id, MultipartFile file) throws IOException;
    ProductResponse createProduct(ProductsDto productsDto, MultipartFile file) throws IOException;
    ProductResponse deleteProduct(Long id);

}
