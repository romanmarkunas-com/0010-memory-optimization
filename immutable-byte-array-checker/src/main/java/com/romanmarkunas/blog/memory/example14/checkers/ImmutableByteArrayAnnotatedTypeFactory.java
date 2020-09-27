package com.romanmarkunas.blog.memory.example14.checkers;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class ImmutableByteArrayAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    final AnnotationMirror IMMUTABLE_BYTE_ARRAY_ANNOTATION
            = AnnotationBuilder.fromClass(elements, ImmutableByteArray.class);
    final AnnotationMirror NOT_IMMUTABLE_BYTE_ARRAY_ANNOTATION
            = AnnotationBuilder.fromClass(elements, NotAnImmutableByteArray.class);


    public ImmutableByteArrayAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        postInit();
    }


    @Override
    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        return new HashSet<>(Set.of(
                ImmutableByteArray.class,
                NotAnImmutableByteArray.class
        ));
    }
}
