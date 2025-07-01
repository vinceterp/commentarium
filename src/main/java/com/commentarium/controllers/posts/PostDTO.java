package com.commentarium.controllers.posts;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

import com.commentarium.controllers.comments.CommentDTO;
import com.commentarium.entities.User;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private User author;
    private List<CommentDTO> comments;
    private String originalUrl;
    private Date createdAt;
    private String title;
}
