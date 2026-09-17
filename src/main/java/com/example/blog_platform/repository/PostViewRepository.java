package com.example.blog_platform.repository;

import com.example.blog_platform.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);
}
