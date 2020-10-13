package com.romanmarkunas.blog.memory.example14.checkers;

import com.sun.source.tree.*;
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
    private static final String WEAKENING = "byte.array.weakening";

    public ImmutableByteArrayVisitor(ImmutableByteArrayChecker checker) {
        super(checker);
    }


    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        checkArrayAssignment(node.getVariable(), node);
        return super.visitAssignment(node, p);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        checkArrayAssignment(node.getVariable(), node);
        return super.visitCompoundAssignment(node, p);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        recursivelyCheckAnnotatedCorrectly(atypeFactory.getAnnotatedType(node), node);
        if (node.getInitializer() != null) {
            checkAssignment(node, node.getInitializer(), node);
        }
        return super.visitVariable(node, p);
    }

    private void checkAssignment(VariableTree target, ExpressionTree source, VariableTree node) {
        AnnotatedTypeMirror annotatedSource = atypeFactory.getAnnotatedType(source);
        AnnotatedTypeMirror annotatedTarget = atypeFactory.getAnnotatedType(target.getType());

        if (!isAnnotatedWithImmutableByteArray(annotatedTarget)
                && isAnnotatedWithImmutableByteArray(annotatedSource)) {
            checker.report(node, new DiagMessage(Diagnostic.Kind.ERROR, WEAKENING));
        }
    }

//    @Override
//    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
//        return super.visitMethodInvocation(node, p);
//    }

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

    private void checkArrayAssignment(final ExpressionTree assignmentVarTree, ExpressionTree node) {
        if (assignmentVarTree.getKind() == Tree.Kind.ARRAY_ACCESS) {
            ArrayAccessTree arrayAccessTree = (ArrayAccessTree) assignmentVarTree;
            ExpressionTree arrayIdentifier = arrayAccessTree.getExpression();
            AnnotatedTypeMirror arrayType = atypeFactory.getAnnotatedType(arrayIdentifier);

            if (isAnnotatedWithImmutableByteArray(arrayType)) {
                checker.report(node, new DiagMessage(Diagnostic.Kind.ERROR, MUTATION));
            }
        }
    }
}
