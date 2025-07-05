package com.commentarium.services;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.commentarium.config.YoutubeApiClient;
import com.commentarium.controllers.posts.PostsRequest;
import com.commentarium.entities.Comment;
import com.commentarium.entities.CommentariumApiHelper;
import com.commentarium.entities.Post;
import com.commentarium.entities.User;
import com.commentarium.entities.youTubeApi.YouTubeVideoListResponse;
import com.commentarium.repositories.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final YoutubeApiClient youtubeApiClient;
    private final PostRepository postRepository;

    public CommentariumApiHelper<Post> createPost(PostsRequest request) {

        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (user == null) {
                throw new RuntimeException("User not authenticated");
            }

            // Check if the post already exists
            Optional<Post> existingPost = postRepository.findOneByOriginalUrl(request.getOriginalUrl());
            if (existingPost.isPresent()) {
                return CommentariumApiHelper.<Post>builder()
                        .message("Post already exists")
                        .status("success")
                        .data(null)
                        .build();
            }

            YouTubeVideoListResponse videoDetails = youtubeApiClient.getVideoDetails(request.getOriginalUrl());
            if (videoDetails.getItems().isEmpty()) {
                throw new RuntimeException("Video not found or invalid URL");
            }

            Post post = Post.builder()
                    .author(user)
                    .originalUrl(request.getOriginalUrl())
                    .title(videoDetails.getItems().get(0).getSnippet().getTitle())
                    .createdAt(new java.util.Date())
                    .comments(new java.util.ArrayList<Comment>())
                    .viewCount(videoDetails.getItems().get(0).getStatistics().getViewCount())
                    .build();
            Post savedPost = postRepository.save(post);

            CommentariumApiHelper<Post> response = CommentariumApiHelper.<Post>builder()
                    .message("Post created successfully")
                    .status("success")
                    .data(savedPost)
                    .build();

            return response;
        } catch (Exception e) {
            return CommentariumApiHelper.<Post>builder()
                    .message("Error creating post: " + e.getMessage())
                    .status("error")
                    .data(null)
                    .build();

        }

    }

    public Optional<Post> getPostWithComments(Long postId) {
        return postRepository.findById(postId);
    }

}
