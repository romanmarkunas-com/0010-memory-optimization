package com.romanmarkunas.blog.memory.example14.checkers;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.DiagMessage;
import org.checkerframework.framework.type.AnnotatedTypeMirror;

import javax.tools.Diagnostic;

public class ImmutableByteArrayVisitor extends BaseTypeVisitor<ImmutableByteArrayAnnotatedTypeFactory> {

    private static final String MUTATION_ERROR = "byte.array.mutation";

    public ImmutableByteArrayVisitor(ImmutableByteArrayChecker checker) {
        super(checker);
    }


    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        ExpressionTree assignmentVarTree = node.getVariable();
        if (assignmentVarTree.getKind() == Tree.Kind.ARRAY_ACCESS) {
            ArrayAccessTree arrayAccessTree = (ArrayAccessTree)assignmentVarTree;
            ExpressionTree arrayIdentifier = arrayAccessTree.getExpression();
            AnnotatedTypeMirror arrayType = atypeFactory.getAnnotatedType(arrayIdentifier);

            if (arrayType.getAnnotation(ImmutableByteArray.class) != null) {
                checker.report(node, new DiagMessage(Diagnostic.Kind.ERROR, MUTATION_ERROR));
            }
        }
        return super.visitAssignment(node, p);
    }
}
