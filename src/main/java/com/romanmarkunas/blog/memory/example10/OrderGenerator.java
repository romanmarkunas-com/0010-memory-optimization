package com.romanmarkunas.blog.memory.example10;

import com.romanmarkunas.blog.memory.address.Address;

import java.util.List;
import java.util.Random;

public class OrderGenerator {

    private final Random rn = new Random(42);
    private final List<Address> addresses;
    private long id = 0;


    public OrderGenerator(List<Address> addresses) {
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("Should have at least 1 address");
        }
        this.addresses = addresses;
    }


    public Order next() {
        Address address;
        return new Order(
                id++,
                randomUser(),
                rn.nextInt(1000),
                rn.nextInt(10),
                rn.nextInt(10_000),
                (address = addresses.get(rn.nextInt(addresses.size()))).getNumber(),
                address.getStreet(),
                address.getCity(),
                address.getRegion(),
                address.getPostCode()
        );
    }


    private String randomUser() {
        return randomChar() + randomChar() + randomChar() + String.format("%03d", rn.nextInt(1000));
    }

    private char randomChar() {
        return (char)('A' + rn.nextInt(26));
    }
}
