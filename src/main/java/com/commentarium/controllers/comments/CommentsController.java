package com.commentarium.controllers.comments;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// import com.commentarium.entities.User;

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
    public ResponseEntity<String> getComments(@RequestParam("postId") Long postId) {
        System.out.println("Fetching comments for postId: " + postId);
        return ResponseEntity.ok("Comments Sections");
    }
}
