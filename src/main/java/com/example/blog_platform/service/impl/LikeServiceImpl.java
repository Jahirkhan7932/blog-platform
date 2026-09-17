package com.example.blog_platform.service.impl;

import com.example.blog_platform.dto.LikeResponse;
import com.example.blog_platform.entity.Like;
import com.example.blog_platform.entity.Post;
import com.example.blog_platform.entity.User;
import com.example.blog_platform.exception.ResourceNotFoundException;
import com.example.blog_platform.repository.LikeRepository;
import com.example.blog_platform.repository.PostRepository;
import com.example.blog_platform.repository.UserRepository;
import com.example.blog_platform.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public boolean toggleLike(Long postId, String currentUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        User user = getCurrentUser(currentUsername);

        // If already liked → remove (unlike)
        Optional<Like> existing = likeRepository.findByUserIdAndPostId(user.getId(), postId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false;   // now unliked
        }

        // Otherwise → add like
        Like like = Like.builder()
                .user(user)
                .post(post)
                .build();
        likeRepository.save(like);
        return true;    // now liked
    }

    @Override
    public long countLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Override
    public List<LikeResponse> getUsersWhoLiked(Long postId) {
        return likeRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LikeResponse> getPostsLikedByUser(String currentUsername) {
        User user = getCurrentUser(currentUsername);
        return likeRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------- Helpers ----------

    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private LikeResponse mapToResponse(Like like) {
        return LikeResponse.builder()
                .id(like.getId())
                .userId(like.getUser().getId())
                .username(like.getUser().getUsername())
                .postId(like.getPost().getId())
                .createdAt(like.getCreatedAt())
                .build();
    }
}
