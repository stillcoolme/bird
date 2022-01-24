package com.learning.framework;

import com.learning.framework.helper.*;
import com.learning.framework.util.ClassUtil;

/**
 * 加载相应的Helper类
 */
public final class HelperLoader {
    public static void init() {
        Class<?>[] classList={
                ClassHelper.class,
                BeanHelper.class,
                AopHelper.class,  // Aop需要在Ioc之前，因为需要通过AopHelper获取代理对象，然后才能通过IocHelper依赖注入
                IoCHelper.class,
                ControllerHelper.class,

        };
        for (Class<?> cls : classList) {
            ClassUtil.loadClass(cls.getName(),false);
        }
    }
}
