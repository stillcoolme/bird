package com.learning.framework.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    //判断字符串是否为空
    public static boolean isEmpty(String str) {
        if (str!=null)
            str=str.trim();
        return StringUtils.isEmpty(str);
    }

    //判断字符串是否非空
    public static boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }

    //split
    public static String[] split(String string, char c) {
        return StringUtils.split(string, c);
    }
}
