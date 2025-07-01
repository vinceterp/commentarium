package com.commentarium.entities.youTubeApi;

import lombok.Data;

@Data
public class YouTubeVideoItem {
    private String kind;
    private String etag;
    private String id;
    private Snippet snippet;
}
