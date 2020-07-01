package com.romanmarkunas.blog.memory.example1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Order {

    private final String id;
    private final String user;
    private final Integer articleNr;
    private final int count;
    private final int pricePence;
    private final String address;


    @JsonCreator
    public Order(
            @JsonProperty("id") String id,
            @JsonProperty("user") String user,
            @JsonProperty("articleNr") Integer articleNr,
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


    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public Integer getArticleNr() {
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
