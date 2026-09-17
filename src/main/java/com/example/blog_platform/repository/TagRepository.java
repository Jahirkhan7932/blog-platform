package com.example.blog_platform.repository;

import com.example.blog_platform.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findBySlug(String slug);
    Optional<Tag> findByName(String name);
}
