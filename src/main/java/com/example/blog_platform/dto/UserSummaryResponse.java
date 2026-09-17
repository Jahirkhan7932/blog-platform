package com.example.blog_platform.dto;

import com.example.blog_platform.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long id;
    private String username;
    private String bio;
    private String avatarUrl;
    private Role role;
}
