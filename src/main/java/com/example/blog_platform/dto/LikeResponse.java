package com.example.blog_platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long postId;
    private LocalDateTime createdAt;
}
