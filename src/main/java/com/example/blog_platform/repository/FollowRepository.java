package com.example.blog_platform.repository;

import com.example.blog_platform.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Who follows this user?
    List<Follow> findByFollowingIdOrderByCreatedAtDesc(Long followingId);

    // Who does this user follow?
    List<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId);

    long countByFollowingId(Long userId);   // followers count

    long countByFollowerId(Long userId);    // following count

    // 🔥 Posts from users I follow — the personal feed query
    @Query("""
        SELECT p FROM Post p
        WHERE p.author.id IN (
            SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
        )
        AND p.status = com.example.blog_platform.entity.PostStatus.PUBLISHED
        ORDER BY p.createdAt DESC
    """)
    Page<com.example.blog_platform.entity.Post> findFeedForUser(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
