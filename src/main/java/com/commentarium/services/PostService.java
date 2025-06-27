package com.commentarium.services;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.commentarium.controllers.posts.PostsRequest;
import com.commentarium.entities.Post;
import com.commentarium.entities.User;
import com.commentarium.repositories.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Post createPost(PostsRequest request) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Post post = new Post();
        post.setCreatedAt(new java.util.Date());
        post.setAuthor(user);
        post.setOriginalUrl(request.getOriginalUrl());
        Post savedPost = postRepository.save(post);

        return savedPost;
    }

    public Optional<Post> getPostWithComments(Long postId) {
        return postRepository.findById(postId);
    }
}
