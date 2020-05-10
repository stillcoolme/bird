package com.stillcoolme.designpattern.behavior.observer.eventbus.app;

import com.stillcoolme.designpattern.behavior.observer.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 9:54
 * Function:
 */
public class AppUserController {

    private EventBus eventBus;

    public AppUserController() {
        eventBus = new EventBus();
//        eventBus = new EventBusAsync();
    }

    public void setRegObservers(List<Object> observers) {
        for (Object observer : observers) {
            eventBus.register(observer);
        }
    }

    public long userRegister(String name, String password) {
        long userid = 1;

//       userid =  userService.register(name, password);

        // 通知消费者
        eventBus.post(userid);

        return userid;
    }

    public static void main(String[] args) {
        AppUserController appUserController = new AppUserController();

        // 注册观察者
        List<Object> observerList = new ArrayList<>();
        observerList.add(new RegPromotionObserver());
        observerList.add(new RegNotificationObserver());
        appUserController.setRegObservers(observerList);

        appUserController.userRegister("bobo", "123456");
    }

}
