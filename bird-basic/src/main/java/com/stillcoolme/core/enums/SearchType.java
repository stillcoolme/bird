package com.stillcoolme.core.enums;

import com.alibaba.fastjson.JSONObject;

/**
 * 用于判断 searchCompare11InRemoteDb 接口，src字段传入的是 key，还是 feature
 */
public enum SearchType {

    SEARCH_KEY,
    SEARCH_FEATURE;

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", SearchType.SEARCH_KEY);

        System.out.println(jsonObject.get("type").toString());

        if(jsonObject.get("type").equals(SearchType.SEARCH_KEY)) {
            System.out.println("ture");
        }
    }
}
