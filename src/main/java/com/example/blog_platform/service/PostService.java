package com.example.blog_platform.service;

import com.example.blog_platform.dto.PostRequest;
import com.example.blog_platform.dto.PostResponse;
import com.example.blog_platform.entity.PostStatus;
import org.springframework.data.domain.Page;
import com.example.blog_platform.dto.TrendingPostResponse;
import java.util.List;

public interface PostService {

    PostResponse createPost(PostRequest request);

    List<TrendingPostResponse> getTrendingPosts(int days, int limit);

    PostResponse getPostById(Long id);

    PostResponse getPostBySlug(String slug);

    Page<PostResponse> getAllPosts(int page, int size, String sortBy);

    Page<PostResponse> getPostsByStatus(PostStatus status, int page, int size);

    PostResponse updatePost(Long id, PostRequest request);

    void deletePost(Long id);

    Page<PostResponse> searchPosts(String query, int page, int size);

    Page<PostResponse> getPostsByTag(String tagSlug, int page, int size);

    PostResponse recordView(Long postId, String currentUsername);
}