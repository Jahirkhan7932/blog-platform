package com.example.blog_platform.service;

import com.example.blog_platform.dto.LikeResponse;

import java.util.List;

public interface LikeService {

    boolean toggleLike(Long postId, String currentUsername);

    long countLikes(Long postId);

    List<LikeResponse> getUsersWhoLiked(Long postId);

    List<LikeResponse> getPostsLikedByUser(String currentUsername);
}
