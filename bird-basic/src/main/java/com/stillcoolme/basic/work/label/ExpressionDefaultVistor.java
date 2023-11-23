package com.stillcoolme.basic.work.label;

/**
 * <p>TODO</p>
 *
 * @author stillcoolme
 * @version V1.0.0
 * @date 2023/11/15 10:34
 */
public abstract class ExpressionDefaultVistor<R> implements ExpressionVistor<R> {

    protected abstract R defaultMethod(Expression expression);



}
