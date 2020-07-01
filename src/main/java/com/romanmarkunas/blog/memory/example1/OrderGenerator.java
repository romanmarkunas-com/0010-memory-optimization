package com.romanmarkunas.blog.memory.example1;

import java.util.Random;

public class OrderGenerator {

    private final Random rn = new Random(42);
    private long id = 0;


    public Order next() {
        return new Order(
                Long.toString(id++),
                randomUser(),
                rn.nextInt(1000),
                rn.nextInt(10),
                rn.nextInt(10_000),
                ""
        );
    }


    private String randomUser() {
        return randomChar() + randomChar() + randomChar() + String.format("%-3s", rn.nextInt(1000));
    }

    private char randomChar() {
        return (char)('A' + rn.nextInt(26));
    }
}
