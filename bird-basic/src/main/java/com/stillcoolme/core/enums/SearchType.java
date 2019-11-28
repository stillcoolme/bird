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

        String searchType = jsonObject.getString("type");
        System.out.println(searchType);
        // searchType = "SEARCH_KEY" 作为String 是不等于 SearchType.SEARCH_KEY的
        if(searchType.equals(SearchType.SEARCH_KEY)){
            System.out.println("ture");
        } else {
            System.out.println("false");
        }
    }
}
