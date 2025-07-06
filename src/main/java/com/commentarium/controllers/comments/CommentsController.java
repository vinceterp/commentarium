package com.commentarium.controllers.comments;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.commentarium.entities.Comment;
import com.commentarium.entities.CommentariumApiHelper;
import com.commentarium.services.CommentService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentsController {

        private final CommentService commentService;

        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<CommentariumApiHelper<String>> createComment(@RequestBody CommentRequest request) {
                CommentariumApiHelper<String> createdComment = commentService.createComment(request);
                if (createdComment.getData() == null) {
                        return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).body(createdComment);
                }
                return ResponseEntity.ok(createdComment);
        }

        @GetMapping
        public ResponseEntity<CommentariumApiHelper<List<CommentDTO>>> getComments(@RequestParam("postId") Long postId,
                        Pageable pageable) {
                CommentariumApiHelper<List<Comment>> comments = commentService.getCommentsByPostId(postId, pageable);
                if (comments.getData() == null) {
                        return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND)
                                        .body(CommentariumApiHelper.<List<CommentDTO>>builder()
                                                        .message(comments.getMessage())
                                                        .status(comments.getStatus())
                                                        .data(null)
                                                        .build());
                }
                System.out.println("Fetching comments for postId: " + postId);
                return ResponseEntity.ok(
                                CommentariumApiHelper.<List<CommentDTO>>builder()
                                                .message(comments.getMessage())
                                                .status(comments.getStatus())
                                                .data(comments.getData().stream()
                                                                .map(this::toDTO)
                                                                .collect(Collectors.toList()))
                                                .build());
        }

        private CommentDTO toDTO(Comment comment) {
                CommentDTO dto = CommentDTO.builder()
                                .id(comment.getId())
                                .author(comment.getAuthor())
                                .content(comment.getContent())
                                .likeCount(comment.getLikeCount())
                                .createdAt(comment.getCreatedAt().toString())
                                .replies(comment.getReplies().stream()
                                                .map(this::toDTO)
                                                .collect(Collectors.toList()))
                                .build();
                return dto;
        }
}
