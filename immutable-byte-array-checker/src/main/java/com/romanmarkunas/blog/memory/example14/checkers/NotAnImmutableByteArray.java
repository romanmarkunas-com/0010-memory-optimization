package com.romanmarkunas.blog.memory.example14.checkers;


import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Target;

@Target({}) // not supposed to be used in code, implicit if not annotated
@DefaultQualifierInHierarchy
@SubtypeOf({})
@interface NotAnImmutableByteArray {}
