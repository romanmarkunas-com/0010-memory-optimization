package com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.immutablebytearray;

import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;

public class MutationScenarios {

    public void allowMutationOfMethodArgumentUsingHardcodedIndex(byte[] array) {
        array[0] = (byte) 1;
    }

//    public void failMutationOfAnnotatedMethodArgumentUsingHardcodedIndex(byte @ImmutableByteArray [] array) {
//        // :: error: (byte.array.mutation)
//        array[0] = (byte) 1;
//    }
//
//    public void failCompoundMutationOfPrimitiveArrayAsMethodArgumentUsingHardcodedIndex(byte @ImmutableByteArray [] array) {
//        // :: error: (byte.array.mutation)
//        array[0] += 1;
//    }
//
//    public void allowMutationOfMethodArgumentUsingIteration(byte[] array) {
//        for (int i = 0; i < array.length; i++) {
//            array[i] = (byte) 1;
//        }
//    }
//
//    public void failMutationOfMethodArgumentUsingIteration(byte @ImmutableByteArray [] array) {
//        for (int i = 0; i < array.length; i++) {
//            // :: error: (byte.array.mutation)
//            array[i] = (byte) 1;
//        }
//    }
//
//    public void failCompoundMutationOfMethodArgumentUsingIteration(byte @ImmutableByteArray [] array) {
//        for (int i = 0; i < array.length; i++) {
//            // :: error: (byte.array.mutation)
//            array[i] += 1;
//        }
//    }
//
//    public void shouldNotAllowAssignmentToUnannotatedVariable(byte @ImmutableByteArray [] array) {
//        // :: error: (assignment.type.incompatible)
//        byte[] temp = array;
//        temp[0] = (byte) 1;
//    }
//
//    public void shouldNotAllowMutationUsingArrayFill(byte @ImmutableByteArray [] array) {
//        // :: error: (assignment.type.incompatible)
//        Arrays.fill(array, (byte) 1);
//    }
}