package com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.immutablebytearray;

import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AnnotationUsageScenarios {

    public void allowAnnotatingByteArrayTypes() {
        byte @ImmutableByteArray [] array;
        byte [][] @ImmutableByteArray [] nestedArray;
    }

    public void failAnnotatingOtherTypesAsVariables() {
        // :: error: (byte.array.misuse)
        @ImmutableByteArray byte primitive;
        // :: error: (byte.array.misuse)
        @ImmutableByteArray Object object;
        // :: error: (byte.array.misuse)
        @ImmutableByteArray String string;
        // :: error: (byte.array.misuse)
        @ImmutableByteArray byte[] arrayOfPrimitives;
        // :: error: (byte.array.misuse)
        @ImmutableByteArray Object[] arrayOfObjects;
        // :: error: (byte.array.misuse)
        @ImmutableByteArray String[] arrayOfStrings;
        // :: error: (byte.array.misuse)
        int @ImmutableByteArray [] primitiveArray;
        // :: error: (byte.array.misuse)
        Object @ImmutableByteArray [] objectArray;
        // :: error: (byte.array.misuse)
        String @ImmutableByteArray [] stringArray;
        // :: error: (byte.array.misuse)
        byte @ImmutableByteArray [][] wrongNestedArray;
        // :: error: (byte.array.misuse)
        @ImmutableByteArray byte[][] nestedArrayOfPrimitives;
        // :: error: (byte.array.misuse)
        int @Nullable @ImmutableByteArray [] intermixedWithOtherAnnotations;
    }

    public void failAnnotatingOtherTypesAsMethodArguments(
        // :: error: (byte.array.misuse)
        @ImmutableByteArray byte primitive,
        // :: error: (byte.array.misuse)
        @ImmutableByteArray Object object,
        // :: error: (byte.array.misuse)
        @ImmutableByteArray String string,
        // :: error: (byte.array.misuse)
        @ImmutableByteArray byte[] arrayOfPrimitives,
        // :: error: (byte.array.misuse)
        @ImmutableByteArray Object[] arrayOfObjects,
        // :: error: (byte.array.misuse)
        @ImmutableByteArray String[] arrayOfStrings,
        // :: error: (byte.array.misuse)
        int @ImmutableByteArray [] primitiveArray,
        // :: error: (byte.array.misuse)
        Object @ImmutableByteArray [] objectArray,
        // :: error: (byte.array.misuse)
        String @ImmutableByteArray [] stringArray,
        // :: error: (byte.array.misuse)
        byte @ImmutableByteArray [][] wrongNestedArray,
        // :: error: (byte.array.misuse)
        @ImmutableByteArray byte[][] nestedArrayOfPrimitives,
        // :: error: (byte.array.misuse)
        int @Nullable @ImmutableByteArray [] intermixedWithOtherAnnotations
    ) {}

    public static class FailAnnotatingOtherTypesAsClassFields {
        // :: error: (byte.array.misuse)
        private @ImmutableByteArray byte primitive;
        // :: error: (byte.array.misuse)
        private @ImmutableByteArray Object object;
        // :: error: (byte.array.misuse)
        private @ImmutableByteArray String string;
        // :: error: (byte.array.misuse)
        private @ImmutableByteArray byte[] arrayOfPrimitives;
        // :: error: (byte.array.misuse)
        private @ImmutableByteArray Object[] arrayOfObjects;
        // :: error: (byte.array.misuse)
        private @ImmutableByteArray String[] arrayOfStrings;
        // :: error: (byte.array.misuse)
        private int @ImmutableByteArray [] primitiveArray;
        // :: error: (byte.array.misuse)
        private Object @ImmutableByteArray [] objectArray;
        // :: error: (byte.array.misuse)
        private String @ImmutableByteArray [] stringArray;
        // :: error: (byte.array.misuse)
        private byte @ImmutableByteArray [][] wrongNestedArray;
        // :: error: (byte.array.misuse)
        private @ImmutableByteArray byte[][] nestedArrayOfPrimitives;
        // :: error: (byte.array.misuse)
        private int @Nullable @ImmutableByteArray [] intermixedWithOtherAnnotations;
    }
}