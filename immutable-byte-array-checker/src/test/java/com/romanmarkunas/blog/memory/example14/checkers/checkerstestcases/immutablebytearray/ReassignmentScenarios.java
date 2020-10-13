package com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.immutablebytearray;

import com.romanmarkunas.blog.memory.example14.checkers.ImmutableByteArray;

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

//    public void shouldNotAllowMutationUsingArrayFill(byte @ImmutableByteArray [] array) {
//        // :: error: (assignment.type.incompatible)
//        Arrays.fill(array, (byte) 1);
//    }

    // reassignment to var should be forbidden as well

    // nested arrays

//    public void shouldNotAllowAssignmentToUnannotatedVariable(SomeType<byte @ImmutableByteArray []> array) {
//        // :: error: (assignment.type.incompatible)
//        SomeType<byte[]> temp = array;
//        temp.get()[0] = (byte) 1;
//    }

//    public void failAnnotatingOtherTypesAsGenericParameter() {
//        // :: error: (byte.array.misuse)
//        AnnotationUsageScenarios.SomeType<@ImmutableByteArray byte[]> foo.get()[0] = 1;
//        // :: error: (byte.array.misuse)
//        AnnotationUsageScenarios.SomeType<AnnotationUsageScenarios.SomeType<AnnotationUsageScenarios.SomeType<int @ImmutableByteArray []>>> bar;
//    }
}