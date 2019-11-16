package com.stillcoolme.framework.spider;

import us.codecraft.webmagic.Spider;

/**
 * @author: stillcoolme
 * @date: 2019/11/14 13:38
 * @description: 参考： https://juejin.im/post/5dcb94856fb9a04a98477eb0?utm_source=tuicool&utm_medium=referral
 */
public class Main {

    public static void main(String[] args) {
        Spider.create(new FilmProcessor())
                .addUrl(FilmProcessor.URL)
                .run();
    }
}
