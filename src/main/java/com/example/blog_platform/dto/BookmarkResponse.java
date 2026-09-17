package com.example.blog_platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkResponse {

    private Long id;
    private Long postId;
    private String postTitle;
    private String postSlug;
    private LocalDateTime createdAt;
}
