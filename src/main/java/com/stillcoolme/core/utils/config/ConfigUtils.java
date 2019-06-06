package com.stillcoolme.core.utils.config;

public class ConfigUtils {

    public static MyProperties appProp;

    static{
        appProp = MyProperties.getInstance("app.properties");
    }

    public static void main(String[] args) {
        System.out.println(ConfigUtils.appProp.get("port"));
    }
}
