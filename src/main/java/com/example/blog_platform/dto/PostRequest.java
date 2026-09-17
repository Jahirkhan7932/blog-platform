package com.example.blog_platform.dto;

import com.example.blog_platform.entity.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be 3-200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String coverImageUrl;

    private PostStatus status;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    private Set<String> tags;   // tag names (e.g., ["java", "spring"])
}