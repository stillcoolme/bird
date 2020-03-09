package com.stillcoolme.framework.self.di;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2020/3/9 10:53
 * Function:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BeanDefinition {

    private String id;
    private String className;
    private List<ConstructorArg> constructorArgs = new ArrayList<>();
    private Scope scope = Scope.SINGLETON;
    private boolean lazyInit = false;


    public boolean isSingleton() {
        return scope.equals(Scope.SINGLETON);
    }

    public static enum Scope {
        SINGLETON,
        PROTOTYPE
    }

    @Data
    public static class ConstructorArg {
        private boolean isRef;
        private Class type;
        private Object arg;
    }
}
