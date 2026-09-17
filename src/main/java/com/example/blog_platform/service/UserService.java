package com.example.blog_platform.service;

import com.example.blog_platform.dto.UserRequest;
import com.example.blog_platform.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserRequest request);

    void deleteUser(Long id);
}
