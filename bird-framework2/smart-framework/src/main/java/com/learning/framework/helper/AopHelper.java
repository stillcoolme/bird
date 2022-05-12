package com.learning.framework.helper;

import com.learning.framework.annotation.Aspect;
import com.learning.framework.proxy.AspectProxy;
import com.learning.framework.proxy.Proxy;
import com.learning.framework.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2022-01-18 09:44:00
 * @Description
 *  方法拦截助手类，用来加载AOP框架
 */
@Slf4j
public class AopHelper {

    static {
        try{
            Map<Class<?>, Set<Class<?>>> proxyMap = createProxyMap();
            // 获取 目标类 和 代理对象之间的映射关系
            Map<Class<?>, List<Proxy>> targetMap = createTargetMap(proxyMap);
            for(Map.Entry<Class<?>, List<Proxy>> targetEntry:targetMap.entrySet()) {

                Class<?> targetClass = targetEntry.getKey();
                List<Proxy> proxies = targetEntry.getValue();
                Object proxy = ProxyManager.createProxy(targetClass, proxies);
                BeanHelper.setBean(targetClass, proxy);

            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 获取具有@Aspect注解中设置的注解类
     * @param aspect
     * @return
     * @throws Exception
     */
    public static final Set<Class<?>> createTargetClassSet(Aspect aspect) throws Exception{
        Set<Class<?>> targetClassSet = new HashSet<Class<?>>();

        Class<? extends Annotation> annotation = aspect.value();
        // 排除掉自己，不是 Aspect类
        if(annotation != null && !annotation.equals(Aspect.class)) {

            // 比如 @Aspect(Controller.class)，就获取到 Controller.class，这样就对所有 Controller.class 进行注解的意思
            targetClassSet.addAll(ClassHelper.getClassSetByAnnotation(annotation));
        }
        return targetClassSet;
    }

    /**
     * 获取代理类（切面类） 及其 目标类（被代理类）集合的映射关系， 根据这个关系才能分析查 目标类 与 代理对象列表之间的 映射关系
     *
     * 比如 ControllerAspcet，确定其有 @Aspect注解，然后获取注解的value，看对什么类进行AOP，存入 proxyMap
     * @return
     */
    public static final Map<Class<?>, Set<Class<?>>> createProxyMap() throws Exception {
        Map<Class<?>, Set<Class<?>>> proxyMap = new HashMap<Class<?>, Set<Class<?>>>();

        Set<Class<?>> proxyClassSet = ClassHelper.getClassSetBySuper(AspectProxy.class);
        for (Class<?> proxyClass: proxyClassSet) {
            if (proxyClass.isAnnotationPresent(Aspect.class)) {
                Aspect aspect = proxyClass.getAnnotation(Aspect.class);
                Set<Class<?>> targetClassSet = createTargetClassSet(aspect);
                proxyMap.put(proxyClass, targetClassSet);
            }
        }
        return proxyMap;
    }

    /**
     * 根据 createProxyMap() 获取 目标类 和 代理对象之间的映射关系
     * targetMap:  targetClass -> List(proxyClass)
     * @param proxyMap
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static final Map<Class<?>, List<Proxy>> createTargetMap(Map<Class<?>,Set<Class<?>>> proxyMap) throws InstantiationException, IllegalAccessException{
        Map<Class<?>, List<Proxy>> targetMap = new HashMap<Class<?>, List<Proxy>>();

        for (Map.Entry<Class<?>, Set<Class<?>>> proxyEntry: proxyMap.entrySet()) {
            Class<?> proxyClass = proxyEntry.getKey();
            Set<Class<?>> targetClassSet = proxyEntry.getValue();
            for(Class<?> targetClass : targetClassSet) {
                Proxy proxy = (Proxy) proxyClass.newInstance();
                if (targetMap.containsKey(targetClass)) {
                    targetMap.get(targetClass).add(proxy);
                } else {
                    List<Proxy> proxyList = new ArrayList<Proxy>();
                    proxyList.add(proxy);
                    targetMap.put(targetClass, proxyList);
                }
            }

        }
        return targetMap;
    }

}
