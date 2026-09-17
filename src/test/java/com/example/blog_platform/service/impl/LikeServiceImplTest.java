package com.example.blog_platform.service.impl;

import com.example.blog_platform.dto.LikeResponse;
import com.example.blog_platform.entity.Like;
import com.example.blog_platform.entity.Post;
import com.example.blog_platform.entity.Role;
import com.example.blog_platform.entity.User;
import com.example.blog_platform.exception.ResourceNotFoundException;
import com.example.blog_platform.repository.LikeRepository;
import com.example.blog_platform.repository.PostRepository;
import com.example.blog_platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    private User alice;
    private Post post;

    @BeforeEach
    void setUp() {
        alice = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .role(Role.AUTHOR)
                .build();

        post = Post.builder()
                .id(10L)
                .title("Test Post")
                .slug("test-post")
                .content("Some content")
                .build();
    }

    // ============ toggleLike ============

    @Test
    @DisplayName("toggleLike: when user hasn't liked the post, adds a like and returns true")
    void toggleLike_whenNotLiked_addsLikeAndReturnsTrue() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(likeRepository.findByUserIdAndPostId(1L, 10L)).thenReturn(Optional.empty());

        boolean result = likeService.toggleLike(10L, "alice");

        assertThat(result).isTrue();
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(likeRepository, never()).delete(any(Like.class));
    }

    @Test
    @DisplayName("toggleLike: when user has already liked, removes the like and returns false")
    void toggleLike_whenAlreadyLiked_removesLikeAndReturnsFalse() {
        Like existingLike = Like.builder().id(99L).user(alice).post(post).build();

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(likeRepository.findByUserIdAndPostId(1L, 10L)).thenReturn(Optional.of(existingLike));

        boolean result = likeService.toggleLike(10L, "alice");

        assertThat(result).isFalse();
        verify(likeRepository, times(1)).delete(existingLike);
        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("toggleLike: throws when the post doesn't exist")
    void toggleLike_whenPostMissing_throwsResourceNotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggleLike(999L, "alice"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found")
                .hasMessageContaining("999");

        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    @DisplayName("toggleLike: throws when the user doesn't exist")
    void toggleLike_whenUserMissing_throwsResourceNotFound() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggleLike(10L, "ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ============ countLikes ============

    @Test
    @DisplayName("countLikes: returns the count from the repository")
    void countLikes_returnsCount() {
        when(likeRepository.countByPostId(10L)).thenReturn(7L);

        long count = likeService.countLikes(10L);

        assertThat(count).isEqualTo(7L);
        verify(likeRepository).countByPostId(10L);
    }

    // ============ getUsersWhoLiked ============

    @Test
    @DisplayName("getUsersWhoLiked: maps Like entities to DTOs correctly")
    void getUsersWhoLiked_returnsMappedList() {
        Like like = Like.builder()
                .id(1L)
                .user(alice)
                .post(post)
                .createdAt(LocalDateTime.now())
                .build();

        when(likeRepository.findByPostIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(like));

        List<LikeResponse> result = likeService.getUsersWhoLiked(10L);

        assertThat(result).hasSize(1);
        LikeResponse first = result.get(0);
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(first.getUserId()).isEqualTo(1L);
        assertThat(first.getUsername()).isEqualTo("alice");
        assertThat(first.getPostId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getUsersWhoLiked: returns empty list when no likes")
    void getUsersWhoLiked_whenEmpty_returnsEmptyList() {
        when(likeRepository.findByPostIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        List<LikeResponse> result = likeService.getUsersWhoLiked(10L);

        assertThat(result).isEmpty();
    }
}
