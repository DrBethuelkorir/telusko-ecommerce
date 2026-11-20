package com.kiptoo.e_com_tel.service.Roles;

import com.kiptoo.e_com_tel.models.Roles;
import java.util.List;

public interface IRolesService {

    Roles saveRole(Roles role);

    List<Roles> getAllRoles();

    Roles getRoleById(Long id);

    Roles getRoleByName(String roleName);

    void deleteRole(Long id);
}