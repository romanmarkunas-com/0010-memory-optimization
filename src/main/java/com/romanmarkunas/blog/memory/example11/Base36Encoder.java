package com.romanmarkunas.blog.memory.example11;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 0-9 corresponds to characters 0-9 (ASCII 48-57)
 * 10-35 corresponds to characters A-Z (ASCII 65-90)
 */
public class Base36Encoder {

    private static final byte[] SUPPORTED_CHARS;
    static {
        SUPPORTED_CHARS = new byte[36];
//        for (int i = 0; i < 36; i++) {
//            SUPPORTED_CHARS[i] = (byte) encodedByteToChar(i);
//        }
        int digitCount = 10;
        for (int i = 0; i < digitCount; i++) {
            SUPPORTED_CHARS[i] = (byte) ('0' + i);
        }
        for (int i = 0; i < 26; i++) {
            SUPPORTED_CHARS[i + digitCount] = (byte) ('A' + i);
        }
    }


    public static byte[] encode(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        byte[] ret = new byte[calculateOutSize(bytes.length)];
        int encoded = add(charToEncodedByte(str.charAt(0)), 0);
        if (str.length() > 1) {
            encoded = add(charToEncodedByte(str.charAt(1)), encoded);
            encoded = encoded << 4;
        }
        else {
            encoded = encoded << 2;
        }
        if (str.length() > 1) {
            ret[0] = (byte) ((encoded >> 8) & 0x00FF);
            ret[1] = (byte) (encoded & 0x00FF);
        }
        else {
            ret[0] = (byte) ((encoded) & 0x00FF);
        }
        return ret;
    }

    private static int add(int encoded, int accumulator) {
        accumulator = accumulator << 6;
        accumulator = accumulator | (encoded & 0x3F);
        return accumulator;
    }

    public static String decode(byte[] bytes) {
        int single = (bytes[0] >> 2) & 0x3F;
        char decoded = encodedByteToChar(single);
        return "" + decoded;
    }


    private static int calculateOutSize(int length) {
        int quotient = length * 6 / 8;
        int remainder = length * 6 % 8;
        return remainder == 0 ? quotient : quotient + 1;
    }

    private static byte charToEncodedByte(char c) {
        for (int i = 0; i < SUPPORTED_CHARS.length; i++) {
            if ((char) SUPPORTED_CHARS[i] == c) {
                return (byte) i;
            }
        }
        throw new IllegalArgumentException("Unsupported character: " + c);
    }

    private static char encodedByteToChar(int single) {
        if (single >= 0 && single < 10) {
            return (char) ('0' + single);
        }
        else if (single >= 10 && single < 36) {
            return (char) ('A' + single - 10);
        }
        else {
            throw new IllegalArgumentException("Unsupported byte value: " + single);
        }
    }
}
