package com.example.blog_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment must be under 2000 characters")
    private String content;
}
