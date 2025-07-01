package com.commentarium.entities.youTubeApi;

import lombok.Data;

@Data
public class Snippet {
    private String publishedAt;
    private String channelId;
    private String title;
    private String description;
    private Thumbnails thumbnails;
    private String channelTitle;
    private String categoryId;
    private String liveBroadcastContent;
    private Localized localized;
    private String defaultAudioLanguage;
}
