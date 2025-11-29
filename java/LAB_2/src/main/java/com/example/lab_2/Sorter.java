package com.example.lab_2;

import java.util.concurrent.atomic.AtomicLong;

class Sorter implements Runnable {
    private final StringList list;
    private final AtomicLong stepCounter;
    private volatile boolean running = true;

    public Sorter(StringList list, AtomicLong stepCounter) {
        this.list = list;
        this.stepCounter = stepCounter;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
                if (list.bubbleSortStep(stepCounter)) {
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
