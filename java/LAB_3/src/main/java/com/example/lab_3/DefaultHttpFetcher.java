package com.example.lab_3;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DefaultHttpFetcher implements HttpFetcher {

    private final HttpClient client;

    public DefaultHttpFetcher() {
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    @Override
    public String fetch(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.statusCode());
            }
            return response.body();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to fetch " + path, e);
        }
    }
}
