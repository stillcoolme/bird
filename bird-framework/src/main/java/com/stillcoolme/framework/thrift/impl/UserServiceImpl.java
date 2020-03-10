package com.stillcoolme.framework.thrift.impl;

import com.stillcoolme.framework.thrift.User;
import com.stillcoolme.framework.thrift.UserService;
import org.apache.thrift.TException;

/**
 * @author: stillcoolme
 * @date: 2020/3/10 15:00
 * Function:
 */
public class UserServiceImpl implements UserService.Iface {

    @Override
    public String sayHello(User user) throws TException {
        return "Hi,My name is " + user.username + " and My age is " + user.age;
    }
}
