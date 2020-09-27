package com.romanmarkunas.blog.memory.example14.checkers;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

public class ImmutableByteArrayChecker extends BaseTypeChecker {

    @Override
    protected BaseTypeVisitor<ImmutableByteArrayAnnotatedTypeFactory> createSourceVisitor() {
        return new ImmutableByteArrayVisitor(this);
    }
}
