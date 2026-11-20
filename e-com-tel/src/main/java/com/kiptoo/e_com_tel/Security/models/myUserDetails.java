package com.kiptoo.e_com_tel.Security.models;

import com.kiptoo.e_com_tel.models.Users;
import com.kiptoo.e_com_tel.repo.UserRepo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class myUserDetails implements UserDetails {
    private Long id;
    private  String email;
    private  String password;
    private Collection<?  extends GrantedAuthority> authorities;

    public static myUserDetails buildUserDetails(Users users){
        List<GrantedAuthority> authorities = users.getRoles()
                .stream()
                .map(role ->new SimpleGrantedAuthority(role.getRole()))
                .collect(Collectors.toList());
        return new myUserDetails(
                users.getId(),
                users.getEmail(),
                users.getPassword(),
                authorities
        );
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
