package com.romanmarkunas.blog.memory.example9;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Order {

    private final long id;
    private final String user;
    private final int articleNr;
    private final int count;
    private final int pricePence;
    private final String address;


    @JsonCreator
    public Order(
            @JsonProperty("id") long id,
            @JsonProperty("user") String user,
            @JsonProperty("articleNr") int articleNr,
            @JsonProperty("count") int count,
            @JsonProperty("pricePence") int pricePence,
            @JsonProperty("address") String address
    ) {
        this.id = id;
        this.user = user;
        this.articleNr = articleNr;
        this.count = count;
        this.pricePence = pricePence;
        this.address = address;
    }


    public long getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public int getArticleNr() {
        return articleNr;
    }

    public int getCount() {
        return count;
    }

    public int getPricePence() {
        return pricePence;
    }

    public String getAddress() {
        return address;
    }
}
