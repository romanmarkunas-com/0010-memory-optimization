package com.romanmarkunas.blog.memory.example14.checkers;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class ImmutableByteArrayAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    public ImmutableByteArrayAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }


    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        return new HashSet<>(Set.of(
                ImmutableByteArray.class,
                MaybeImmutableByteArray.class
        ));
    }
}
