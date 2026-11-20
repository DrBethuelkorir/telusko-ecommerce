package com.kiptoo.e_com_tel.service.Products;

import com.kiptoo.e_com_tel.Dtos.ProductsDto;
import com.kiptoo.e_com_tel.Response.ProductResponse;
import com.kiptoo.e_com_tel.Utils.DtosMapping;
import com.kiptoo.e_com_tel.models.Products;
import com.kiptoo.e_com_tel.repo.ProductsRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductsService implements IProductService {

    final private ProductsRepo productsRepo;

    @Override
    public ProductResponse getAllProducts() {
        List<Products> productList =  productsRepo.findAll();
        List<ProductsDto> productsDtoList = productList.stream()
                .map(DtosMapping::mapEntityProductToDto)
                .toList();
        if(productsDtoList.isEmpty()){
            return new ProductResponse(201, "products not found", null,null);
        }

        return new ProductResponse(200,"Products found found",null,productsDtoList);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Products products = productsRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Class not found"));
        ProductsDto productsDto = DtosMapping.mapEntityProductToDto(products);

        return new ProductResponse(200,"Product found", productsDto, null);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(ProductsDto productsDto, Long id)  {
        if (productsDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product payload required");
        }

        Products products = productsRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: "));
        products.setName(productsDto.getName());
        products.setPrice(productsDto.getPrice());
        products.setCategory(productsDto.getCategory());
        products.setDescription(productsDto.getDescription());
        products.setExpirationDate(productsDto.getExpirationDate());
        products.setQuantity(productsDto.getQuantity());

        productsRepo.save(products);
        ProductsDto  productsDto1 = DtosMapping.mapEntityProductToDto(products);

        return new ProductResponse(200,"Product updated",productsDto1,null);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductsDto productsDto)  {
        if (productsDto == null) throw new IllegalArgumentException("Product payload required");



        Products products = new  Products();
        products.setName(productsDto.getName());
        products.setPrice(productsDto.getPrice());
        products.setCategory(productsDto.getCategory());
        products.setDescription(productsDto.getDescription());
        products.setExpirationDate(productsDto.getExpirationDate());
        products.setQuantity(productsDto.getQuantity());

        Products products1 = productsRepo.save(products);
        ProductsDto productsDto1 = DtosMapping.mapEntityProductToDto(products1);


        return new ProductResponse(200,"Products created successfully", productsDto1, null);
    }

    @Override
    public void deleteProduct(Long id) {
        Products products = productsRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Class not found"));
        productsRepo.delete(products);
        ProductsDto productsDto = DtosMapping.mapEntityProductToDto(products);
        new ProductResponse(200, "deleted successfully", productsDto, null);
    }
}