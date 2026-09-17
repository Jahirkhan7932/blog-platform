package com.example.blog_platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;      // milliseconds
    private UserResponse user;
}
