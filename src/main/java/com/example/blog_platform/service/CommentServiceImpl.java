package com.example.blog_platform.service;

import com.example.blog_platform.dto.CommentRequest;
import com.example.blog_platform.dto.CommentResponse;
import com.example.blog_platform.entity.Comment;
import com.example.blog_platform.entity.Post;
import com.example.blog_platform.entity.User;
import com.example.blog_platform.exception.ResourceNotFoundException;
import com.example.blog_platform.repository.CommentRepository;
import com.example.blog_platform.repository.PostRepository;
import com.example.blog_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // ---------------- Create ----------------

    @Override
    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request, String currentUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        User author = getCurrentUser(currentUsername);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(author)
                .parent(null)          // top-level
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToResponse(saved, new ArrayList<>());
    }

    @Override
    @Transactional
    public CommentResponse addReply(Long parentCommentId, CommentRequest request, String currentUsername) {
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + parentCommentId));
        User author = getCurrentUser(currentUsername);

        Comment reply = Comment.builder()
                .content(request.getContent())
                .post(parent.getPost())
                .author(author)
                .parent(parent)
                .build();

        Comment saved = commentRepository.save(reply);
        return mapToResponse(saved, new ArrayList<>());
    }

    // ---------------- Read (tree) ----------------

    @Override
    public List<CommentResponse> getCommentTreeForPost(Long postId) {
        // Verify the post exists
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        // 1. Fetch ALL comments for this post in one query (oldest first)
        List<Comment> allComments = commentRepository.findByPostId(postId);

        // 2. Sort so replies come after parents naturally
        allComments.sort(Comparator.comparing(Comment::getCreatedAt));

        // 3. Build a map of id -> CommentResponse (empty replies for now)
        Map<Long, CommentResponse> responseMap = new LinkedHashMap<>();
        for (Comment c : allComments) {
            responseMap.put(c.getId(), mapToResponse(c, new ArrayList<>()));
        }

        // 4. Wire up parents and children
        List<CommentResponse> roots = new ArrayList<>();
        for (Comment c : allComments) {
            CommentResponse node = responseMap.get(c.getId());

            if (c.getParent() == null) {
                // top-level comment
                roots.add(node);
            } else {
                // attach to parent's replies list
                CommentResponse parentNode = responseMap.get(c.getParent().getId());
                if (parentNode != null) {
                    parentNode.getReplies().add(node);
                }
            }
        }

        return roots;
    }

    // ---------------- Update ----------------

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, String currentUsername) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Only the author can edit their own comment
        if (!comment.getAuthor().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        Comment updated = commentRepository.save(comment);
        return mapToResponse(updated, new ArrayList<>());
    }

    // ---------------- Delete ----------------

    @Override
    @Transactional
    public void deleteComment(Long commentId, String currentUsername) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getAuthor().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        // cascade=ALL + orphanRemoval=true in the entity will delete all nested replies
        commentRepository.delete(comment);
    }

    @Override
    public long countCommentsForPost(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    // ---------------- Helpers ----------------

    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private CommentResponse mapToResponse(Comment comment, List<CommentResponse> replies) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPost().getId())
                .authorId(comment.getAuthor().getId())
                .authorUsername(comment.getAuthor().getUsername())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replies(replies)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}