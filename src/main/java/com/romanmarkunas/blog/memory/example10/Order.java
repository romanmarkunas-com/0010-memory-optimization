package com.romanmarkunas.blog.memory.example10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Order {

    private final long id;
    private final String user;
    private final int articleNr;
    private final int count;
    private final int pricePence;
    private final String addressNumber;
    private final String addressStreet;
    private final String addressCity;
    private final String addressRegion;
    private final String addressPostCode;


    @JsonCreator
    public Order(
            @JsonProperty("id") long id,
            @JsonProperty("user") String user,
            @JsonProperty("articleNr") int articleNr,
            @JsonProperty("count") int count,
            @JsonProperty("pricePence") int pricePence,
            @JsonProperty("addressNumber") String addressNumber,
            @JsonProperty("addressStreet") String addressStreet,
            @JsonProperty("addressCity") String addressCity,
            @JsonProperty("addressRegion") String addressRegion,
            @JsonProperty("addressPostCode") String addressPostCode) {
        this.id = id;
        this.user = user;
        this.articleNr = articleNr;
        this.count = count;
        this.pricePence = pricePence;
        this.addressNumber = addressNumber.intern();
        this.addressStreet = addressStreet.intern();
        this.addressCity = addressCity.intern();
        this.addressRegion = addressRegion.intern();
        this.addressPostCode = addressPostCode.intern();
    }


    public long getId() {
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

    @JsonIgnore
    public String getAddress() {
        return addressNumber + " "
                + addressStreet + ", "
                + addressCity + ", "
                + addressRegion + ", "
                + addressPostCode;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public String getAddressRegion() {
        return addressRegion;
    }

    public String getAddressPostCode() {
        return addressPostCode;
    }
}
