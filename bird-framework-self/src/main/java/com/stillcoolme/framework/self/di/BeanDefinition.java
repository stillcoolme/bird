package com.stillcoolme.framework.self.di;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.BufferedOutputStream;
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

        public static Builder newBuilder() {
            return new Builder();
        }

        public static class Builder {
            private boolean isRef;
            private Class type;
            private Object arg;

            public Builder setIsRef(boolean isRef) {
                this.isRef = isRef;
                return this;
            }

            public Builder setType(Class type) {
                this.type = type;
                return this;
            }

            public Builder setArg(Object arg) {
                this.arg = arg;
                return this;
            }

            public ConstructorArg doBuilder() {
                ConstructorArg constructorArg = new ConstructorArg();
                // 如果这个参数是引用类型，就不用设置Arg， 只要设置 Type class
                if(isRef != true) {
                    constructorArg.setRef(false);
                    constructorArg.setArg(arg);
                    constructorArg.setType(type);
                } else {
                    constructorArg.setRef(true);
                    constructorArg.setArg(arg);
                }
                return constructorArg;
            }

        }
    }
}
