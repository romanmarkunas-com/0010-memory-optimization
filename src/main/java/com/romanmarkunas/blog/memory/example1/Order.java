package com.romanmarkunas.blog.memory.example1;

public final class Order {

    private final String id;
    private final String user;
    private final Integer articleNr;
    private final int count;
    private final int price;
    private final String address;

    public Order(
            String id,
            String user,
            Integer articleNr,
            int count,
            int price,
            String address
    ) {
        this.id = id;
        this.user = user;
        this.articleNr = articleNr;
        this.count = count;
        this.price = price;
        this.address = address;
    }
}
