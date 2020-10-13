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

    public void failReassignmentToUnannotatedVariable(byte @ImmutableByteArray [] array) {
        byte[] temp = new byte[10];
        // :: error: (byte.array.weakening)
        temp = array;
    }

    public void failInitialisationToUnannotatedVariable(byte @ImmutableByteArray [] array) {
        byte[] temp;
        if (true) {
            // :: error: (byte.array.weakening)
            temp = array;
        }
    }

    public byte[] failReturningAnnotatedAsUnannotated(byte @ImmutableByteArray [] array) {
        byte aByte = array[0];
        // :: error: (byte.array.weakening)
        return array;
    }

    public void allowStrengtheningAssignment(byte[] array) {
        byte @ImmutableByteArray [] temp = array;
    }

    public void allowStrengtheningReassignment(byte[] array) {
        byte @ImmutableByteArray [] temp;
        temp = array;
    }

    public void allowStrengtheningMethodInvocation(byte[] array) {
        someMethod(array);
    }

    private void someMethod(byte @ImmutableByteArray [] array) {

    }

    public byte @ImmutableByteArray [] allowStrengtheningReturn(byte[] array) {
        return array;
    }

    public void allowAssignmentToUnannotatedVariableIfSuppressed(byte @ImmutableByteArray [] array) {
        @SuppressWarnings("byte.array.weakening")
        byte[] temp = array;
    }
}