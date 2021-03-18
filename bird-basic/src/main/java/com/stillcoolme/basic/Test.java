package com.stillcoolme.basic;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
@Slf4j
public class Test {

    public static void main(String[] args) {

        String env = "localhost:5000/bos-web/api/v1";
//         String env = "11.114.0.108:5000/bos-web/api/v1";

        Long second = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));

        // 1. 查策略
        String parameter2 = "group_id=TDCM1B";   // TDCM1B
        String req2 = "accesskey_id=aba_api&" + parameter2 + "&timestamp=%s&version=v1.0&cs@2020";
        req2 = String.format(req2, String.valueOf(second));
        String req2_2 = md5Util(req2);

        req2 = env + "/call-script/query?" + req2.replace("cs@2020", "signature=") + req2_2;


        // 2. 查话术
        String parameter = "strategy_id=1";
        String req1 = "accesskey_id=aba_api&" + parameter + "&timestamp=%s&version=v1.0&cs@2020";
        req1 = String.format(req1, String.valueOf(second));
        String req1_2 = md5Util(req1);

        req1 = env + "/call-script/query?" + req1.replace("cs@2020", "signature=") + req1_2;


        // 4. 评价策略
        String parameter4 = "strategy_id=1";
        String req4 = "accesskey_id=aba_api&" + parameter4 + "&timestamp=%s&version=v1.0&cs@2020";
        req4 = String.format(req4, String.valueOf(second));
        String req4_2 = md5Util(req4);
        req4 = env + "/call-script/score?" + req4.replace("cs@2020", "signature=") + req4_2;


        // 4. 评价话术
        String parameter3 = "score=1";
        String req3 = "accesskey_id=aba_api&" + parameter3 + "&timestamp=%s&version=v1.0&cs@2020";
        req3 = String.format(req3, String.valueOf(second));
        String req3_2 = md5Util(req3);
        req3 = env + "/call-script/score?" + req3.replace("cs@2020", "signature=") + req3_2;


        System.out.println("查策略");
        System.out.println(req2);

        System.out.println("查话术");
        System.out.println(req1);

        System.out.println("评价策略");
        System.out.println(req4);

        System.out.println("评价话术");
        System.out.println(req3);


    }


    private static String md5Util(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString().toUpperCase();
    }

}
