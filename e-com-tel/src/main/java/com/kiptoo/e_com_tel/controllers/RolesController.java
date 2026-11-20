package com.kiptoo.e_com_tel.controllers;

import com.kiptoo.e_com_tel.models.Roles;
import com.kiptoo.e_com_tel.service.Roles.RolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles") // Optional: Adds a base path for all endpoints here
public class RolesController {

    @Autowired
    RolesService rolesService;

    @GetMapping("/roles")
    public ResponseEntity<List<Roles>> getAllRoles() {
        List<Roles> roles = rolesService.getAllRoles();

        if (roles == null || roles.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<Roles> getRoleById(@PathVariable Long id) {
        try {
            Roles role = rolesService.getRoleById(id);
            return new ResponseEntity<>(role, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/roles")
    public ResponseEntity<?> addRole(@RequestBody Roles role) {
        try {
            // Best practice: Ensure roles start with "ROLE_" if you are using Spring Security
            if (role.getRole() != null && !role.getRole().startsWith("ROLE_")) {
                role.setRole("ROLE_" + role.getRole());
            }
            Roles savedRole = rolesService.saveRole(role);
            return new ResponseEntity<>(savedRole, HttpStatus.CREATED); // 201 Created
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Roles role) {
        try {
            // Fetch existing role to make sure it exists before updating
            Roles existingRole = rolesService.getRoleById(id);

            // Update the fields
            existingRole.setRole(role.getRole());

            Roles updatedRole = rolesService.saveRole(existingRole);
            return new ResponseEntity<>(updatedRole, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Failed to update: Role not found", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            rolesService.deleteRole(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}