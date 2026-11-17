package com.kiptoo.e_com_tel.controllers;

import com.kiptoo.e_com_tel.Dtos.ProductsDto;
import com.kiptoo.e_com_tel.Response.ProductResponse;
import com.kiptoo.e_com_tel.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class ProductsController {

    @Autowired
    ProductsService productsService;

    @GetMapping("/products")
    public ResponseEntity<ProductResponse> getAllProducts(){
        ProductResponse productResponse = productsService.getAllProducts();

        if(productResponse == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(productResponse, HttpStatus.OK) ;
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable long id) {
        ProductResponse products = productsService.getProductById(id);
        if (products == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<ProductResponse>(products, HttpStatus.OK);
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProducts(
            @RequestPart("productsDto") ProductsDto productsDto,
            @RequestPart("imageFile") MultipartFile imageFile) {
        try {
            ProductResponse response = productsService.createProduct(productsDto, imageFile);
            return new ResponseEntity<>(response, HttpStatus.CREATED);  // 201 Created
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/products/image/{id}")
    public ResponseEntity<byte[]> getImageProductById(@PathVariable long id) {
        ProductResponse products = productsService.getProductById(id);
        byte[] imageFile = products.getProductsDto().getImageData();

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(products.getProductsDto().getImageType()))
                .body(imageFile)  ;
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProducts(@PathVariable long id,
                                            @RequestPart ProductsDto productsDto,
                                            @RequestPart MultipartFile imageFile ) throws IOException {
            ProductResponse products1 = productsService.updateProduct(productsDto,id,imageFile);
            if (products1 != null) {
                return new ResponseEntity<>(products1, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>("Failed To update",HttpStatus.BAD_REQUEST);
            }



    }
    @DeleteMapping("products/{id}")
    public ResponseEntity<?> deleteProducts(@PathVariable Long id) {
        try {
            productsService.deleteProduct(id);
            return new ResponseEntity<>( HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
