package com.romanmarkunas.blog.memory.example14;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

import java.nio.charset.StandardCharsets;

public final class Order {

    private final long id;
    private final int user;
    private final int articleNr;
    private final int count;
    private final int pricePence;
    private final int addressNumber;
    private final int addressStreet;
    private final int addressCity;
    private final int addressRegion;
    private final int addressPostCode;


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
            @JacksonInject PooledByteArrayMap byteArrayPool
    ) {
        this.id = id;
        this.user = byteArrayPool.put(user);
        this.articleNr = articleNr;
        this.count = count;
        this.pricePence = pricePence;
        this.addressNumber = internString(addressNumber, byteArrayPool);
        this.addressStreet = internString(addressStreet, byteArrayPool);
        this.addressCity = internString(addressCity, byteArrayPool);
        this.addressRegion = internString(addressRegion, byteArrayPool);
        this.addressPostCode = internString(addressPostCode, byteArrayPool);
    }


    public long getId() {
        return id;
    }

    public byte @ImmutableByteArray [] getUser(PooledByteArrayMap byteArrayPool) {
        return byteArrayPool.get(user);
    }

    public long getUserPoolKey() {
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

    @JsonIgnore
    public String getAddress(PooledByteArrayMap byteArrayPool) {
        return getAddressNumber(byteArrayPool) + " "
                + getAddressStreet(byteArrayPool) + ", "
                + getAddressCity(byteArrayPool) + ", "
                + getAddressRegion(byteArrayPool) + ", "
                + getAddressPostCode(byteArrayPool);
    }

    public String getAddressNumber(PooledByteArrayMap byteArrayPool) {
        return restoreString(addressNumber, byteArrayPool);
    }

    public String getAddressStreet(PooledByteArrayMap byteArrayPool) {
        return restoreString(addressStreet, byteArrayPool);
    }

    public String getAddressCity(PooledByteArrayMap byteArrayPool) {
        return restoreString(addressCity, byteArrayPool);
    }

    public String getAddressRegion(PooledByteArrayMap byteArrayPool) {
        return restoreString(addressRegion, byteArrayPool);
    }

    public String getAddressPostCode(PooledByteArrayMap byteArrayPool) {
        return restoreString(addressPostCode, byteArrayPool);
    }

    private int internString(String str, PooledByteArrayMap byteArrayPool) {
        return byteArrayPool.put(str.getBytes(StandardCharsets.US_ASCII));
    }

    @SuppressWarnings("byte.array.weakening")
    private String restoreString(int key, PooledByteArrayMap byteArrayPool) {
        return new String(byteArrayPool.get(key), StandardCharsets.US_ASCII);
    }
}
