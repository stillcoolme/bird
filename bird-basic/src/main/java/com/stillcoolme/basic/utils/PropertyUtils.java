package com.stillcoolme.basic.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author: stillcoolme
 * @date: 2019/10/9 16:21
 * @description:
 */
public class PropertyUtils {

    public static void main(String[] args) {
        /**
         * 1. Class.getResourceAsStream(String path) ：
         *      path 不以’/'开头时默认是从此类所在的包下取资源，以’/'开头则是从ClassPath根下获取。其只是通过path构造一个绝对路径，最终还是由ClassLoader获取资源。
         * 2. Class.getClassLoader.getResourceAsStream(String path) ：
         *      默认则是从ClassPath根下获取，path不能以’/'开头，最终是由ClassLoader获取资源。
         */
        InputStream inStream = PropertyUtils.class.getClassLoader().getResourceAsStream("app.properties");
        Properties prop = new Properties();
        try {
            prop.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String key = prop.getProperty("app.name");

    }
}
