package com.example.blog_platform.controller;

import com.example.blog_platform.dto.ApiResponse;
import com.example.blog_platform.dto.CommentRequest;
import com.example.blog_platform.dto.CommentResponse;
import com.example.blog_platform.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Comments", description = "Threaded comments and replies")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ---------- Add a top-level comment ----------
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.addComment(postId, request, currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", response));
    }

    // ---------- Reply to a comment ----------
    @PostMapping("/comments/{commentId}/reply")
    public ResponseEntity<ApiResponse<CommentResponse>> replyToComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.addReply(commentId, request, currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reply added", response));
    }

    // ---------- Get all comments on a post (nested tree) ----------
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsForPost(
            @PathVariable Long postId) {
        List<CommentResponse> tree = commentService.getCommentTreeForPost(postId);
        return ResponseEntity.ok(ApiResponse.success("Comments fetched", tree));
    }

    // ---------- Edit a comment ----------
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.updateComment(commentId, request, currentUsername());
        return ResponseEntity.ok(ApiResponse.success("Comment updated", response));
    }

    // ---------- Delete a comment ----------
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId, currentUsername());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }

    // ---------- Helper: get the logged-in user's username from JWT ----------
    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }


}
