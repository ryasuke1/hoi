package com.example.lab_3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class HttpSpider {
    private final HttpFetcher fetcher;
    private final JsonParser parser;
    private final Set<String> visited;

    public HttpSpider(HttpFetcher fetcher, JsonParser parser) {
        this.fetcher = fetcher;
        this.parser = parser;
        this.visited = ConcurrentHashMap.newKeySet();
    }

    public List<String> collectAllMessages(String startPath) {
        if (!visited.add(startPath)) {
            return List.of();
        }
        String body = fetcher.fetch(startPath);
        String message = parser.parseMessage(body);
        List<String> messages = new ArrayList<>();
        if (!message.isEmpty()) {
            messages.add(message);
        }
        List<String> successors = parser.parseSuccessors(body);
        if (successors.isEmpty()) {
            return messages;
        }
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = successors.stream()
                .filter(s -> !visited.contains("/" + s))
                .map(s -> executor.submit(() -> collectAllMessages("/" + s)))
                .toList();
            List<String> allSubMessages = new ArrayList<>();
            for (var future : futures) {
                try {
                    allSubMessages.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
            messages.addAll(allSubMessages);
        }
        return messages;
    }
}
