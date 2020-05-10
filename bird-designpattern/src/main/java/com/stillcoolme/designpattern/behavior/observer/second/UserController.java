package com.stillcoolme.designpattern.behavior.observer.second;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2020/5/9 16:49
 * Function:
 *   当我们需要添加新的观察者的时候，
 *   比如，用户注册成功之后，推送用户注册信息给大数据征信系统，
 *   UserController 类的 register() 函数完全不需要修改，只需要再添加一个实现了 RegObserver 接口的类
 */
public class UserController {

    // private UserService userService; // 依赖注入
    private List<RegObserver> regObservers = new ArrayList<>();

    // 一次性设置好，之后也不可能动态的修改
    public void setRegObservers(List<RegObserver> observers) {
        regObservers.addAll(observers);
    }

    public Long register(String name, String password) {
        //省略输入参数的校验代码
        //省略userService.register()异常的try-catch代码
        // long userId = userService.register(name, password);

        for (RegObserver observer : regObservers) {
            observer.handleRegSuccess(1);
        }
        return null;
    }


    public static void main(String[] args) {
        UserController userController = new UserController();

        ArrayList<RegObserver> observerList = new ArrayList<>();
        observerList.add(new RegNotificationObserver());
        observerList.add(new RegPromotionObserver());

        userController.setRegObservers(observerList);

        userController.register("bobo", "123456");
    }
}
