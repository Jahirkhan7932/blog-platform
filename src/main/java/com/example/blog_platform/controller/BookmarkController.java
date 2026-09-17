package com.example.blog_platform.controller;

import com.example.blog_platform.dto.ApiResponse;
import com.example.blog_platform.dto.BookmarkResponse;
import com.example.blog_platform.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Bookmarks", description = "Save posts for later")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // Toggle bookmark on a post
    @PostMapping("/posts/{postId}/bookmark")
    public ResponseEntity<ApiResponse<Boolean>> toggleBookmark(@PathVariable Long postId) {
        boolean bookmarked = bookmarkService.toggleBookmark(postId, currentUsername());
        String msg = bookmarked ? "Post bookmarked" : "Bookmark removed";
        return ResponseEntity.ok(ApiResponse.success(msg, bookmarked));
    }

    // My bookmarks (private)
    @GetMapping("/users/me/bookmarks")
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> myBookmarks() {
        return ResponseEntity.ok(ApiResponse.success("My bookmarks",
                bookmarkService.getMyBookmarks(currentUsername())));
    }

    // Public count
    @GetMapping("/posts/{postId}/bookmarks/count")
    public ResponseEntity<ApiResponse<Long>> countBookmarks(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("Bookmark count",
                bookmarkService.countBookmarks(postId)));
    }

    // Helper
    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
