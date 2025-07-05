package com.commentarium.controllers.posts;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.commentarium.controllers.comments.CommentDTO;
import com.commentarium.entities.Comment;
import com.commentarium.entities.CommentariumApiHelper;
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
    public ResponseEntity<CommentariumApiHelper<PostDTO>> createPost(@RequestBody PostsRequest request) {
        CommentariumApiHelper<Post> createdPost = postService.createPost(request);
        if (createdPost.getData() == null) {
            return ResponseEntity.status(500).body(toDTO(createdPost.getData(), createdPost.getMessage()));
        }
        return ResponseEntity.ok(toDTO(createdPost.getData(), createdPost.getMessage()));
    }

    private CommentariumApiHelper<PostDTO> toDTO(Post post, String message) {

        if (post == null) {
            return CommentariumApiHelper.<PostDTO>builder()
                    .message(message)
                    .status("failure")
                    .data(null)
                    .build();
        }

        CommentariumApiHelper<PostDTO> dto = CommentariumApiHelper.<PostDTO>builder()
                .message(message)
                .status("success")
                .data(PostDTO.builder().id(post.getId())
                        .author(post.getAuthor())
                        .createdAt(post.getCreatedAt())
                        .originalUrl(post.getOriginalUrl())
                        .title(post.getTitle())
                        .viewCount(post.getViewCount())
                        .comments(post.getComments().stream()
                                .filter(c -> c.getParent() == null)
                                .map(this::toDTO)
                                .collect(Collectors.toList()))
                        .build())
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
