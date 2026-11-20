package com.kiptoo.e_com_tel.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Users {

    @Id // You were missing this!
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Products> products = new ArrayList<>();

    // This is the Many-to-Many relationship
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles", // Name of the connecting table in the database
            joinColumns = @JoinColumn(name = "user_id"), // Column for this entity
            inverseJoinColumns = @JoinColumn(name = "role_id") // Column for the other entity
    )
    private Set<Roles> roles = new HashSet<>();
}