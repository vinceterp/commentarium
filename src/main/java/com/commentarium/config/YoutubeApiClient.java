package com.commentarium.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.commentarium.entities.youTubeApi.YouTubeVideoListResponse;

@Service
public class YoutubeApiClient {
    @Value("${spring.application.youtube.api.key}")
    private String youtubeApiKey;
    @Value("${spring.application.youtube.api.url}")
    private String youtubeApiUrl;
    @Value("${spring.application.youtube.api.part}")
    private String youtubeApiPart;

    private final WebClient webClient;

    public YoutubeApiClient(WebClient.Builder youtubeApiClientBuilder) {
        this.webClient = youtubeApiClientBuilder
                .baseUrl(youtubeApiUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public YouTubeVideoListResponse getVideoDetails(String originalUrl) {
        String videoId = getVideoIdFromUrl(originalUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL: " + originalUrl);
        }

        String youtubeQueryUrl = String.format(
                "%s?part=%s&id=%s&key=%s",
                youtubeApiUrl,
                youtubeApiPart,
                videoId,
                youtubeApiKey);

        return webClient.get()
                .uri(youtubeQueryUrl)
                .retrieve()
                .bodyToMono(YouTubeVideoListResponse.class)
                .block();
    }

    public String getVideoIdFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("v")) {
                        return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing YouTube URL: " + e.getMessage());
        }
        return null;
    }
}
