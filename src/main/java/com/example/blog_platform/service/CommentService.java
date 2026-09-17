package com.example.blog_platform.service;

import com.example.blog_platform.dto.CommentRequest;
import com.example.blog_platform.dto.CommentResponse;

import java.util.List;

public interface CommentService {

    CommentResponse addComment(Long postId, CommentRequest request, String currentUsername);

    CommentResponse addReply(Long parentCommentId, CommentRequest request, String currentUsername);

    List<CommentResponse> getCommentTreeForPost(Long postId);

    CommentResponse updateComment(Long commentId, CommentRequest request, String currentUsername);

    void deleteComment(Long commentId, String currentUsername);

    long countCommentsForPost(Long postId);
}
