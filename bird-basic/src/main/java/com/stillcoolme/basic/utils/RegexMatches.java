package com.stillcoolme.basic.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatches {

    public static void main(String args[]) {
        String str = "jdbc:phoenix:thin:url=http://11.114.0.113:9880";
        String replaceStr = "11.114.0.113:9880";
        // String patternStr = "^jdbc:phoenix:thin:url=";

        String patternStr = "((http|https)://)((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(:([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$)";

        boolean result = Pattern.matches(patternStr, str);
        System.out.println(result);

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(str);

        String xx = matcher.replaceAll(replaceStr);
        System.out.println(xx);

    }

}
