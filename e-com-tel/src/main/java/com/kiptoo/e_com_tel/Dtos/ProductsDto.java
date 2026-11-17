package com.kiptoo.e_com_tel.Dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductsDto {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Date expirationDate;
    private boolean available;
    private Long quantity;
    private String imageName;
    private String imageType;
    private byte[] imageData;
}
