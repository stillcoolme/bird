package com.learning.framework.annotation;

import java.lang.annotation.*;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-17 18:48:00
 * @Description 切面注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {

    Class<? extends Annotation> value();
}
