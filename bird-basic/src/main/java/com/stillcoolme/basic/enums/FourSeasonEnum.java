package com.stillcoolme.basic.enums;

/**
 * @author: stillcoolme
 * @date: 2019/8/27 8:46
 * @description:
 **/
public enum FourSeasonEnum {
    SPRING("spring"),
    SUMMER("summer"),
    AUTUMN("autumn"),
    WINTER("winter");

    private String name;

    FourSeasonEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static void main(String[] args) {
        System.out.println(FourSeasonEnum.SPRING.getName());
        FourSeasonEnum fourSeasonEnum = FourSeasonEnum.AUTUMN;
        if (fourSeasonEnum.equals(FourSeasonEnum.AUTUMN)) {
            System.out.println("equal");
        } else {
            System.out.println("not equal");
        }
    }
}
