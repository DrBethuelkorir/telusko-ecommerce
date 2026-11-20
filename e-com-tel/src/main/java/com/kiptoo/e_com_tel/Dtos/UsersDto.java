package com.kiptoo.e_com_tel.Dtos;


import com.kiptoo.e_com_tel.models.Roles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersDto {
    private Long id;
    private String email;
    private List<RolesDto> rolesDtos;
}
