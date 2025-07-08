package com.commentarium.services;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.commentarium.config.YoutubeApiClient;
import com.commentarium.controllers.posts.PostsRequest;
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
                throw new RuntimeException("Post already exists for this URL");
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

    public CommentariumApiHelper<Post> getPostByVideoId(String videoId) {
        // Build the youtube url from the videoId
        try {
            String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
            Optional<Post> post = postRepository.findOneByOriginalUrl(youtubeUrl);

            if (post.isEmpty()) {
                throw new RuntimeException("Post not found for the given video ID");
            }

            YouTubeVideoListResponse videoDetails = youtubeApiClient.getVideoDetails(youtubeUrl);

            post.get().setViewCount(videoDetails.getItems().get(0).getStatistics().getViewCount());

            return CommentariumApiHelper.<Post>builder()
                    .message("Post found")
                    .status("success")
                    .data(post.get())
                    .build();

        } catch (Exception e) {
            return CommentariumApiHelper.<Post>builder()
                    .message("Error fetching post: " + e.getMessage())
                    .status("error")
                    .data(null)
                    .build();
        }
    }

}
