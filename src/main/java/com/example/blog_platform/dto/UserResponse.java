package com.example.blog_platform.dto;

import com.example.blog_platform.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
