package com.learning.framework.util;

import org.apache.commons.lang3.ArrayUtils;

/**
 *对数组类的一些封装
 */
public class ArrayUtil {
    /**
     * 判断数组是否为空
     */
    public static boolean isEmpry(Object[] array) {
        return ArrayUtils.isEmpty(array);
    }

    /**
     * 判断数组是否非空
     */
    public static boolean isNotEmpty(Object[] array) {
        return ArrayUtils.isNotEmpty(array);
    }
}
