package com.learning.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropsUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropsUtil.class);

    /**
     * 加载属性文件
     */
    public static Properties loadProps(String fileName) {
        Properties properties = null;
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            if (is == null)
                throw new FileNotFoundException("no such file exists");
            properties = new Properties();
            properties.load(is);
        } catch (IOException e) {
            LOGGER.error("failed to load properties", e);
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("close inputstream failure", e);
                }
        }
        return properties;
    }

    /**
     * 获取字符型属性,默认值为""
     */

    public static final String getString(Properties props, String key) {
        return getString(props, key, "");
    }

    //获取字符型属性，自己设置默认值
    public static final String getString(Properties props, String key, String defaultValue) {
        String value = defaultValue;
        if (props.contains(key))
            value = props.getProperty(key);
        return value;
    }

    /**
     * 获取数值属性,默认值为0
     */

    public static final int getInt(Properties props, String key) {
        return getInt(props, key, 0);
    }

    //获取数值型属性，自己设置默认值
    public static final int getInt(Properties props, String key, int defaultValue) {
        int value = defaultValue;
        if (props.contains(key))
            value = CastUtil.castInt(props.getProperty(key));
        return value;
    }

    //获取布尔型属性，默认值false
    public static final boolean getBoolean(Properties props, String key) {
        return getBoolean(props, key, false);
    }
    //获取布尔型属性，自己设置默认值
    public static final boolean getBoolean(Properties props, String key, boolean defaultValue) {
        boolean value = defaultValue;
        if (props.containsKey(key)) {
            value = CastUtil.castBoolean(props.getProperty(key));
        }
        return value;
    }
}
