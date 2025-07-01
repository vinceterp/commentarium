package com.commentarium.services;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.commentarium.config.YoutubeApiClient;
import com.commentarium.controllers.posts.PostsRequest;
import com.commentarium.entities.Comment;
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

    public Post createPost(PostsRequest request) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        YouTubeVideoListResponse videoDetails = youtubeApiClient.getVideoDetails(request.getOriginalUrl());

        Post post = Post.builder()
                .author(user)
                .originalUrl(request.getOriginalUrl())
                .title(videoDetails.getItems().get(0).getSnippet().getTitle())
                .createdAt(new java.util.Date())
                .comments(new java.util.ArrayList<Comment>())
                .build();
        Post savedPost = postRepository.save(post);

        return savedPost;
    }

    public Optional<Post> getPostWithComments(Long postId) {
        return postRepository.findById(postId);
    }

}
