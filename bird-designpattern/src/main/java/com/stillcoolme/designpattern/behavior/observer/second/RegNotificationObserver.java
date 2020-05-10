package com.stillcoolme.designpattern.behavior.observer.second;


/**
 * @author: stillcoolme
 * @date: 2020/5/9 16:46
 * Function:
 *  发送一封“欢迎注册成功”的站内信 的观察者
 */
public class RegNotificationObserver implements RegObserver {

    // private NotificationService notificationService;

    @Override
    public void handleRegSuccess(long userId) {
       // notificationService.sendInboxMessage(userId, "Welcome...");
    }
}
