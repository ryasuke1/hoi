package com.example.lab_2;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomStringList implements StringList {
    private Node head;
    private final Lock listLock = new ReentrantLock();

    @Override
    public void addFirst(String data) {
        Node newNode = new Node(data);
        listLock.lock();
        try {
            newNode.next = head;
            head = newNode;
        } finally {
            listLock.unlock();
        }
    }

    @Override
    public void print() {
        listLock.lock();
        try {
            System.out.println("Current list state:");
            for (String s : this) {
                System.out.println(s);
            }
            System.out.println("-----");
        } finally {
            listLock.unlock();
        }
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            private Node current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public String next() {
                if (!hasNext()) throw new NoSuchElementException();
                String data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    public boolean bubbleSortStep(AtomicLong stepCounter) {
        if (head == null || head.next == null) return false;

        boolean swapped = false;
        listLock.lock();
        try {
            Node prev = null;
            Node current = head;
            current.lock.lock();
            Node next = current.next;

            while (next != null) {
                next.lock.lock();
                try {
                    if (current.data.compareTo(next.data) > 0) {
                        if (prev == null) {
                            head = next;
                        } else {
                            prev.next = next;
                        }
                        current.next = next.next;
                        next.next = current;
                        Node temp = current;
                        current = next;
                        next = temp;
                        swapped = true;
                        stepCounter.incrementAndGet();
                    }
                    if (prev != null) {
                        prev.lock.unlock();
                    }
                    prev = current;
                    current = next;
                    next = current.next;
                    if (next != null) {
                        next.lock.lock();
                    }
                } finally {
                    if (next != null) {
                        next.lock.unlock();
                    }
                }
            }
            current.lock.unlock();
            if (prev != null) {
                prev.lock.unlock();
            }
        } finally {
            listLock.unlock();
        }
        return swapped;
    }
}
