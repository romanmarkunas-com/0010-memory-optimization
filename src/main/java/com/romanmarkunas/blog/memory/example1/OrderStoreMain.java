package com.romanmarkunas.blog.memory.example1;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class OrderStoreMain {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (scanner.hasNextLine()) {
                    String received = scanner.nextLine();
                    System.out.println("echoing: " + received);
                    System.out.flush();
                }
                else {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(5));
                }
            }
        }
    }
}
