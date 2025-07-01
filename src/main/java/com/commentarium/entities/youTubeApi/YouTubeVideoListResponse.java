package com.commentarium.entities.youTubeApi;

import lombok.Data;

import java.util.List;

@Data
public class YouTubeVideoListResponse {
    private String kind;
    private String etag;
    private List<YouTubeVideoItem> items;
    private PageInfo pageInfo;
}
