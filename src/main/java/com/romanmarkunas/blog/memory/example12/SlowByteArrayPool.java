package com.romanmarkunas.blog.memory.example12;

import java.util.Arrays;

public class SlowByteArrayPool implements ByteArrayPool {

    private byte[][] vals = new byte[1][];
    private int size = 0;


    @Override
    public byte[] intern(byte[] in) {
        for (byte[] val : vals) {
            if (Arrays.equals(val, in)) {
                return val;
            }
        }

        if (size >= vals.length) {
            vals = Arrays.copyOf(vals, vals.length << 1);
        }

        vals[size] = in;
        size++;
        return in;
    }
}
