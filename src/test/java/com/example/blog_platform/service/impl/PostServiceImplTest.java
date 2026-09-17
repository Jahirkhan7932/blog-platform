package com.example.blog_platform.service.impl;

import com.example.blog_platform.dto.PostRequest;
import com.example.blog_platform.dto.PostResponse;
import com.example.blog_platform.dto.TrendingPostResponse;
import com.example.blog_platform.entity.Post;
import com.example.blog_platform.entity.PostStatus;
import com.example.blog_platform.entity.PostView;
import com.example.blog_platform.entity.User;
import com.example.blog_platform.exception.ResourceNotFoundException;
import com.example.blog_platform.repository.CommentRepository;
import com.example.blog_platform.repository.LikeRepository;
import com.example.blog_platform.repository.PostRepository;
import com.example.blog_platform.repository.PostViewRepository;
import com.example.blog_platform.repository.TagRepository;
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
class PostServiceImplTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private TagRepository tagRepository;
    @Mock private PostViewRepository postViewRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private User alice;
    private Post samplePost;

    @BeforeEach
    void setUp() {
        alice = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .build();

        samplePost = Post.builder()
                .id(100L)
                .title("My First Blog Post")
                .slug("my-first-blog-post")
                .content("This is some content for testing.")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .viewCount(0L)
                .author(alice)
                .tags(new HashSet<>())
                .build();
    }

    // ==========================================
    // createPost
    // ==========================================

    @Test
    @DisplayName("createPost: saves and returns a DTO with generated slug and reading time")
    void createPost_generatesSlugAndReadingTime() {
        PostRequest request = PostRequest.builder()
                .title("Hello World!")
                .content("Short content here for the test.")
                .authorId(1L)
                .status(PostStatus.PUBLISHED)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.existsBySlug("hello-world")).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setId(200L);
            return p;
        });

        PostResponse response = postService.createPost(request);

        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getTitle()).isEqualTo("Hello World!");
        assertThat(response.getSlug()).isEqualTo("hello-world");
        assertThat(response.getReadingTime()).isGreaterThanOrEqualTo(1);
        assertThat(response.getAuthorUsername()).isEqualTo("alice");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("createPost: throws when author does not exist")
    void createPost_throwsWhenAuthorMissing() {
        PostRequest request = PostRequest.builder()
                .title("Test")
                .content("Content")
                .authorId(999L)
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author not found")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("createPost: appends -2 to slug if base slug already exists")
    void createPost_handlesSlugCollision() {
        PostRequest request = PostRequest.builder()
                .title("Hello World!")
                .content("Content")
                .authorId(1L)
                .status(PostStatus.PUBLISHED)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.existsBySlug("hello-world")).thenReturn(true);
        when(postRepository.existsBySlug("hello-world-2")).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(201L);
            return p;
        });

        PostResponse response = postService.createPost(request);

        assertThat(response.getSlug()).isEqualTo("hello-world-2");
    }

    // ==========================================
    // getPostById / getPostBySlug
    // ==========================================

    @Test
    @DisplayName("getPostById: returns mapped DTO when found")
    void getPostById_returnsDto() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(samplePost));

        PostResponse response = postService.getPostById(100L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getSlug()).isEqualTo("my-first-blog-post");
    }

    @Test
    @DisplayName("getPostById: throws when not found")
    void getPostById_throwsWhenMissing() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found");
    }

    // ==========================================
    // searchPosts
    // ==========================================

    @Test
    @DisplayName("searchPosts: throws when query is blank")
    void searchPosts_throwsOnBlankQuery() {
        assertThatThrownBy(() -> postService.searchPosts("  ", 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Search query cannot be empty");
    }

    @Test
    @DisplayName("searchPosts: passes trimmed query to repository")
    void searchPosts_trimsQuery() {
        when(postRepository.searchPosts(eq("spring"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(samplePost)));

        Page<PostResponse> results = postService.searchPosts("  spring  ", 0, 10);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getSlug()).isEqualTo("my-first-blog-post");
        verify(postRepository).searchPosts(eq("spring"), any(Pageable.class));
    }

    // ==========================================
    // getPostsByTag
    // ==========================================

    @Test
    @DisplayName("getPostsByTag: delegates to repository and maps results")
    void getPostsByTag_returnsMappedPage() {
        when(postRepository.findByTagSlug(eq("java"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(samplePost)));

        Page<PostResponse> results = postService.getPostsByTag("java", 0, 10);

        assertThat(results.getContent()).hasSize(1);
        verify(postRepository).findByTagSlug(eq("java"), any(Pageable.class));
    }

    // ==========================================
    // recordView
    // ==========================================

    @Test
    @DisplayName("recordView: increments count when user hasn't viewed before")
    void recordView_incrementsWhenNewView() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(samplePost));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(postViewRepository.existsByUserIdAndPostId(1L, 100L)).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponse response = postService.recordView(100L, "alice");

        assertThat(response.getViewCount()).isEqualTo(1L);
        verify(postViewRepository).save(any(PostView.class));
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("recordView: does NOT increment when user already viewed")
    void recordView_skipsWhenAlreadyViewed() {
        samplePost.setViewCount(5L);

        when(postRepository.findById(100L)).thenReturn(Optional.of(samplePost));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(postViewRepository.existsByUserIdAndPostId(1L, 100L)).thenReturn(true);

        PostResponse response = postService.recordView(100L, "alice");

        assertThat(response.getViewCount()).isEqualTo(5L);
        verify(postViewRepository, never()).save(any(PostView.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("recordView: throws when post missing")
    void recordView_throwsWhenPostMissing() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.recordView(999L, "alice"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found");
    }

    // ==========================================
    // getTrendingPosts
    // ==========================================

    @Test
    @DisplayName("getTrendingPosts: computes score from likes, comments, views, and recency")
    void getTrendingPosts_computesScore() {
        Post fresh = Post.builder()
                .id(1L)
                .title("Fresh")
                .slug("fresh")
                .status(PostStatus.PUBLISHED)
                .viewCount(10L)
                .author(alice)
                .tags(new HashSet<>())
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        when(postRepository.findPublishedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(fresh));
        when(likeRepository.countByPostId(1L)).thenReturn(5L);
        when(commentRepository.countByPostId(1L)).thenReturn(3L);

        List<TrendingPostResponse> result = postService.getTrendingPosts(7, 10);

        assertThat(result).hasSize(1);
        TrendingPostResponse trending = result.get(0);
        assertThat(trending.getLikeCount()).isEqualTo(5L);
        assertThat(trending.getCommentCount()).isEqualTo(3L);
        assertThat(trending.getViewCount()).isEqualTo(10L);
        // score = 5*3 + 3*2 + 10*1 - 2*0.5 = 15 + 6 + 10 - 1 = 30.0
        assertThat(trending.getScore()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("getTrendingPosts: sorts by score descending and applies limit")
    void getTrendingPosts_sortsAndLimits() {
        Post lowEngagement = Post.builder()
                .id(1L).title("Low").slug("low").status(PostStatus.PUBLISHED)
                .viewCount(1L).author(alice).tags(new HashSet<>())
                .createdAt(LocalDateTime.now()).build();

        Post highEngagement = Post.builder()
                .id(2L).title("High").slug("high").status(PostStatus.PUBLISHED)
                .viewCount(100L).author(alice).tags(new HashSet<>())
                .createdAt(LocalDateTime.now()).build();

        when(postRepository.findPublishedSince(any(LocalDateTime.class)))
                .thenReturn(List.of(lowEngagement, highEngagement));
        when(likeRepository.countByPostId(1L)).thenReturn(0L);
        when(likeRepository.countByPostId(2L)).thenReturn(10L);
        when(commentRepository.countByPostId(1L)).thenReturn(0L);
        when(commentRepository.countByPostId(2L)).thenReturn(5L);

        List<TrendingPostResponse> result = postService.getTrendingPosts(7, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPost().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getTrendingPosts: returns empty when no candidates")
    void getTrendingPosts_emptyWhenNoCandidates() {
        when(postRepository.findPublishedSince(any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<TrendingPostResponse> result = postService.getTrendingPosts(7, 10);

        assertThat(result).isEmpty();
    }

    // ==========================================
    // deletePost
    // ==========================================

    @Test
    @DisplayName("deletePost: throws when post doesn't exist")
    void deletePost_throwsWhenMissing() {
        when(postRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> postService.deletePost(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found");

        verify(postRepository, never()).deleteById(any());
    }
}