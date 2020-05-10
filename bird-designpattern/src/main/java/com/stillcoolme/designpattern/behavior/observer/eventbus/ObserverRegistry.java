package com.stillcoolme.designpattern.behavior.observer.eventbus;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author: stillcoolme
 * @date: 2020/5/9 20:58
 * Function: 观察者注册表，好强！！
 */
public class ObserverRegistry {

    private ConcurrentMap<Class<?>, CopyOnWriteArraySet<ObserverAction>> registry = new ConcurrentHashMap<>();

    /**
     * @param observer 允许任意类型的观察者注册
     */
    public void register(Object observer) {
        Map<Class<?>, Collection<ObserverAction>> observerActions = findObserverActions(observer);
        for (Map.Entry<Class<?>, Collection<ObserverAction>> entry : observerActions.entrySet()) {
            Class<?> eventType = entry.getKey();
            Collection<ObserverAction> observerAction = entry.getValue();
            CopyOnWriteArraySet<ObserverAction> registeredObserverAction = registry.get(eventType);
            if (registeredObserverAction == null) {
                registry.putIfAbsent(eventType, new CopyOnWriteArraySet<ObserverAction>());
                registeredObserverAction = registry.get(eventType);
            }
            registeredObserverAction.addAll(observerAction);
        }
    }

    /**
     * 获得该次监听到的消息类型要调用哪些方法
     *
     * @param event
     * @return
     */
    public List<ObserverAction> getMatchedObserverActions(Object event) {
        List<ObserverAction> matchedObservers = new ArrayList<>();
        Class<?> postEventType = event.getClass();
        registry.forEach((eventType, observerActions) -> {
            if (postEventType.isAssignableFrom(eventType)) {
                matchedObservers.addAll(observerActions);
            }
        });
        return matchedObservers;
    }


    /**
     * 得到该 Observer 类 里面所有注解了 @Subscribe 的方法
     *
     * @param observer
     * @return
     */
    private Map<Class<?>, Collection<ObserverAction>> findObserverActions(Object observer) {
        Map<Class<?>, Collection<ObserverAction>> observerActions = new HashMap<>();
        Class<?> clazz = observer.getClass();
        for (Method method : getAnnotationMethod(clazz)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            // 观察者接收的消息类型
            Class<?> eventType = parameterTypes[0];
            if (!observerActions.containsKey(eventType)) {
                observerActions.put(eventType, new ArrayList<>());
            }
            observerActions.get(eventType).add(new ObserverAction(clazz, method));
        }
        return observerActions;
    }

    private List<Method> getAnnotationMethod(Class clazz) {
        ArrayList<Method> methodList = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    methodList.add(method);
                }
            }
        }
        return methodList;
    }

}
