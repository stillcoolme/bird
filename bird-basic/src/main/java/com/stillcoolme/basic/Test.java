package com.stillcoolme.basic;

import java.util.ArrayList;

/**
 * @author: stillcoolme
 * @date: 2019/11/26 9:45
 * @description:
 */
public class Test {

    public static void main(String[] args) {
        int[] test = new int[0];

        String realLabelId = "bcard_gen_cust_app_2011";
        System.out.println("xx: " + realLabelId.contains("bcard_gen_cust"));

        String labelValue = "569.0";
        String newLabelValue = labelValue.substring(labelValue.length() - 2);
        System.out.println(".0".equals(newLabelValue));


        String loan_core = "1900-01-01 24:49:29";
        if (loan_core.startsWith("1900-01-01")) {
            System.out.println(loan_core.substring(0, 10));
        } else {
            System.out.println("no");
        }
        ArrayList arrayList = new ArrayList();
        arrayList.toArray();

        System.out.println("".split("\\(")[0]);
    }
}
