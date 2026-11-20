package com.kiptoo.e_com_tel.repo;

import com.kiptoo.e_com_tel.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepo extends JpaRepository<Roles,Long>{
    Optional<Roles> findByRole(String role);
}
