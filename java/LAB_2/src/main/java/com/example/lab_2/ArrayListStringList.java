package com.example.lab_2;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class ArrayListStringList implements StringList {
    private final List<String> list;

    public ArrayListStringList() {
        list = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void addFirst(String data) {
        synchronized (list) {
            list.add(0, data);
        }
    }

    @Override
    public void print() {
        synchronized (list) {
            System.out.println("Current list state:");
            for (String s : list) {
                System.out.println(s);
            }
            System.out.println("-----");
        }
    }

    @Override
    public Iterator<String> iterator() {
        synchronized (list) {
            return new ArrayList<>(list).iterator();
        }
    }

    public boolean bubbleSortStep(AtomicLong stepCounter) {
        boolean swapped = false;
        synchronized (list) {
            for (int i = 0; i < list.size() - 1; i++) {
                String current = list.get(i);
                String next = list.get(i + 1);
                if (current.compareTo(next) > 0) {
                    list.set(i, next);
                    list.set(i + 1, current);
                    swapped = true;
                    stepCounter.incrementAndGet();
                }
            }
        }
        return swapped;
    }
}
