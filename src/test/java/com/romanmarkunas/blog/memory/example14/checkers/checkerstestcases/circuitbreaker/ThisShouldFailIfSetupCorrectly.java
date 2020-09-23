package com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.circuitbreaker;

import org.checkerframework.checker.units.qual.Length;

import java.util.Random;

public class ThisShouldFailIfSetupCorrectly {

   public void shouldNotAllowAssigningUnknownUnitsToLength() {
        int unknownUnitType = new Random().nextInt(10) - 5;
        // :: error: (assignment.type.incompatible)
        @Length int lengthValue = unknownUnitType;
        if (lengthValue < 0) {
            throw new RuntimeException();
        }
    }
}