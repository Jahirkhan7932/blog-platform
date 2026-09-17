package com.example.blog_platform.service.impl;

import com.example.blog_platform.dto.PostRequest;
import com.example.blog_platform.dto.PostResponse;
import com.example.blog_platform.dto.TrendingPostResponse;
import com.example.blog_platform.entity.Post;
import com.example.blog_platform.entity.PostStatus;
import com.example.blog_platform.entity.PostView;
import com.example.blog_platform.entity.Tag;
import com.example.blog_platform.entity.User;
import com.example.blog_platform.exception.ResourceNotFoundException;
import com.example.blog_platform.repository.CommentRepository;
import com.example.blog_platform.repository.LikeRepository;
import com.example.blog_platform.repository.PostRepository;
import com.example.blog_platform.repository.PostViewRepository;
import com.example.blog_platform.repository.TagRepository;
import com.example.blog_platform.repository.UserRepository;
import com.example.blog_platform.service.PostService;
import com.example.blog_platform.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostViewRepository postViewRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    // ================= CREATE =================

    @Override
    @Transactional
    public PostResponse createPost(PostRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Author not found with id: " + request.getAuthorId()));

        String slug = generateUniqueSlug(request.getTitle());

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(slug)
                .content(request.getContent())
                .coverImageUrl(request.getCoverImageUrl())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT)
                .readingTime(calculateReadingTime(request.getContent()))
                .viewCount(0L)
                .author(author)
                .tags(resolveTags(request.getTags()))
                .build();

        Post saved = postRepository.save(post);
        return mapToResponse(saved);
    }

    // ================= READ =================

    @Override
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return mapToResponse(post);
    }

    @Override
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with slug: " + slug));
        return mapToResponse(post);
    }

    @Override
    public Page<PostResponse> getAllPosts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return postRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<PostResponse> getPostsByStatus(PostStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<PostResponse> searchPosts(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.searchPosts(query.trim(), pageable).map(this::mapToResponse);
    }

    @Override
    public Page<PostResponse> getPostsByTag(String tagSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByTagSlug(tagSlug, pageable).map(this::mapToResponse);
    }

    // ================= UPDATE =================

    @Override
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (!post.getTitle().equals(request.getTitle())) {
            post.setSlug(generateUniqueSlug(request.getTitle()));
            post.setTitle(request.getTitle());
        }

        post.setContent(request.getContent());
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setReadingTime(calculateReadingTime(request.getContent()));

        if (request.getStatus() != null) {
            post.setStatus(request.getStatus());
        }
        if (request.getTags() != null) {
            post.setTags(resolveTags(request.getTags()));
        }

        Post updated = postRepository.save(post);
        return mapToResponse(updated);
    }

    // ================= DELETE =================

    @Override
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }

    // ================= VIEW COUNTER =================

    @Override
    @Transactional
    public PostResponse recordView(Long postId, String currentUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        boolean alreadyViewed = postViewRepository.existsByUserIdAndPostId(user.getId(), postId);

        if (!alreadyViewed) {
            postViewRepository.save(PostView.builder()
                    .user(user)
                    .post(post)
                    .build());

            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        }

        return mapToResponse(post);
    }

    // ================= TRENDING =================

    @Override
    public List<TrendingPostResponse> getTrendingPosts(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Post> candidates = postRepository.findPublishedSince(since);

        List<TrendingPostResponse> scored = new ArrayList<>();

        for (Post post : candidates) {
            long likeCount = likeRepository.countByPostId(post.getId());
            long commentCount = commentRepository.countByPostId(post.getId());
            long viewCount = post.getViewCount();

            double score = (likeCount * 3.0)
                    + (commentCount * 2.0)
                    + (viewCount * 1.0);

            long hoursAgo = ChronoUnit.HOURS.between(post.getCreatedAt(), LocalDateTime.now());
            score -= hoursAgo * 0.5;

            scored.add(TrendingPostResponse.builder()
                    .post(mapToResponse(post))
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .viewCount(viewCount)
                    .score(score)
                    .build());
        }

        return scored.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ================= HELPERS =================

    private String generateUniqueSlug(String title) {
        String base = SlugUtil.toSlug(title);
        String slug = base;
        int counter = 2;
        while (postRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    private int calculateReadingTime(String content) {
        if (content == null || content.isBlank()) return 0;
        int words = content.trim().split("\\s+").length;
        return Math.max(1, words / 200);
    }

    private Set<Tag> resolveTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames == null) return tags;

        for (String name : tagNames) {
            String cleanName = name.trim().toLowerCase();
            if (cleanName.isEmpty()) continue;

            Tag tag = tagRepository.findByName(cleanName)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder()
                                    .name(cleanName)
                                    .slug(SlugUtil.toSlug(cleanName))
                                    .build()));
            tags.add(tag);
        }
        return tags;
    }

    private PostResponse mapToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .coverImageUrl(post.getCoverImageUrl())
                .status(post.getStatus())
                .readingTime(post.getReadingTime())
                .viewCount(post.getViewCount())
                .authorId(post.getAuthor().getId())
                .authorUsername(post.getAuthor().getUsername())
                .tags(post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}