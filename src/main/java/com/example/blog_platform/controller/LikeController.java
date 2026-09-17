package com.example.blog_platform.controller;

import com.example.blog_platform.dto.ApiResponse;
import com.example.blog_platform.dto.LikeResponse;
import com.example.blog_platform.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Likes", description = "Like / unlike posts")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // Toggle like on a post
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(@PathVariable Long postId) {
        boolean liked = likeService.toggleLike(postId, currentUsername());
        String msg = liked ? "Post liked" : "Post unliked";
        return ResponseEntity.ok(ApiResponse.success(msg, liked));
    }

    // Count likes on a post (public)
    @GetMapping("/posts/{postId}/likes/count")
    public ResponseEntity<ApiResponse<Long>> countLikes(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Like count", likeService.countLikes(postId)));
    }

    // List users who liked a post (public)
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<ApiResponse<List<LikeResponse>>> getUsersWhoLiked(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Likes fetched", likeService.getUsersWhoLiked(postId)));
    }

    // Posts liked by current user
    @GetMapping("/users/me/likes")
    public ResponseEntity<ApiResponse<List<LikeResponse>>> myLikes() {
        return ResponseEntity.ok(ApiResponse.success("My likes", likeService.getPostsLikedByUser(currentUsername())));
    }

    // Helper
    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
