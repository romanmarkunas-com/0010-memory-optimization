package com.romanmarkunas.blog.memory.example12;

import java.util.Arrays;
import java.util.NavigableSet;
import java.util.TreeSet;

public class TreeBasedByteArrayPool implements ByteArrayPool {

    private final NavigableSet<byte[]> values = new TreeSet<>(Arrays::compare);


    @Override
    public byte[] intern(byte[] in) {
        boolean newValue = values.add(in);
        if (newValue) {
            return in;
        }
        else {
            return values.floor(in);
        }
    }
}
