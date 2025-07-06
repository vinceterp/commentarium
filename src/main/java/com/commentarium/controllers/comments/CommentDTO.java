package com.commentarium.controllers.comments;

import java.util.List;

import com.commentarium.entities.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private User author;
    private String content;
    private List<CommentDTO> replies;
    private int likeCount;
    private String createdAt;
}
