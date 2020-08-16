package com.romanmarkunas.blog.memory.example12;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.charset.StandardCharsets;

public final class Order {

    private final long id;
    private final byte[] user;
    private final int articleNr;
    private final int count;
    private final int pricePence;
    private final byte[] addressNumber;
    private final byte[] addressStreet;
    private final byte[] addressCity;
    private final byte[] addressRegion;
    private final byte[] addressPostCode;


    @JsonCreator
    public Order(
            @JsonProperty("id") long id,
            @JsonProperty("user") byte[] user,
            @JsonProperty("articleNr") int articleNr,
            @JsonProperty("count") int count,
            @JsonProperty("pricePence") int pricePence,
            @JsonProperty("addressNumber") String addressNumber,
            @JsonProperty("addressStreet") String addressStreet,
            @JsonProperty("addressCity") String addressCity,
            @JsonProperty("addressRegion") String addressRegion,
            @JsonProperty("addressPostCode") String addressPostCode,
            @JacksonInject ByteArrayPool byteArrayPool
    ) {
        this.id = id;
        this.user = user;
        this.articleNr = articleNr;
        this.count = count;
        this.pricePence = pricePence;
        this.addressNumber = byteArrayPool.intern(addressNumber.getBytes(StandardCharsets.US_ASCII));
        this.addressStreet = byteArrayPool.intern(addressStreet.getBytes(StandardCharsets.US_ASCII));
        this.addressCity = byteArrayPool.intern(addressCity.getBytes(StandardCharsets.US_ASCII));
        this.addressRegion = byteArrayPool.intern(addressRegion.getBytes(StandardCharsets.US_ASCII));
        this.addressPostCode = byteArrayPool.intern(addressPostCode.getBytes(StandardCharsets.US_ASCII));
    }


    public long getId() {
        return id;
    }

    public byte[] getUser() {
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
        return getAddressNumber() + " "
                + getAddressStreet() + ", "
                + getAddressCity() + ", "
                + getAddressRegion() + ", "
                + getAddressPostCode();
    }

    public String getAddressNumber() {
        return new String(addressNumber);
    }

    public String getAddressStreet() {
        return new String(addressStreet);
    }

    public String getAddressCity() {
        return new String(addressCity);
    }

    public String getAddressRegion() {
        return new String(addressRegion);
    }

    public String getAddressPostCode() {
        return new String(addressPostCode);
    }
}
