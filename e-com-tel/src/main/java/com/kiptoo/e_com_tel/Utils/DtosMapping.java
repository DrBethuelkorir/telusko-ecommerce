package com.kiptoo.e_com_tel.Utils;

import com.kiptoo.e_com_tel.Dtos.ProductsDto;
import com.kiptoo.e_com_tel.Dtos.RolesDto;
import com.kiptoo.e_com_tel.Dtos.UsersDto;
import com.kiptoo.e_com_tel.models.Products;
import com.kiptoo.e_com_tel.models.Roles;
import com.kiptoo.e_com_tel.models.Users;

import java.util.List;

public class DtosMapping {

    public static ProductsDto mapEntityProductToDto(Products products) {
        ProductsDto productsDto = new ProductsDto();

        productsDto.setId(products.getId());
        productsDto.setName(products.getName());
        productsDto.setPrice(products.getPrice());
        productsDto.setCategory(products.getCategory());
        productsDto.setAvailable(products.isAvailable());
        productsDto.setQuantity(products.getQuantity());
        productsDto.setExpirationDate(products.getExpirationDate());
        productsDto.setDescription(products.getDescription());

        return productsDto;
    }

    public static RolesDto mapRolesEntityToRolesDto(Roles roles) {
        RolesDto rolesDto = new  RolesDto();
        rolesDto.setId(roles.getId());
        rolesDto.setRole(roles.getRole());

        return  rolesDto;
    }

    public static UsersDto mapUserEntityToUserDtoPlusRoles(Users users) {
        UsersDto usersDto = new UsersDto();

        usersDto.setId(users.getId());
        usersDto.setEmail(users.getEmail());
        if(users.getRoles() != null){
            List<RolesDto> rolesDtos = users.getRoles().stream()
                    .map(DtosMapping::mapRolesEntityToRolesDto)
                    .toList();
            usersDto.setRolesDtos(rolesDtos);
        }
return usersDto;
    }
    public static UsersDto mapUserEntityToUserDto(Users users) {
        UsersDto usersDto = new UsersDto();
        usersDto.setId(users.getId());
        usersDto.setEmail(users.getEmail());

        return usersDto;
    }
}
