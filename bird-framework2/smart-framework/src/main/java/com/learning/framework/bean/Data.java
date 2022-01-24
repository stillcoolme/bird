package com.learning.framework.bean;

/**
 * 返回数据对象
 */
public class Data {
    /**
     * 模型数据
     */
    private Object model;

    public Object getModel() {
        return model;
    }

    public Data(Object model) {

        this.model = model;
    }
}
