package com.example.blog_platform.service;

import com.example.blog_platform.dto.PostResponse;
import com.example.blog_platform.dto.UserSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FollowService {

    void followUser(Long userId, String currentUsername);

    void unfollowUser(Long userId, String currentUsername);

    List<UserSummaryResponse> getFollowers(Long userId);

    List<UserSummaryResponse> getFollowing(Long userId);

    long countFollowers(Long userId);

    long countFollowing(Long userId);

    Page<PostResponse> getPersonalFeed(String currentUsername, int page, int size);
}
