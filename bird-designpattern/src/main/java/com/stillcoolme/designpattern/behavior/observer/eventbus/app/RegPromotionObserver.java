package com.stillcoolme.designpattern.behavior.observer.eventbus.app;

import com.stillcoolme.designpattern.behavior.observer.eventbus.Subscribe;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 10:02
 * Function:
 */
public class RegPromotionObserver {

    // private PromotionService promotionService; // 依赖注入

    // 通过这个注解来说明 该监听者监听到事件后要执行该方法
    @Subscribe
    public void handleRegSuccess(long userId) {
        // promotionService.issueNewUserExperienceCash(userId);
    }
}
