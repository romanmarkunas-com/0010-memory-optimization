package com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.immutablebytearray;

import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

public class MutationScenarios {

    public void allowMutationOfMethodArgumentUsingHardcodedIndex(byte[] array) {
        array[0] = (byte) 1;
    }

    public void failMutationOfAnnotatedMethodArgumentUsingHardcodedIndex(byte @ImmutableByteArray [] array) {
        // :: error: (byte.array.mutation)
        array[0] = (byte) 1;
    }

    public void failCompoundMutationOfPrimitiveArrayAsMethodArgumentUsingHardcodedIndex(byte @ImmutableByteArray [] array) {
        // :: error: (byte.array.mutation)
        array[0] += 1;
    }

    public void failMutationOfAnnotatedMethodArgumentUsingNestedHardcodedIndex(byte [] @ImmutableByteArray [] array) {
        // :: error: (byte.array.mutation)
        array[0][0] = (byte) 1;
    }

    public void failCompoundMutationOfAnnotatedMethodArgumentUsingNestedHardcodedIndex(byte [] @ImmutableByteArray [] array) {
        // :: error: (byte.array.mutation)
        array[0][0] += 1;
    }

    public void allowMutationOfMethodArgumentUsingIteration(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) 1;
        }
    }

    public void failMutationOfMethodArgumentUsingIteration(byte @ImmutableByteArray [] array) {
        for (int i = 0; i < array.length; i++) {
            // :: error: (byte.array.mutation)
            array[i] = (byte) 1;
        }
    }

    public void failCompoundMutationOfMethodArgumentUsingIteration(byte @ImmutableByteArray [] array) {
        for (int i = 0; i < array.length; i++) {
            // :: error: (byte.array.mutation)
            array[i] += 1;
        }
    }
}