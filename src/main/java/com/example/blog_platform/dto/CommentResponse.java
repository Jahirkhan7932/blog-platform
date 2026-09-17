package com.example.blog_platform.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Long id;
    private String content;
    private Long postId;
    private Long authorId;
    private String authorUsername;
    private Long parentId;                     // null if top-level
    private List<CommentResponse> replies;     // empty if no replies
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}