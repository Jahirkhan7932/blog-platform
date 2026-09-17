package com.example.blog_platform.repository;

import com.example.blog_platform.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // All top-level comments for a post (parent is null), newest first
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(Long postId);

    // All comments (of any level) for a post — useful for counting
    List<Comment> findByPostId(Long postId);

    // All replies to a specific comment
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    // Count comments on a post
    long countByPostId(Long postId);
}
