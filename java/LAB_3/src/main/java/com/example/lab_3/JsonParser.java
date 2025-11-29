package com.example.lab_3;

import java.util.List;

public interface JsonParser {
    String parseMessage(String body);
    List<String> parseSuccessors(String body);
}
