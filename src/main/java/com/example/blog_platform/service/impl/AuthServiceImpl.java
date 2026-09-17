package com.example.blog_platform.service.impl;

import com.example.blog_platform.dto.AuthResponse;
import com.example.blog_platform.dto.LoginRequest;
import com.example.blog_platform.dto.UserResponse;
import com.example.blog_platform.entity.User;
import com.example.blog_platform.repository.UserRepository;
import com.example.blog_platform.security.JwtService;
import com.example.blog_platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            // Ask Spring Security to verify the credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid username/email or password");
        }

        // Load the user — could be by username or email
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        // Generate the JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userResponse)
                .build();
    }
}