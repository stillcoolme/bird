package com.stillcoolme.basic.utils.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取相应配置文件中的所有配置，也可根据配置名获得配置项
 */
public class ConfigUtils {

    public static Properties appProp;

    static{
        // classpath: 其实也就是 包的根路径
        appProp = MyProperties.getMyProperties("classpath:app.properties");
    }

    static class MyProperties{
        public static Properties getMyProperties(String location){
            Properties properties = new Properties();
            if(location != null && location.length() > 0) {
                InputStream inputStream = null;
                if (location.toLowerCase().startsWith("classpath:")) {
                    String path = location.substring("classpath:".length());
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                    inputStream = ConfigUtils.MyProperties.class.getResourceAsStream(path);
                } else {
                    if (!location.startsWith("/")) {
                        location = "/" + location;
                    }
                    inputStream = ConfigUtils.MyProperties.class.getResourceAsStream(location);
                }

                try {
                    properties.load(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return properties;
            } else {
                return null;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(ConfigUtils.appProp.get("app.name"));
    }
}
