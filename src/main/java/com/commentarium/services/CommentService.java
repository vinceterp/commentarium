package com.commentarium.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.commentarium.controllers.comments.CommentRequest;
import com.commentarium.entities.Comment;
import com.commentarium.entities.CommentariumApiHelper;
import com.commentarium.entities.User;
import com.commentarium.repositories.CommentRepository;
import com.commentarium.repositories.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentariumApiHelper<String> createComment(CommentRequest request) {
        try {
            // Validate the request

            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (request.getPostId() == null || request.getContent() == null || request.getContent().isEmpty()) {
                return CommentariumApiHelper.<String>builder()
                        .message("Invalid request data")
                        .status("error")
                        .data(null)
                        .build();
            }
            // Check if the post exists (you might want to implement this check)
            // Optional: You can add a check to see if the post with request.getPostId()
            // exists
            if (!postRepository.existsById(request.getPostId())) {
                return CommentariumApiHelper.<String>builder()
                        .message("Post not found")
                        .status("error")
                        .data(null)
                        .build();
            }

            // Get parent comment based on parentCommentId if provided
            Comment parentComment = null;
            if (request.getParentCommentId() != null) {
                parentComment = commentRepository.findById(request.getParentCommentId())
                        .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            }

            // Create and save the comment
            Comment comment = Comment.builder()
                    .postId(request.getPostId())
                    .content(request.getContent())
                    .parent(parentComment)
                    .likeCount(0) // Initialize like count to 0
                    .userId(user.getId()) // Assuming User has a getId() method
                    .createdAt(new java.util.Date())
                    .build();

            commentRepository.save(comment);

            return CommentariumApiHelper.<String>builder()
                    .message("Comment created successfully")
                    .status("success")
                    .data("Comment ID: " + comment.getId())
                    .build();
        } catch (Exception e) {
            return CommentariumApiHelper.<String>builder()
                    .message("Error creating comment: " + e.getMessage())
                    .status("error")
                    .data(null)
                    .build();
        }
    }
}
