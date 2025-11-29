package com.example.lab_3;

import java.util.Collections;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        HttpSpider spider = new HttpSpider(new DefaultHttpFetcher(), new DefaultJsonParser());
        List<String> messages = spider.collectAllMessages("/");
        Collections.sort(messages);
        System.out.println(messages.size());
        for (String message : messages) {
            System.out.println(message);
        }
    }
}
