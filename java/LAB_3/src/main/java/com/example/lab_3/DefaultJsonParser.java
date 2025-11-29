package com.example.lab_3;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DefaultJsonParser implements JsonParser {
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"(.*?)\"");
    private static final Pattern SUCCESSORS_PATTERN = Pattern.compile("\"successors\"\\s*:\\s*\\[(.*?)]");
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(.*?)\"");

    @Override
    public String parseMessage(String body) {
        Matcher matcher = MESSAGE_PATTERN.matcher(body);
        return matcher.find() ? matcher.group(1) : "";
    }

    @Override
    public List<String> parseSuccessors(String body) {
        List<String> successors = new ArrayList<>();
        Matcher listMatcher = SUCCESSORS_PATTERN.matcher(body);
        if (listMatcher.find()) {
            String raw = listMatcher.group(1);
            Matcher itemMatcher = STRING_PATTERN.matcher(raw);
            while (itemMatcher.find()) {
                successors.add(itemMatcher.group(1));
            }
        }
        return successors;
    }
}
