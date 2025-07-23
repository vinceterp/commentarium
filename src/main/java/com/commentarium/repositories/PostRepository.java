package com.commentarium.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.commentarium.entities.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = { "author" })
    Optional<Post> findOneByOriginalUrl(String originalUrl);

    @EntityGraph(attributePaths = { "author" })
    Optional<Post> findById(Long id);

}
