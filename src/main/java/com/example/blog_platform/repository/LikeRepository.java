package com.example.blog_platform.repository;

import com.example.blog_platform.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // Check if a specific user liked a specific post
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // Find the row (for deletion)
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    // Count likes on a post
    long countByPostId(Long postId);

    // Get all likes on a post (for listing who liked)
    List<Like> findByPostIdOrderByCreatedAtDesc(Long postId);

    // Get all posts a user liked (for "my likes" page)
    List<Like> findByUserIdOrderByCreatedAtDesc(Long userId);
}
