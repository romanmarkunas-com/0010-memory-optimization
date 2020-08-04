package com.romanmarkunas.blog.memory.example11;

import java.nio.charset.StandardCharsets;

/**
 * 0-9 corresponds to characters 0-9 (ASCII 48-57)
 * 10-35 corresponds to characters A-Z (ASCII 65-90)
 * 36 is a special padding value
 */
public class Base36Encoder {

    private static final byte[] SUPPORTED_CHARS;
    private static final byte THIRD_BYTE_PADDING = (byte) 36;
    static {
        SUPPORTED_CHARS = new byte[36];
        for (int i = 0; i < 36; i++) {
            SUPPORTED_CHARS[i] = (byte) encodedByteToChar(i);
        }
    }


    public static byte[] encode(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        byte[] ret = new byte[calculateOutSize(bytes.length)];

        int length = str.length();
        int encoded = 0;
        int moreToEncode = length;
        while (moreToEncode > 0) {
            int encodeThisPass = Math.min(4, moreToEncode);
            encoded += encodeUpTo4Chars(length - moreToEncode, encodeThisPass, str, encoded, ret);
            moreToEncode -= encodeThisPass;
        }

        return ret;
    }

    public static String decode(byte[] bytes) {
        StringBuilder out = new StringBuilder();

        int length = bytes.length;
        int decoded = 0;
        int moreToDecode = length;
        while (moreToDecode > 0) {
            int decodeThisPass = Math.min(3, moreToDecode);
            decodeUpTo3Bytes(decoded, decodeThisPass, bytes, out);
            decoded += decodeThisPass;
            moreToDecode -= decodeThisPass;
        }

        return out.toString();
    }


    private static int calculateOutSize(int length) {
        int quotient = length * 6 / 8;
        int remainder = length * 6 % 8;
        return remainder == 0 ? quotient : quotient + 1;
    }

    private static int encodeUpTo4Chars(int inOffset, int inLength, String in, int outOffset, byte[] out) {
        int encodedByteCount = 0;

        for (int i = 0; i < inLength; i++) {
            byte encoded = charToEncodedByte(in.charAt(inOffset + i));
            if (i == 0) {
                out[outOffset] |= (encoded << 2);
                encodedByteCount = 1;
            }
            else if (i == 1) {
                out[outOffset] |= ((encoded >> 6) & 0x0003);
                out[outOffset + 1] |= (encoded << 4);
                encodedByteCount = 2;
            }
            else if (i == 2) {
                out[outOffset + 1] |= ((encoded >> 4) & 0x0007);
                out[outOffset + 2] |= (encoded << 6);
                if (inLength == 3) {
                    out[outOffset + 2] |= (THIRD_BYTE_PADDING & 0x003F);
                    encodedByteCount = 3;
                }
            }
            else if (i == 3) {
                out[outOffset + 2] |= (encoded & 0x003F);
                encodedByteCount = 3;
            }
            else {
                throw new IllegalStateException("Should encode up to 4 chars at once!");
            }
        }

        return encodedByteCount;
    }

    private static void decodeUpTo3Bytes(int inOffset, int inLength, byte[] in, StringBuilder out) {
        for (int i = 0; i < inLength; i++) {
            if (i == 0) {
                int single = (in[inOffset] >> 2) & 0x003F;
                out.append(encodedByteToChar(single));
            }
            else if (i == 1) {
                int left = (in[inOffset] << 4) & 0x0030;
                int right = (in[inOffset + 1] >> 4) & 0x000F;
                out.append(encodedByteToChar(left | right));
            }
            else if (i == 2) {
                int left = (in[inOffset + 1] << 2) & 0x003C;
                int right = (in[inOffset + 2] >> 6) & 0x0003;
                out.append(encodedByteToChar(left | right));

                int last6Bytes = in[inOffset + 2] & 0x003F;
                if (last6Bytes != THIRD_BYTE_PADDING) {
                    out.append(encodedByteToChar(last6Bytes));
                }
            }
            else {
                throw new IllegalStateException("Should encode up to 4 chars at once!");
            }
        }
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
