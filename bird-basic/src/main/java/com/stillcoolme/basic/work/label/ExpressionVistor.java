package com.stillcoolme.basic.work.label;

public interface ExpressionVistor<R> {

    R visit(Expression other);

    R visit(LeafExpression leafExpression);
}
