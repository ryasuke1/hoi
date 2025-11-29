package com.example.lab_2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node {
    String data;
    Node next;
    final Lock lock = new ReentrantLock();

    Node(String data) {
        this.data = data;
        this.next = null;
    }
}
