package com.romanmarkunas.blog.memory.example14.checkers;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.DiagMessage;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class ImmutableByteArrayVisitor extends BaseTypeVisitor<ImmutableByteArrayAnnotatedTypeFactory> {

    private static final String MUTATION = "byte.array.mutation";
    private static final String MISUSE = "byte.array.misuse";

    public ImmutableByteArrayVisitor(ImmutableByteArrayChecker checker) {
        super(checker);
    }


    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        ExpressionTree assignmentVarTree = node.getVariable();
        if (assignmentVarTree.getKind() == Tree.Kind.ARRAY_ACCESS) {
            if (isAnnotatedWithImmutableByteArray(atypeFactory.getAnnotatedType(node))) {
                checker.report(node, new DiagMessage(Diagnostic.Kind.ERROR, MUTATION));
            }
        }
        return super.visitAssignment(node, p);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        recursivelyCheckAnnotatedCorrectly(atypeFactory.getAnnotatedType(node), node);
        return super.visitVariable(node, p);
    }


    private void recursivelyCheckAnnotatedCorrectly(final AnnotatedTypeMirror annotatedType, VariableTree node) {
        TypeMirror type = annotatedType.getUnderlyingType();
        TypeKind typeKind = type.getKind();

        if (isAnnotatedWithImmutableByteArray(annotatedType)) {
            if (typeKind != TypeKind.ARRAY) {
                checker.report(node, new DiagMessage(Diagnostic.Kind.ERROR, MISUSE));
                return;
            }

            ArrayType arrayType = (ArrayType) type;
            TypeMirror componentType = arrayType.getComponentType();
            TypeKind componentTypeKind = componentType.getKind();
            if (componentTypeKind != TypeKind.BYTE) {
                checker.report(node, new DiagMessage(Diagnostic.Kind.ERROR, MISUSE));
                return;
            }
        }

        if (typeKind == TypeKind.ARRAY) {
            AnnotatedTypeMirror.AnnotatedArrayType annotatedArray = (AnnotatedTypeMirror.AnnotatedArrayType) annotatedType;
            recursivelyCheckAnnotatedCorrectly(annotatedArray.getComponentType(), node);
        }
        else if (typeKind == TypeKind.DECLARED) {
            AnnotatedTypeMirror.AnnotatedDeclaredType declaredType = (AnnotatedTypeMirror.AnnotatedDeclaredType) annotatedType;
            for (AnnotatedTypeMirror typeParameter : declaredType.getTypeArguments()) {
                recursivelyCheckAnnotatedCorrectly(typeParameter, node);
            }
        }
    }

    private boolean isAnnotatedWithImmutableByteArray(final AnnotatedTypeMirror annotatedType) {
        return annotatedType.getAnnotation(ImmutableByteArray.class) != null;
    }
}
