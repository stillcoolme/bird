package com.stillcoolme.designpattern.behavior.observer.eventbus.app;

import com.stillcoolme.designpattern.behavior.observer.eventbus.Subscribe;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 10:07
 * Function:
 */
public class RegNotificationObserver {
    // private NotificationService notificationService;

    @Subscribe
    public void handleRegSuccess(long userId) {

        // notificationService.sendInboxMessage(userId, "...");
    }

}
