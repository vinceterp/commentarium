package com.commentarium.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentariumApiHelper<T> {
    private String message;
    private String status;
    private T data;
}
