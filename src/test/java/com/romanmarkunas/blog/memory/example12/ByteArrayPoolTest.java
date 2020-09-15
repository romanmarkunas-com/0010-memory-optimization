package com.romanmarkunas.blog.memory.example12;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;

class ByteArrayPoolTest {

    static Stream<ByteArrayPool> implementations() {
        return Stream.of(
                new SlowByteArrayPool(),
                new TreeBasedByteArrayPool()
        );
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldReturnSameObjectForEmptyPool(ByteArrayPool pool) {
        byte[] in = new byte[]{0, 1, 5};
        byte[] out = pool.intern(in);

        assertSame(in, out);
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldReturnSameObjectForNonEmptyPoolButNoMatch(ByteArrayPool pool) {
        byte[] filler1 = new byte[]{0, 1, 5};
        pool.intern(filler1);
        byte[] filler2 = new byte[]{0, 2, 5, 1};
        pool.intern(filler2);
        byte[] filler3 = new byte[]{0, 2};
        pool.intern(filler3);

        byte[] in = new byte[]{0, 2, 5};
        byte[] out = pool.intern(in);

        assertSame(in, out);
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldReturnPooledObjectForPoolWithMatch(ByteArrayPool pool) {
        byte[] filler1 = new byte[]{0, 1, 4};
        pool.intern(filler1);
        byte[] existing = new byte[]{0, 1, 5};
        pool.intern(existing);
        byte[] filler2 = new byte[]{0, 1, 6};
        pool.intern(filler2);

        byte[] in = new byte[]{0, 1, 5};
        byte[] out = pool.intern(in);

        assertSame(existing, out);
    }
}