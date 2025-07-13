package com.commentarium.controllers.comments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequest {
    private Long commentId;
    private String content; // Updated content of the comment
    private Long postId; // ID of the post to which the comment belongs
    private String likedBy;
}
