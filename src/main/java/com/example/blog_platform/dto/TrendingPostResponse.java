package com.example.blog_platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingPostResponse {

    private PostResponse post;
    private long likeCount;
    private long commentCount;
    private long viewCount;
    private double score;
}
