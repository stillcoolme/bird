package com.stillcoolme.basic.utils.config;

import com.stillcoolme.basic.io.filechannel.Test;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * @author: stillcoolme
 * @date: 2019/10/9 16:21
 * @description:
 */
@Slf4j
public class PropertyUtils {

    /**
     * 从 Resource 目录读取配置文件
     * @param fileName
     */
    public static void getPropFromResourcePath(String fileName) {
        /**
         * 1. class.getResourceAsStream(String path) ：
         *      path 不以’/'开头时默认是从此类所在的包下取资源，以’/'开头则是从ClassPath根下获取，所以一般要带'/'。
         *      其只是通过path构造一个绝对路径，最终还是由ClassLoader获取资源。
         * 2. class.getClassLoader.getResourceAsStream(String path) ：
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
        System.out.println(key);
    }

    /**
     * 读取jar包同级目录的配置文件
     * @param fileName
     * @return
     */
    public static Properties getConfigOutJar(String fileName) {
        // 读取配置文件
        Properties properties = new Properties();
        try {
            // 读取系统外配置文件 (即Jar包外文件)，当前用户目录的相对路径。这是一颗定时炸弹，随时可能要你的命。
            // 尽量使用URI形式的绝对路径资源。它可以很容易的转变为URI，URL，File对象。
            String filePath = System.getProperty("user.dir")
                    + "/" + fileName;
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            properties.load(in);
        } catch (IOException e) {
            log.error("读取配置信息出错！", e);
        }
        return properties;
    }

    /**
     * 获取当前类路径，classpath路径
     */
    public static void getAbsolutePath() {

        // 获取当前类路径
        // file:/Users/stillcoolme/Documents/ideaProject/myself/bird/bird-basic/target/classes/com/stillcoolme/basic/io/filechannel/
        URL resource = Test.class.getResource("");
        System.out.println(resource.toString());
        System.out.println(resource.getPath());
        // classpath路径
        // file:/Users/stillcoolme/Documents/ideaProject/myself/bird/bird-basic/target/classes/
        URL resource2 = Test.class.getResource("/");
        System.out.println(resource2.toString());
        System.out.println(resource2.getPath());

    }


    public static void main(String[] args) {
        getPropFromResourcePath("");

        getAbsolutePath();
    }
}
