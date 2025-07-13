package com.commentarium.repositories;

import com.commentarium.entities.Post;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = { "author" })
    Optional<Post> findOneByOriginalUrl(String originalUrl);
}
