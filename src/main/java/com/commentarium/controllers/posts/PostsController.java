package com.commentarium.controllers.posts;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.commentarium.controllers.comments.CommentDTO;
import com.commentarium.entities.Comment;
import com.commentarium.entities.Post;
import com.commentarium.services.PostService;

import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostsRequest request) {
        Post createdPost = postService.createPost(request);
        return ResponseEntity.ok(toDTO(createdPost));
    }

    private PostDTO toDTO(Post post) {
        PostDTO dto = new PostDTO();

        dto.setId(post.getId());
        dto.setAuthor(post.getAuthor());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setOriginalUrl(post.getOriginalUrl());
        dto.setComments(post.getComments().stream()
                .filter(c -> c.getParent() == null)
                .map(this::toDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private CommentDTO toDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setAuthor(comment.getAuthor());
        dto.setContent(comment.getContent());
        dto.setReplies(comment.getReplies().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
        return dto;
    }

}
