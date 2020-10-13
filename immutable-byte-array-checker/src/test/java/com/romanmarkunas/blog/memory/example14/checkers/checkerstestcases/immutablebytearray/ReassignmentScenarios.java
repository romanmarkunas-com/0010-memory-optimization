package com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.immutablebytearray;

import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

import java.util.Arrays;

public class ReassignmentScenarios {

    public void allowAssignmentToAnnotatedVariable(byte @ImmutableByteArray [] array) {
        byte @ImmutableByteArray [] temp = array;
    }

    public void allowAssignmentToUnannotatedVariable(byte[] array) {
        byte[] temp = array;
        temp[0] = (byte) 1;
    }

    public void failAssignmentToUnannotatedVariable(byte @ImmutableByteArray [] array) {
        // :: error: (byte.array.weakening)
        byte[] temp = array;
        // :: error: (byte.array.mutation)
        temp[0] = (byte) 1;
    }

    public void allowAssignmentToNestedAnnotatedVariable(byte [] @ImmutableByteArray [] array) {
        byte [] @ImmutableByteArray [] temp = array;
    }

    public void allowAssignmentToNestedUnannotatedVariable(byte[][] array) {
        byte[][] temp = array;
        temp[0][0] = (byte) 1;
    }

    public void failAssignmentToNestedUnannotatedVariable(byte [] @ImmutableByteArray [] array) {
        // :: error: (byte.array.weakening)
        byte[][] temp = array;
        temp[0][0] = (byte) 1;
    }

    public void shouldNotAllowMutationUsingArrayFill(byte @ImmutableByteArray [] array) {
        // :: error: (byte.array.weakening)
        Arrays.fill(array, (byte) 1);
    }

    // reassignment to separately declared variable should be forbidden as well
}