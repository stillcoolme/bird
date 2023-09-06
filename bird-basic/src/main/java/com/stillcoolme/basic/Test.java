package com.stillcoolme.basic;

import com.stillcoolme.basic.utils.LocalDateTimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
public class Test {

    public static void main(String[] args) {
        String str = "2022-12-05";
        int i = "2022-12-06".compareTo(str);
        System.out.println(i);

        ArrayList arrayList = new ArrayList();
        arrayList.add("BOB");

        Map<String, List<String>> thisMap= new HashMap<>();
        thisMap.put("name", Arrays.asList("HAHA", "BOBO", "LUCY"));
        arrayList.addAll(thisMap.get("name"));

        System.out.println(arrayList);

        String inputTime = "2023-10-29";
//        String inputTime = "2023-10-29 23:59:59";
        if (StringUtils.isNotEmpty(inputTime) && inputTime.length() >= 10) {
            inputTime = inputTime.substring(0, 10);
            System.out.println("inputTime: " + inputTime);
            SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd");

            Date yyyyMMdd = null;
            try {
                yyyyMMdd = inputFormatter.parse(inputTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String yyyyMMddString = new SimpleDateFormat("yyyyMMdd").format(yyyyMMdd);
            System.out.println(yyyyMMddString);


            String yyyy_MM_ddString = new SimpleDateFormat("yyyy-MM-dd").format(yyyyMMdd);
            System.out.println(yyyy_MM_ddString);


            String yyyyMMddhhmmssString = new SimpleDateFormat("yyyyMMddHHmmss").format(yyyyMMdd);
            System.out.println(yyyyMMddhhmmssString);
        }

    }
}
