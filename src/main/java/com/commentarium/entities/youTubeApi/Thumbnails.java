package com.commentarium.entities.youTubeApi;

import lombok.Data;

@Data
public class Thumbnails {
    private ThumbnailInfo defaultThumbnail;
    private ThumbnailInfo medium;
    private ThumbnailInfo high;
    private ThumbnailInfo standard;
    private ThumbnailInfo maxres;
}
