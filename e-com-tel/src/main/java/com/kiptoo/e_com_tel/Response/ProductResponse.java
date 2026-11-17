package com.kiptoo.e_com_tel.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kiptoo.e_com_tel.Dtos.ProductsDto;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private int statusCode;
    private String message;
    private ProductsDto productsDto;
    private List<ProductsDto> productsDtoList;
}
