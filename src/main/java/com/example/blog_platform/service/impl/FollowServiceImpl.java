package com.example.blog_platform.service.impl;

import com.example.blog_platform.dto.PostResponse;
import com.example.blog_platform.dto.UserSummaryResponse;
import com.example.blog_platform.entity.Follow;
import com.example.blog_platform.entity.Post;
import com.example.blog_platform.entity.Tag;
import com.example.blog_platform.entity.User;
import com.example.blog_platform.exception.DuplicateResourceException;
import com.example.blog_platform.exception.ResourceNotFoundException;
import com.example.blog_platform.repository.FollowRepository;
import com.example.blog_platform.repository.UserRepository;
import com.example.blog_platform.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void followUser(Long userId, String currentUsername) {
        User currentUser = getCurrentUser(currentUsername);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (currentUser.getId().equals(target.getId())) {
            throw new DuplicateResourceException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), target.getId())) {
            throw new DuplicateResourceException("You already follow this user");
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(target)
                .build();
        followRepository.save(follow);
    }

    @Override
    @Transactional
    public void unfollowUser(Long userId, String currentUsername) {
        User currentUser = getCurrentUser(currentUsername);

        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(currentUser.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("You are not following this user"));

        followRepository.delete(follow);
    }

    @Override
    public List<UserSummaryResponse> getFollowers(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return followRepository.findByFollowingIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(f -> mapToUserSummary(f.getFollower()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSummaryResponse> getFollowing(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(f -> mapToUserSummary(f.getFollowing()))
                .collect(Collectors.toList());
    }

    @Override
    public long countFollowers(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Override
    public long countFollowing(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Override
    public Page<PostResponse> getPersonalFeed(String currentUsername, int page, int size) {
        User currentUser = getCurrentUser(currentUsername);
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> feed = followRepository.findFeedForUser(currentUser.getId(), pageable);
        return feed.map(this::mapPostToResponse);
    }

    // ---------- Helpers ----------

    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private UserSummaryResponse mapToUserSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }

    private PostResponse mapPostToResponse(Post post) {
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
