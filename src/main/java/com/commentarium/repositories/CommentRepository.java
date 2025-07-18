package com.commentarium.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.commentarium.entities.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = { "author" })
    List<Comment> findByPostId(Long postId, Pageable Pageable);

    @EntityGraph(attributePaths = { "author" })
    Optional<Comment> findByIdAndPostId(Long id, Long postId);

    boolean existsByIdAndPostId(Long id, Long postId);
}
