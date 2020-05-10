package com.stillcoolme.designpattern.behavior.observer.second;

/**
 * @author: stillcoolme
 * @date: 2020/5/9 16:45
 * Function:
 *  注册后 发送优惠券 的观察者
 */
public class RegPromotionObserver implements RegObserver {

    // private PromotionService promotionService; // 依赖注入

    @Override
    public void handleRegSuccess(long userId) {
       // promotionService.issueNewUserExperienceCash(userId);
    }

}
