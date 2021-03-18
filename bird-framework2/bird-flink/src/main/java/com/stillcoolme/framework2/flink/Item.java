package com.stillcoolme.framework2.flink;

public class Item {
    String name;
    Integer id;

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}