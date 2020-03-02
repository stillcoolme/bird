package com.stillcoolme.basic.reflect;

import com.stillcoolme.basic.bean.Person;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: stillcoolme
 * @date: 2020/2/28 11:13
 */
public class BasicDemo {

    /**
     * 关键就是 Class类
     * Java中手动创建的每一个类，在编译后JVM都会为其创建一个Class类对象，它保存了所建类的信息。
     *
     * 通过Class类实例化对象，与new关键字创建对象区别：创建对象的方式不一样，前者是使用类加载机制，后者是创建一个新类。
     *
     * 从JVM的角度看，我们使用关键字new创建一个类的时候，这个类可以没有被加载。
     * 但是使用newInstance()方法的时候，就必须保证：1、这个类已经加载；2、这个类已经连接了。
     * 上面两个步骤的正是Class的静态方法forName()所完成的，这个静态方法调用了启动类加载器，即加载java API的那个加载器。
     * 可以看出，newInstance()把new这个方式分解为两步：即首先调用Class加载方法加载某个类，然后实例化。
     *  这样分步的好处是显而易见的。我们可以在调用class的静态加载方法forName时获得更好的灵活性，提供给了一种降耦的手段。
     *
     */
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        // 获取 Class类对象 的三种方法
        getClazz();
        // 创建实例
        newInst();
        // 通过Class类在运行时获得某个类对象的信息
        newMethod();

        /**
         * newInstance()在运行时实例化对象，
         * 关于运行时这一点，可以参考工厂模式与反射的结合，即只需要更改主方法中的类全限定名参数，就可以增加实例化对象。
         *
         *     进一步可以写成如下形式：
         *     String className = readfromXMlConfig;//从xml 配置文件中获得 类全限定名 字符串
         *     class c = Class.forName(className);
         *     factory = (ExampleInterface)c.newInstance();
         *
         */

    }

    /**
     * 获取 Class类对象 的三种方法，还不是获取 Person 对象。
     * @throws ClassNotFoundException
     */
    public static void  getClazz() throws ClassNotFoundException {
        Person person = new Person();

        //方式一 ：通过对象的getClass()方法，取得 Person  的 Class 类对象 Class<?>
        Class<?> clazz1 = person.getClass();

        //方式二：通过类的class属性
        Class<?> clazz2 = Person.class;

        //方式三：通过Class类的静态方法forName(String className)，参数必须是类的全限定名
        Class<?> clazz3 =Class.forName("com.stillcoolme.basic.bean.Person");

        System.out.println(clazz1.getName());

        System.out.println(clazz2.getName());

        System.out.println(clazz3.getName());
    }

    /**
     * 看看  getClass, getName 的区别
     *
     * clazz.getName()   得到的是 Person 的名
     *
     * clazz.getClass()  得到 Class 对象
     * clazz.newInstance().getClass()  得到 Person 对象
     */
    private static void newInst() {
        try {

            // 1. 反射创建对象
            // step1  Class.forName
            Class<?> clazz = Class.forName("com.stillcoolme.basic.bean.Person");
            System.out.println(clazz.getClass());   // class java.lang.Class
            System.out.println(clazz.getClass().getName());   // java.lang.Class
            System.out.println(clazz.getName());    //输出Class类对象中Person类的信息 com.stillcoolme.basic.bean.Person
            // step2  clazz.newInstance();
            Object object = clazz.newInstance();
            System.out.println(object.getClass());               //  class  com.stillcoolme.basic.bean.Person
            System.out.println(object.getClass().getName());     //  com.stillcoolme.basic.bean.Person

            // 2. 创建有参数的
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Object person = constructor.newInstance("lili");
        } catch (Exception  e) {
            e.printStackTrace();
        }

    }

    /**
     * 看看  getMethod 的怎么用
     * 直接 clazz.getMethod() (获得自己和父类的所有方法）
     * 而不是 clazz.getClass().getMethod()
     * 因为 clazz 对象才存储了类的信息
     */
    private static void newMethod() {
        try {

            Class<?> claz = Class.forName("com.stillcoolme.basic.bean.Person");
            Object obj = claz.newInstance();

            //无参无返回值
            Method met = claz.getMethod("getName");
            met.invoke(obj);

            //有参无返回值
            Method met2 = claz.getMethod("setName", String.class);
            met2.invoke(obj, "lili");
        } catch (Exception  e) {
            e.printStackTrace();
        }


    }

}
