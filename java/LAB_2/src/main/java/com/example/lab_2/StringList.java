package com.example.lab_2;

import java.util.concurrent.atomic.AtomicLong;

interface StringList extends Iterable<String> {
    void addFirst(String data);
    void print();
    boolean bubbleSortStep(AtomicLong stepCounter);
}
