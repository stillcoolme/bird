package com.stillcoolme.basic.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stillcoolme.basic.bean.Person;

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

    /**
     * JSONArray转List
     * @return
     */
    public static List jsonArray2List(JSONArray jsonArray){
        String arrString = jsonArray.toJSONString();
        List<Person> list = JSONObject.parseArray(arrString, Person.class);
        return list;
    }

    /**
     * 格式化打印
     * @param response
     * @return
     */
    private JSONObject prettyPrint(String response) {
        JSONObject jsonObject = JSONObject.parseObject(response);
        String responseJson = JSON.toJSONString(jsonObject, true);
        System.out.println(responseJson);
        return jsonObject;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList();
        list.add("haha");
        list.add("gaga");
        list2JsonArray(list);
    }
}
