package com.example.lab_2;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    private static final int MAX_CHUNK_SIZE = 80;
    private static int NUM_THREADS = 2;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Threades: ");
        NUM_THREADS = scanner.nextInt();
        scanner.nextLine();
        AtomicLong stepCounterCustom = new AtomicLong(0);
        AtomicLong stepCounterArrayList = new AtomicLong(0);

        System.out.println("Running with CustomStringList...");
        StringList customList = new CustomStringList();
        runProgram(customList, stepCounterCustom, scanner);

        System.out.println("\nRunning with ArrayListStringList...");
        StringList arrayList = new ArrayListStringList();
        runProgram(arrayList, stepCounterArrayList, scanner);

        System.out.println("CustomStringList step count: " + stepCounterCustom.get());
        System.out.println("ArrayListStringList step count: " + stepCounterArrayList.get());

        scanner.close();
    }

    private static void runProgram(StringList list, AtomicLong stepCounter, Scanner scanner) {
        List<Thread> threads = new ArrayList<>();
        List<Sorter> sorters = new ArrayList<>();

        for (int i = 0; i < NUM_THREADS; i++) {
            Sorter sorter = new Sorter(list, stepCounter);
            Thread thread = new Thread(sorter);
            threads.add(thread);
            sorters.add(sorter);
            thread.start();
        }

        while (true) {
            System.out.print("Enter text: ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                list.print();
            } else {
                for (int i = 0; i < input.length(); i += MAX_CHUNK_SIZE) {
                    String chunk = input.substring(i, Math.min(i + MAX_CHUNK_SIZE, input.length()));
                    list.addFirst(chunk);
                }
            }
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
        }

        for (Sorter sorter : sorters) {
            sorter.stop();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
