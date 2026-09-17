package com.example.blog_platform.service;

import com.example.blog_platform.dto.BookmarkResponse;

import java.util.List;

public interface BookmarkService {

    boolean toggleBookmark(Long postId, String currentUsername);

    List<BookmarkResponse> getMyBookmarks(String currentUsername);

    long countBookmarks(Long postId);
}
