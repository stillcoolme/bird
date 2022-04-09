package com.learning.framework.helper;

import com.learning.framework.annotation.Inject;
import com.learning.framework.util.CollectionUtil;
import com.learning.framework.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 依赖注入助手类
 */
public final class IoCHelper {
    //获取所有bean类与bean实例之间的映射关系
    static {
        Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
        if (CollectionUtil.isNotEmpty(beanMap)) {
           //遍历BeanMap
            for (Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()) {
                //从BeanMap中获取Bean以及Bean实例
                Class<?> beanClass=beanEntry.getKey();
                Object instance=beanEntry.getValue();
                //获取Bean类定义的所有成员变量(Bean Field)
                Field[] fields=beanClass.getDeclaredFields();
                if (com.learning.framework.util.ArrayUtil.isNotEmpty(fields)) {
                    //遍历bean field
                    for (Field field : fields) {
                        //判断当前field是否带有Inject注解
                        if (field.isAnnotationPresent(Inject.class)) {
                            //在beanMap中获取对应的实例
                            Class<?> beanFieldClass=field.getType();
                            Object beanFieldInstance = beanMap.get(beanFieldClass);
                            if (beanFieldInstance != null) {
                                ReflectionUtil.setField(instance, field, beanFieldInstance);
                            }
                        }
                    }
                }
            }
        }
    }
}
