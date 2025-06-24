package com.commentarium.controllers.comments;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentsController {

    @GetMapping
    public ResponseEntity<String> getComments() {
        return ResponseEntity.ok("Comments Sections");
    }
}
