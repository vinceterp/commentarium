package com.commentarium.controllers.comments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCommentRequest {
    private Long postId;
    private Long commentId; // ID of the comment to be deleted
}
