package com.stillcoolme.core;

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
    }
}
