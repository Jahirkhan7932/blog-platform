package com.example.blog_platform.repository;

import com.example.blog_platform.entity.Post;
import com.example.blog_platform.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(String slug);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    boolean existsBySlug(String slug);

    // ---------- Search by title or content ----------
    // ---------- Search by title or content ----------
    @Query("""
    SELECT p FROM Post p
    WHERE p.status = com.example.blog_platform.entity.PostStatus.PUBLISHED
    AND (p.title LIKE %:q% OR p.content LIKE %:q%)
    ORDER BY p.createdAt DESC
""")
    Page<Post> searchPosts(@Param("q") String q, Pageable pageable);

    // ---------- Posts that have a specific tag slug ----------
    @Query("""
    SELECT p FROM Post p
    JOIN p.tags t
    WHERE t.slug = :tagSlug
    AND p.status = com.example.blog_platform.entity.PostStatus.PUBLISHED
    ORDER BY p.createdAt DESC
""")
    Page<Post> findByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);

    // Posts created in the last N days (for trending calculation)
    @Query("""
    SELECT p FROM Post p
    WHERE p.status = com.example.blog_platform.entity.PostStatus.PUBLISHED
    AND p.createdAt >= :since
    ORDER BY p.createdAt DESC
""")
    List<Post> findPublishedSince(@Param("since") LocalDateTime since);
}