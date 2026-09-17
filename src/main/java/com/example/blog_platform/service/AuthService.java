package com.example.blog_platform.service;

import com.example.blog_platform.dto.AuthResponse;
import com.example.blog_platform.dto.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}