package com.romanmarkunas.blog.memory.example14.checkers;


import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE_USE})
@SubtypeOf(NotAnImmutableByteArray.class)
public @interface ImmutableByteArray {}
