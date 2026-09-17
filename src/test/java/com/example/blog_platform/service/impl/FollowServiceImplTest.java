package com.example.blog_platform.service.impl;

import com.example.blog_platform.dto.PostResponse;
import com.example.blog_platform.dto.UserSummaryResponse;
import com.example.blog_platform.entity.Follow;
import com.example.blog_platform.entity.Post;
import com.example.blog_platform.entity.PostStatus;
import com.example.blog_platform.entity.Role;
import com.example.blog_platform.entity.User;
import com.example.blog_platform.exception.DuplicateResourceException;
import com.example.blog_platform.exception.ResourceNotFoundException;
import com.example.blog_platform.repository.FollowRepository;
import com.example.blog_platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock private FollowRepository followRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private FollowServiceImpl followService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .role(Role.AUTHOR)
                .build();

        bob = User.builder()
                .id(2L)
                .username("bob")
                .email("bob@example.com")
                .password("hashed")
                .role(Role.AUTHOR)
                .build();
    }

    // ==========================================
    // followUser
    // ==========================================

    @Test
    @DisplayName("followUser: saves a Follow row when valid")
    void followUser_savesFollowWhenValid() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

        followService.followUser(2L, "alice");

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("followUser: throws DuplicateResourceException when following yourself")
    void followUser_throwsWhenFollowingSelf() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> followService.followUser(1L, "alice"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("cannot follow yourself");

        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("followUser: throws DuplicateResourceException when already following")
    void followUser_throwsWhenAlreadyFollowing() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> followService.followUser(2L, "alice"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already follow");

        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("followUser: throws ResourceNotFoundException when target user missing")
    void followUser_throwsWhenTargetMissing() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.followUser(999L, "alice"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ==========================================
    // unfollowUser
    // ==========================================

    @Test
    @DisplayName("unfollowUser: deletes the Follow row when it exists")
    void unfollowUser_deletesWhenFollowing() {
        Follow existingFollow = Follow.builder()
                .id(10L)
                .follower(alice)
                .following(bob)
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(Optional.of(existingFollow));

        followService.unfollowUser(2L, "alice");

        verify(followRepository).delete(existingFollow);
    }

    @Test
    @DisplayName("unfollowUser: throws ResourceNotFoundException when not following")
    void unfollowUser_throwsWhenNotFollowing() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.unfollowUser(2L, "alice"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not following");

        verify(followRepository, never()).delete(any(Follow.class));
    }

    // ==========================================
    // getFollowers / getFollowing
    // ==========================================

    @Test
    @DisplayName("getFollowers: returns mapped UserSummaryResponse list")
    void getFollowers_mapsCorrectly() {
        Follow follow = Follow.builder()
                .id(1L)
                .follower(alice)      // alice follows bob
                .following(bob)
                .build();

        when(userRepository.existsById(2L)).thenReturn(true);
        when(followRepository.findByFollowingIdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(follow));

        List<UserSummaryResponse> result = followService.getFollowers(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("getFollowing: returns mapped UserSummaryResponse list")
    void getFollowing_mapsCorrectly() {
        Follow follow = Follow.builder()
                .id(1L)
                .follower(alice)
                .following(bob)
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(followRepository.findByFollowerIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(follow));

        List<UserSummaryResponse> result = followService.getFollowing(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getUsername()).isEqualTo("bob");
    }

    // ==========================================
    // getPersonalFeed
    // ==========================================

    @Test
    @DisplayName("getPersonalFeed: returns posts from followed users")
    void getPersonalFeed_returnsPostsFromFollowedUsers() {
        Post bobPost = Post.builder()
                .id(50L)
                .title("Bob's Post")
                .slug("bobs-post")
                .content("Content")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .viewCount(0L)
                .author(bob)
                .tags(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(followRepository.findFeedForUser(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bobPost)));

        Page<PostResponse> feed = followService.getPersonalFeed("alice", 0, 10);

        assertThat(feed.getContent()).hasSize(1);
        assertThat(feed.getContent().get(0).getAuthorUsername()).isEqualTo("bob");
        assertThat(feed.getContent().get(0).getSlug()).isEqualTo("bobs-post");
    }

    @Test
    @DisplayName("getPersonalFeed: returns empty when following nobody")
    void getPersonalFeed_emptyWhenNoFollows() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(followRepository.findFeedForUser(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<PostResponse> feed = followService.getPersonalFeed("alice", 0, 10);

        assertThat(feed.getContent()).isEmpty();
    }
}