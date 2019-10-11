package com.stillcoolme.utils;

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
