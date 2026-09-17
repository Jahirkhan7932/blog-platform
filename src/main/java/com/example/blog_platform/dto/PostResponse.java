package com.example.blog_platform.dto;

import com.example.blog_platform.entity.PostStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String coverImageUrl;
    private PostStatus status;
    private Integer readingTime;
    private Long viewCount;
    private String authorUsername;
    private Long authorId;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
