package com.stillcoolme.basic.utils.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 读取相应配置文件中的所有配置，也可根据配置名获得配置项
 */
public class ConfigUtils {

    static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static void main(String[] args) {

        // 方式一加载配置文件 配置文件名作为一个静态变量
        logger.info((String)
                ConfigUtils.appProp.get("app.name")
        );

        // 方式二加载配置文件
        logger.info(getConf("app", "app.name"));
    }

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

    ///////////////////////////// 另一部分
    // 写的时候忘记还能用map来放多个配置文件的配置。。
    private static Map<String, Properties> confMap = new ConcurrentHashMap<>();

    /**
     * 读取resource目录下的 .properties 配置
     * @param fileName 默认读取 conf 文件配置名，不包含 .properties
     * @param confKey
     * @return
     */
    public static String getConf(String fileName, String confKey) {
        try {
            Properties properties = confMap.get(fileName);
            if (properties == null) {
                properties = loadProperties(fileName + ".properties");
                confMap.put(fileName, properties);
            }
            return properties.getProperty(confKey);
        } catch (Exception ex) {
            logger.error("error load conf file: " + fileName);
            System.exit(3);
            return null;
        }
    }

    public static String getConf(String confKey) {
        return getConf("conf", confKey);
    }

    private static Properties loadProperties(String fileName) throws IOException {
        try {
            return loadProperties(fileName, ConfigUtils.class.getClassLoader());
        } catch (NullPointerException e) {
            throw new IOException("找不到文件 " + fileName);
        }
    }

    private static Properties loadProperties(String fileName, ClassLoader classLoader) throws IOException {
        InputStream resourceAsStream = classLoader.getResourceAsStream(fileName);
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
            return properties;
        } finally {
            resourceAsStream.close();
        }
    }

}
