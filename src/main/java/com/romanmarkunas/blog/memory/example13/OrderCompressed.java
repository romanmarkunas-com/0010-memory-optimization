package com.romanmarkunas.blog.memory.example13;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

public class OrderCompressed {

    private final byte[] compressedPart;
    private final String addressNumber;
    private final String addressStreet;
    private final String addressCity;
    private final String addressRegion;
    private final String addressPostCode;


    public OrderCompressed(
            byte[] compressedPart,
            String addressNumber,
            String addressStreet,
            String addressCity,
            String addressRegion,
            String addressPostCode
    ) {
        this.compressedPart = compressedPart;
        this.addressNumber = addressNumber;
        this.addressStreet = addressStreet;
        this.addressCity = addressCity;
        this.addressRegion = addressRegion;
        this.addressPostCode = addressPostCode;
    }

    public static OrderCompressed compress(Order order) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (
                GZIPOutputStream gzipOut = new GZIPOutputStream(out);
                ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
        ) {
            objectOut.writeObject(order);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to serialize " + order, e);
        }
        byte[] bytes = out.toByteArray();
        return new OrderCompressed(
                bytes,
                order.getAddressNumber(),
                order.getAddressStreet(),
                order.getAddressCity(),
                order.getAddressRegion(),
                order.getAddressPostCode()
        );
    }


    public byte[] getCompressedPart() {
        return compressedPart;
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
