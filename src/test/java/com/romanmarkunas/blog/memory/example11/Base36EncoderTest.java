package com.romanmarkunas.blog.memory.example11;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class Base36EncoderTest {

    @Test
    void shouldCompactByteSize() {
        assertThat(Base36Encoder.encode("0")).hasSize(1);
        assertThat(Base36Encoder.encode("00")).hasSize(2);
        assertThat(Base36Encoder.encode("000")).hasSize(3);
        assertThat(Base36Encoder.encode("0000")).hasSize(3);
        assertThat(Base36Encoder.encode("00000")).hasSize(4);
        assertThat(Base36Encoder.encode("000000")).hasSize(5);
        assertThat(Base36Encoder.encode("0000000")).hasSize(6);
        assertThat(Base36Encoder.encode("00000000")).hasSize(6);
    }

    @Test
    void shouldEncodeSingleCharacterWithPadding() {
        assertThat(Base36Encoder.encode("1")).isEqualTo(new byte[]{0b00000100});
    }

    @Test
    void shouldEncodeTwoCharacterWithPadding() {
        assertThat(Base36Encoder.encode("12")).isEqualTo(new byte[]{0b00000100, 0b00100000});
    }

    @Test
    void shouldEncodeThreeCharactersWithPadding() {
        assertThat(Base36Encoder.encode("123")).isEqualTo(new byte[]{0b00000100, 0b00100000, (byte)0b11000000});
    }

    @Test
    void shouldEncodeFourCharactersWithoutPadding() {
        assertThat(Base36Encoder.encode("1234")).isEqualTo(new byte[]{0b00000100, 0b00100000, (byte)0b11000100});
    }

    @Test
    void shouldEncodeFiveCharactersWithPadding() {
        assertThat(Base36Encoder.encode("12345")).isEqualTo(new byte[]{0b00000100, 0b00100000, (byte)0b11000100, 0b00010100});
    }

    @Test
    void shouldDecodeSingleCharacterWithPadding() {
        assertThat(Base36Encoder.decode(new byte[]{0b00001000})).isEqualTo("2");
    }

    @Test
    void shouldDecodeTwoCharacterWithPadding() {
        assertThat(Base36Encoder.decode(new byte[]{0b00000100, 0b00100000})).isEqualTo("12");
    }

    @Test
    void shouldDecodeThreeCharactersWithPadding() {
        assertThat(Base36Encoder.decode(new byte[]{0b00000100, 0b00100000, (byte)0b11000000})).isEqualTo("123");
    }

    @Test
    @Disabled
    void shouldDecodeFourCharactersWithoutPadding() {
        assertThat(Base36Encoder.decode(new byte[]{0b00000100, 0b00100000, (byte)0b11000100})).isEqualTo("1234");
    }

    @Test
    @Disabled
    void shouldDecodeFiveCharactersWithPadding() {
        assertThat(Base36Encoder.decode(new byte[]{0b00000100, 0b00100000, (byte)0b11000100, 0b00010100})).isEqualTo("12345");
    }

    @ParameterizedTest
    @MethodSource("allUnsupportedCharsAsString")
    void shouldFailToEncodeUnsupportedChar(String s) {
        assertThatThrownBy(() -> Base36Encoder.encode(s))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("allUnsupportedByteValues")
    void shouldFailToDecodeUnsupportedByteValue(byte b) {
        assertThatThrownBy(() -> Base36Encoder.decode(new byte[]{(byte) (b << 2)}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("allSupportedCharsAsString")
    void shouldDecodeEncodedValue(String s) {
        byte[] encoded = Base36Encoder.encode(s);
        String decoded = Base36Encoder.decode(encoded);
        assertThat(decoded).isEqualTo(s);
    }

    @ParameterizedTest
    @MethodSource("allSupportedByteValues")
    void shouldEncodeDecodedValue(byte b) {
        byte shiftedByteValue = (byte) (b << 2);
        String decoded = Base36Encoder.decode(new byte[]{shiftedByteValue});
        byte[] encoded = Base36Encoder.encode(decoded);
        assertThat(encoded).hasSize(1);
        assertThat(encoded[0]).isEqualTo(shiftedByteValue);
    }

    static Stream<String> allSupportedCharsAsString() {
        return Stream.of(
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z"
        );
    }

    static Stream<String> allUnsupportedCharsAsString() {
        Set<String> supportedChars = allSupportedCharsAsString().collect(Collectors.toSet());
        return IntStream.range(0, 256)
                .mapToObj(i -> (char) i)
                .map(String::valueOf)
                .filter(s -> !supportedChars.contains(s));
    }

    static Stream<Byte> allSupportedByteValues() {
        return IntStream.range(0, 36).mapToObj(i -> (byte) i);
    }

    static Stream<Byte> allUnsupportedByteValues() {
        return IntStream.range(36, 64).mapToObj(i -> (byte) i);
    }
}