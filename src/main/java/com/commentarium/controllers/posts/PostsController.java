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

        PostDTO dto = PostDTO.builder()
                .id(post.getId())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .originalUrl(post.getOriginalUrl())
                .title(post.getTitle())
                .comments(post.getComments().stream()
                        .filter(c -> c.getParent() == null)
                        .map(this::toDTO)
                        .collect(Collectors.toList()))

                .build();
        return dto;
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
