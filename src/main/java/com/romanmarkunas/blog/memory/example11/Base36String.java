package com.romanmarkunas.blog.memory.example11;

import java.util.Arrays;

public class Base36String {

    private final byte[] value;


    public Base36String(byte[] value) {
        this.value = value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Base36String that = (Base36String) o;
        return Arrays.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return Base36Encoder.decode(value);
    }
}
