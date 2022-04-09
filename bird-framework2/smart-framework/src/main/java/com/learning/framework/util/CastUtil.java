package com.learning.framework.util;

/**
 * 类型转换的工具类
 */
public class CastUtil {
    /**
     * 转为string类型,默认值为“”
     *
     * @param obj
     */
    public static String castString(Object obj) {
        return castString(obj, "");
    }

    /**
     * 转为string，自己提供默认值
     *
     * @param obj
     * @param defaultValue
     * @return
     */
    public static String castString(Object obj, String defaultValue) {
        return obj == null ? defaultValue : String.valueOf(obj);
    }

    /**
     * 转为double，默认值为0
     *
     * @param obj
     * @return
     */
    public static double castDouble(Object obj) {
        return castDouble(obj, 0);
    }

    /**
     * 转为double，可以自己提供默认值
     *
     * @param obj
     * @param defaultValue
     * @return
     */
    public static double castDouble(Object obj, double defaultValue) {
        double doubleValue = defaultValue;
        if (obj != null) {
            String strValue = castString(obj);
            if (StringUtil.isNotEmpty(strValue)) try {
                doubleValue = Double.parseDouble(strValue);
            } catch (NumberFormatException e) {
                doubleValue = defaultValue;
            }
        }
        return doubleValue;
    }

    /**
     * 转为long，默认值为0
     *
     * @param obj
     * @return
     */
    public static long castLong(Object obj) {
        return castLong(obj, 0);
    }

    /**
     * 转为long，可以自己提供默认值
     *
     * @param obj
     * @param defaultVaue
     * @return
     */
    public static long castLong(Object obj, long defaultVaue) {
        long longValue = defaultVaue;
        if (obj != null) {
            String strValue = String.valueOf(obj);
            if (StringUtil.isNotEmpty(strValue)) {
                try {
                    longValue = Long.parseLong(strValue);
                } catch (NumberFormatException e) {
                    longValue = defaultVaue;
                }
            }
        }
        return longValue;
    }

    /**
     * 转为int，可以自己提供默认值
     *
     * @param obj
     * @param defaultValue
     * @return
     */
    public static int castInt(Object obj, int defaultValue) {
        int intValue = defaultValue;
        if (obj != null) {
            String strValue = castString(obj);
            if (StringUtil.isNotEmpty(strValue)) {
                try {
                    intValue = Integer.parseInt(strValue);
                } catch (NumberFormatException e) {
                    intValue = defaultValue;
                }
            }
        }
        return intValue;
    }

    /**
     * 转为int，默认值0
     *
     * @param obj
     * @return
     */
    public static int castInt(Object obj) {
        return castInt(obj, 0);
    }

    /**
     * 转为boolean，默认值false
     *
     * @param obj
     * @return
     */
    public static boolean castBoolean(Object obj) {
        return castBoolean(obj, false);
    }

    /**
     * 转为boolean，自己提供默认值
     *
     * @param obj
     * @param defaultValue
     * @return
     */
    public static boolean castBoolean(Object obj, boolean defaultValue) {
        boolean booleanValue = defaultValue;
        if (obj != null)
            booleanValue = Boolean.parseBoolean(castString(obj));
        return booleanValue;
    }
}
