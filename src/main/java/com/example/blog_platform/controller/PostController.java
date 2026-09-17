package com.example.blog_platform.controller;

import com.example.blog_platform.dto.ApiResponse;
import com.example.blog_platform.dto.PostRequest;
import com.example.blog_platform.dto.PostResponse;
import com.example.blog_platform.entity.PostStatus;
import com.example.blog_platform.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.blog_platform.dto.TrendingPostResponse;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Posts", description = "Post CRUD, search, view counter, trending")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ---------- CREATE ----------
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", response));
    }

    // ---------- LIST ----------
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Page<PostResponse> posts = postService.getAllPosts(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Posts fetched", posts));
    }

    // ---------- SEARCH ----------
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> searchPosts(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostResponse> results = postService.searchPosts(query, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    // ---------- BY TAG ----------
    @GetMapping("/tag/{slug}")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPostsByTag(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostResponse> results = postService.getPostsByTag(slug, page, size);
        return ResponseEntity.ok(ApiResponse.success("Posts by tag", results));
    }

    // ---------- BY STATUS ----------
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPostsByStatus(
            @PathVariable PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostResponse> posts = postService.getPostsByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Posts fetched", posts));
    }

    // ---------- BY SLUG ----------
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Post fetched", postService.getPostBySlug(slug)));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<TrendingPostResponse>>> getTrending(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        List<TrendingPostResponse> trending = postService.getTrendingPosts(days, limit);
        return ResponseEntity.ok(ApiResponse.success("Trending posts", trending));
    }

    // ---------- BY ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Post fetched", postService.getPostById(id)));
    }

    // ---------- UPDATE ----------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Post updated", postService.updatePost(id, request)));
    }

    // ---------- DELETE ----------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted", null));
    }

    // ---------- RECORD VIEW ----------
    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<PostResponse>> recordView(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("View recorded",
                postService.recordView(id, currentUsername())));
    }

    // ---------- HELPER ----------
    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}