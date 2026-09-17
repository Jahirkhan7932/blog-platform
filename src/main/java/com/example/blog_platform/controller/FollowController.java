package com.example.blog_platform.controller;

import com.example.blog_platform.dto.ApiResponse;
import com.example.blog_platform.dto.PostResponse;
import com.example.blog_platform.dto.UserSummaryResponse;
import com.example.blog_platform.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Follows", description = "Follow users and personal feed")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/users/{userId}/follow")
    public ResponseEntity<ApiResponse<Void>> follow(@PathVariable Long userId) {
        followService.followUser(userId, currentUsername());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Followed user", null));
    }

    @DeleteMapping("/users/{userId}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(@PathVariable Long userId) {
        followService.unfollowUser(userId, currentUsername());
        return ResponseEntity.ok(ApiResponse.success("Unfollowed user", null));
    }

    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> followers(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Followers", followService.getFollowers(userId)));
    }

    @GetMapping("/users/{userId}/following")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> following(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Following", followService.getFollowing(userId)));
    }

    @GetMapping("/users/{userId}/followers/count")
    public ResponseEntity<ApiResponse<Long>> followersCount(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Follower count", followService.countFollowers(userId)));
    }

    @GetMapping("/users/{userId}/following/count")
    public ResponseEntity<ApiResponse<Long>> followingCount(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Following count", followService.countFollowing(userId)));
    }

    @GetMapping("/users/me/feed")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> myFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostResponse> feed = followService.getPersonalFeed(currentUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Your feed", feed));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
