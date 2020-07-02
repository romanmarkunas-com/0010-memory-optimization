package com.romanmarkunas.blog.memory.address;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    private final String number;
    private final String street;
    private final String city;
    private final String region;
    private final String postCode;

    @JsonCreator
    public Address(
            @JsonProperty("NUMBER") String number,
            @JsonProperty("STREET") String street,
            @JsonProperty("CITY") String city,
            @JsonProperty("REGION") String region,
            @JsonProperty("POSTCODE") String postCode
    ) {
        this.number = number;
        this.street = street;
        this.city = city;
        this.region = region;
        this.postCode = postCode;
    }

    public String getNumber() {
        return number;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostCode() {
        return postCode;
    }

    @Override
    public String toString() {
        return "Address{" +
                "number='" + number + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", region='" + region + '\'' +
                ", postCode='" + postCode + '\'' +
                '}';
    }
}
