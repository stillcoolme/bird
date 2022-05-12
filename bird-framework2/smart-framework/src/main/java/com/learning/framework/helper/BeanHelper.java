package com.learning.framework.helper;

import com.learning.framework.util.ReflectionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bean助手类
 */
public class BeanHelper {
    /**
     * 定义Bean映射，用于存放Bean类与Bean实例的关系
     */
    private static final Map<Class<?>, Object> BEAN_MAP = new HashMap<Class<?>, Object>();

    static {
        Set<Class<?>> classSet = ClassHelper.getBeanClassSet();
        for (Class<?> cls : classSet) {
            Object obj = ReflectionUtil.newInstance(cls);
            BEAN_MAP.put(cls, obj);
        }
    }

    /**
     * 获取Bean映射
     */
    public static Map<Class<?>, Object> getBeanMap() {
        return BEAN_MAP;
    }

    /**
     * 获取Bean实例
     */
    public static <T> T getBean(Class<T> cls) {
        if (!BEAN_MAP.containsKey(cls)) {
            throw new RuntimeException("can not get bean by class " + cls);
        }
        return (T) BEAN_MAP.get(cls);
    }

    /**
     * 设置实例
     * @param cls
     * @param t
     */
    public static void setBean(Class<?> cls, Object t) {
        BEAN_MAP.put(cls, t);
    }

}
