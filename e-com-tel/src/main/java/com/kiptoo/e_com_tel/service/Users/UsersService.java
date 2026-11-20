package com.kiptoo.e_com_tel.service.Users;

import com.kiptoo.e_com_tel.models.Users;
import com.kiptoo.e_com_tel.repo.RolesRepo;
import com.kiptoo.e_com_tel.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.kiptoo.e_com_tel.models.Roles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;




@Service
@RequiredArgsConstructor
public class UsersService implements IUserService{

    private final UserRepo userRepo;
    private final RolesRepo rolesRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Users saveUser(Users user) {
        // 1. Check if email already exists
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists!");
        }

        // 2. Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. Handle Roles (ensure they exist in the DB, or assign default)
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<Roles> managedRoles = new HashSet<>();
            for (Roles role : user.getRoles()) {
                // Find the role in the DB by name (e.g., "ROLE_USER")
                Roles existingRole = rolesRepo.findByRole(role.getRole())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + role.getRole()));
                managedRoles.add(existingRole);
            }
            user.setRoles(managedRoles);
        } else {
            // Assign default role if none provided
            Roles defaultRole = rolesRepo.findByRole("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default ROLE_USER not found in database!"));
            user.setRoles(Set.of(defaultRole));
        }

        return userRepo.save(user);
    }

    @Override
    public List<Users> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public Users getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public Users getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }
}
