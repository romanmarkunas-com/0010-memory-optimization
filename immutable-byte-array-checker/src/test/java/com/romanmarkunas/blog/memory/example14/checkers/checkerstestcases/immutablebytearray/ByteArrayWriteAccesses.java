package com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.immutablebytearray;

import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

public class ByteArrayWriteAccesses {

    public void failMutationOfAnnotatedMethodArgumentUsingHardcodedIndex(byte @ImmutableByteArray [] array) {
        // :: error: (byte.array.mutation)
        array[0] = (byte) 1;
    }

    public void shouldAllowMutationOfMethodArgumentUsingHardcodedIndex(byte[] array) {
        array[0] = (byte) 1;
    }

//    public void shouldNotAllowMutationOfPrimitiveArrayAsMethodArgumentUsingHardcodedIndex(int @ImmutableByteArray [] array) {
//        // :: error: (byte.array.mutation)
//        array[0] += 1;
//    }

//    public void shouldNotAllowMutationOfObjectArrayAsMethodArgumentUsingHardcodedIndex(@ImmutableByteArray int[][] array) {
//        // :: error: (assignment.type.incompatible)
//        array[0] = null;
//    }

//    public void shouldNotAllowAnnotatingNonByteArrayTypes() {
//        // :: error: (assignment.type.incompatible)
//        @ImmutableByteArray byte[] array;
//    }
//
//    public void shouldNotAllowMutationOfMethodArgumentUsingIteration(@ImmutableByteArray byte[] array) {
//        for (int i = 0; i < array.length; i++) {
//            // :: error: (assignment.type.incompatible)
//            array[i] = (byte) 1;
//        }
//    }
//
//    public void shouldAllowMutationOfMethodArgumentUsingIteration(byte[] array) {
//        for (int i = 0; i < array.length; i++) {
//            array[i] = (byte) 1;
//        }
//    }
//
//    public void shouldNotAllowAssignmentToUnannotatedVariable(@ImmutableByteArray byte[] array) {
//        // :: error: (assignment.type.incompatible)
//        byte[] temp = array;
//        temp[0] = (byte) 1;
//    }

//    public void shouldNotAllowMutationUsingArrayFill(@ImmutableByteArray byte[] array) {
//        // :: error: (assignment.type.incompatible)
//        Arrays.fill(array, (byte) 1);
//    }
}