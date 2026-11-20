package com.kiptoo.e_com_tel.Security.models;

import com.kiptoo.e_com_tel.models.Users;
import com.kiptoo.e_com_tel.repo.ProductsRepo;
import com.kiptoo.e_com_tel.repo.UserRepo;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {


    private final UserRepo userRepo;


    @Override
    public UserDetails loadUserByUsername(@Nullable String email) throws UsernameNotFoundException {
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        return myUserDetails.buildUserDetails(user);
    }
}

