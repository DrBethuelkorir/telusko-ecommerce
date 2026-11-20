package com.kiptoo.e_com_tel.service.Users;

import com.kiptoo.e_com_tel.models.Users;

import java.util.List;

public interface IUserService {
    Users saveUser(Users user);
    List<Users> getAllUsers();
    Users getUserById(Long id);
    Users getUserByEmail(String email);
    void deleteUser(Long id);
}
