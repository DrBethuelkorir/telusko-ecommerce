package com.kiptoo.e_com_tel.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;

    // mappedBy tells JPA that the "Users" entity owns this relationship.
    // We use a Set to prevent duplicate roles.
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<Users> users = new HashSet<>();
}