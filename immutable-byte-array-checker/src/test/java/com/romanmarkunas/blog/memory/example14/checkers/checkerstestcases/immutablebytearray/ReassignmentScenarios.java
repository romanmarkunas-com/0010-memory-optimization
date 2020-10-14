package com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.immutablebytearray;

import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

import java.nio.charset.StandardCharsets;
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

    public void failMutationUsingArrayFill(byte @ImmutableByteArray [] array) {
        // :: error: (byte.array.weakening)
        Arrays.fill(array, (byte) 1);
    }

    public void failMutationUsingArrayFillForValueReturnedByMethod() {
        // :: error: (byte.array.weakening)
        Arrays.fill(returnArray(), (byte) 1);
    }

    private byte @ImmutableByteArray [] returnArray() {
        return new byte @ImmutableByteArray [10];
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

    public void allowStrengtheningViaConstructor(byte[] array) {
        new SomeClass(array);
    }

    private void someMethod(byte @ImmutableByteArray [] array) {

    }

    private static class SomeClass {
        public SomeClass(byte @ImmutableByteArray [] array) {
        }
    }

    public byte @ImmutableByteArray [] allowStrengtheningReturn(byte[] array) {
        return array;
    }

    public void allowAssignmentToUnannotatedVariableIfSuppressed(byte @ImmutableByteArray [] array) {
        @SuppressWarnings("byte.array.weakening")
        byte[] temp = array;
    }

    public void handleVoidReturns(byte @ImmutableByteArray [] array) {
        if(true) {
            return;
        }
        else {
            throw new RuntimeException("hello");
        }
    }

    public void handleFillWithNulls(byte [][] array) {
        Arrays.fill(array, null);
    }

    public void failMutationViaConstructor(byte @ImmutableByteArray [] array) {
        // :: error: (byte.array.weakening)
        new String(array);
    }

    public void failMutationViaConstructorFromMethodReturnValue(ByteArraySupplier supplier) {
        // :: error: (byte.array.weakening)
        new String(someMethod(), StandardCharsets.US_ASCII);
        // :: error: (byte.array.weakening)
        new String(supplier.get(5));
    }

    private byte @ImmutableByteArray [] someMethod() {
        return new byte @ImmutableByteArray [10];
    }
}