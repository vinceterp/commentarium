package com.commentarium.controllers.comments;

import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// import com.commentarium.entities.User;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentsController {

    @GetMapping
    public ResponseEntity<String> getComments() {
        // This requires that you have a valid authentication setup in your application.
        // If you don't need the user information, you can remove this line.
        // This line retrieves the currently authenticated user from the security context.
        // User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok("Comments Sections");
    }
}
