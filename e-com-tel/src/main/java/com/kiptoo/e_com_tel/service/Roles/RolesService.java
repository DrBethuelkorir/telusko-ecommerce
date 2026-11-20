package com.kiptoo.e_com_tel.service.Roles;

import com.kiptoo.e_com_tel.models.Roles;
import com.kiptoo.e_com_tel.repo.RolesRepo; // Assuming you have a RolesRepo
// import com.kiptoo.e_com_tel.dto.RolesDto;
// import com.kiptoo.e_com_tel.mapper.DtosMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Lombok automatically generates the constructor for final fields
public class RolesService {

    private final RolesRepo rolesRepo;

    // 1. Create or Update a Role
    public Roles saveRole(Roles role) {
        return rolesRepo.save(role);
    }

    // 2. Get all roles
    public List<Roles> getAllRoles() {
        return rolesRepo.findAll();
    }

    // 3. Get a role by ID
    public Roles getRoleById(Long id) {
        return rolesRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    // 4. Get a role by name (e.g., "ROLE_USER", "ROLE_ADMIN")
    public Roles getRoleByName(String roleName) {
        return rolesRepo.findByRole(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + roleName));
    }

    // 5. Delete a role
    public void deleteRole(Long id) {
        rolesRepo.deleteById(id);
    }
}