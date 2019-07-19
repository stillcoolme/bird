package com.stillcoolme.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: stillcoolme
 * Date: 2019/7/17 14:33
 * Description:
 */
public class JsonUtils {

    /**
     * List转成JSONArray
     * @param list
     * @return
     */
    public static JSONArray list2JsonArray(List list){
        JSONArray objects = JSONArray.parseArray(JSON.toJSONString(list));
        return objects;

    }

    public static void main(String[] args) {
        List<String> list = new ArrayList();
        list.add("haha");
        list.add("gaga");
        list2JsonArray(list);
    }
}
