package com.commentarium.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commentarium.entities.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentIsNull(Long postId);
}
