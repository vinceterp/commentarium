package com.commentarium.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.commentarium.controllers.comments.CommentRequest;
import com.commentarium.controllers.comments.DeleteCommentRequest;
import com.commentarium.controllers.comments.UpdateCommentRequest;
import com.commentarium.entities.Comment;
import com.commentarium.entities.CommentariumApiHelper;
import com.commentarium.entities.Role;
import com.commentarium.entities.User;
import com.commentarium.repositories.CommentRepository;
import com.commentarium.repositories.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentariumApiHelper<List<Comment>> getCommentsByPostId(Long postId, Pageable pageable) {
        try {
            // Check if the post exists
            if (!postRepository.existsById(postId)) {
                throw new RuntimeException("Post not found");
            }
            List<Comment> comments = commentRepository.findByPostId(postId, pageable);
            // Only include top-level comments (parent == null)
            List<Comment> topLevelComments = comments.stream()
                    .filter(c -> c.getParent() == null)
                    .toList();
            return CommentariumApiHelper.<List<Comment>>builder()
                    .message("Comments fetched successfully")
                    .status("success")
                    .data(topLevelComments)
                    .build();
        } catch (Exception e) {
            return CommentariumApiHelper.<List<Comment>>builder()
                    .message("Error fetching comments: " + e.getMessage())
                    .status("error")
                    .data(null)
                    .build();
        }
    }

    public CommentariumApiHelper<String> createComment(CommentRequest request) {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (user == null) {
                throw new RuntimeException("User not authenticated");
            }

            if (request.getPostId() == null || request.getContent() == null || request.getContent().isEmpty()) {
                throw new RuntimeException("Invalid request data");
            }

            if (!postRepository.existsById(request.getPostId())) {
                throw new RuntimeException("Post not found");
            }

            // Get parent comment based on parentCommentId and postId if provided
            Comment parentComment = null;
            if (request.getParentCommentId() != null
                    && !commentRepository.existsByIdAndPostId(request.getParentCommentId(), request.getPostId())) {
                throw new RuntimeException("Parent comment not found");

            }

            if (request.getParentCommentId() != null) {
                // Fetch the parent comment if parentCommentId is provided
                parentComment = commentRepository.findById(request.getParentCommentId())
                        .orElse(null); // If parentCommentId is not provided, this will be null
            }
            // Create and save the comment
            Comment comment = Comment.builder()
                    .postId(request.getPostId())
                    .content(request.getContent())
                    .parent(parentComment)
                    .likeCount(0) // Initialize like count to 0
                    .author(user) // Assuming User has a getId() method
                    .createdAt(new java.util.Date())
                    .build();

            Comment createdComment = commentRepository.save(comment);

            return CommentariumApiHelper.<String>builder()
                    .message("Comment created successfully")
                    .status("success")
                    .data("Comment ID: " + createdComment.getId())
                    .build();
        } catch (Exception e) {
            return CommentariumApiHelper.<String>builder()
                    .message("Error creating comment: " + e.getMessage())
                    .status("error")
                    .data(null)
                    .build();
        }
    }

    public CommentariumApiHelper<String> deleteComment(DeleteCommentRequest request) {
        try {

            Optional<Comment> comment = commentRepository.findByIdAndPostId(request.getCommentId(),
                    request.getPostId());
            if (comment.isEmpty()) {
                throw new RuntimeException("Comment not found for the given post");
            }

            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (user == null || !user.getId().equals(comment.get().getAuthor().getId())
                    && !user.getRole().equals(Role.ADMIN)) {
                throw new RuntimeException("User not authorized to delete this comment");
            }

            commentRepository.deleteById(request.getCommentId());

            return CommentariumApiHelper.<String>builder()
                    .message("Comment deleted successfully")
                    .status("success")
                    .data("Comment ID: " + request.getCommentId())
                    .build();
        } catch (Exception e) {
            return CommentariumApiHelper.<String>builder()
                    .message("Error deleting comment: " + e.getMessage())
                    .status("error")
                    .data(null)
                    .build();
        }
    }

    public CommentariumApiHelper<String> updateComment(UpdateCommentRequest request) {
        try {

            Optional<Comment> comment = commentRepository.findByIdAndPostId(request.getCommentId(),
                    request.getPostId());

            if (comment.isEmpty()) {
                throw new RuntimeException("Comment not found for the given post");
            }

            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (request.getContent() != null && (user == null || !user.getId().equals(comment.get().getAuthor().getId())
                    && !user.getRole().equals(Role.ADMIN))) {
                throw new RuntimeException("User not authorized to update this comment");
            }

            if (request.getContent() == null && request.getLikedBy() == null) {
                throw new RuntimeException("Both content and likedBy cannot be empty");
            }

            Comment existingComment = comment.get();
            if (request.getLikedBy() != null) {
                // TO-DO Change the type of likedBy to a list of userIds if you want to track
                // which users liked the comment
                existingComment.setLikeCount(existingComment.getLikeCount() + 1);
            }
            if (request.getContent() != null && !request.getContent().isEmpty()) {
                existingComment.setContent(request.getContent());
            }
            existingComment.setUpdatedAt(new java.util.Date()); // Assuming you have an updatedAt field
            commentRepository.save(existingComment);
            return CommentariumApiHelper.<String>builder()
                    .message("Comment updated successfully")
                    .status("success")
                    .data("Comment ID: " + request.getCommentId())
                    .build();
        } catch (Exception e) {
            return CommentariumApiHelper.<String>builder()
                    .message("Error updating comment: " + e.getMessage())
                    .status("error")
                    .data(null)
                    .build();
        }
    }
}
