package com.stillcoolme.framework.self.di.model;

import lombok.Getter;

/**
 * @author: stillcoolme
 * @date: 2020/3/9 10:37
 * Function:
 */
public class MyClient {
    @Getter
    private Client client;

    public MyClient(Client client) {
        this.client = client;
    }

}
