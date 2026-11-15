package com.kiptoo.e_com_tel.service;

import com.kiptoo.e_com_tel.models.Products;
import com.kiptoo.e_com_tel.repo.ProductsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {

    @Autowired
    ProductsRepo productsRepo;

    public List<Products> getAllProducts() {
        return  productsRepo.findAll();
    }

    public Products getProductById(long id) {
        return productsRepo.findById(id).orElse(null);
    }
}
