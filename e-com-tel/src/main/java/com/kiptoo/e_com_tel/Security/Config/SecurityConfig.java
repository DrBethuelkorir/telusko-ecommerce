package com.kiptoo.e_com_tel.Security.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Autowired
    UserDetailsService userDetailsService;



// ... inside your SecurityConfig class ...

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(customizer -> customizer.disable())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        // 1. ALLOW unauthenticated POST requests to create a user
                        .requestMatchers(HttpMethod.POST, "/users/users").permitAll()

                        // (Optional) If you want to allow the CSRF token endpoint too:
                        // .requestMatchers(HttpMethod.GET, "/token").permitAll()

                        // 2. REQUIRE authentication for EVERY OTHER request
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
//@Bean
//    public UserDetailsService userDetailsService(){
//    UserDetails user1 = User
//            .withDefaultPasswordEncoder()
//            .username("sheriff")
//            .password("Kiptoo@0719")
//            .roles("USER")
//            .build();
//        return new InMemoryUserDetailsManager(user1);
//}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

@Bean
public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
    return  provider;
}

}
