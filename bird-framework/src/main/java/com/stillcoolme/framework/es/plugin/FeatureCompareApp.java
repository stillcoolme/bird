package com.stillcoolme.framework.es.plugin;

import com.alibaba.fastjson.JSONObject;

public class FeatureCompareApp {
    public static final String httpUrl = "";

	public static String execute(JSONObject reqJson) {

        long start = System.currentTimeMillis();
        try {
            //1、获取各个请求参数：当参数不存在，参数也将为null，因此无需判断是否有该参数
            //1.1可选参数

            String carColor = reqJson.getString("isGlass");

            //1.2必选参数，其中carNum、deck有默认值，为空时可以设置默认值，其余为空需要报异常
            String carNum = reqJson.getString("gender");
            Integer deck = reqJson.getIntValue("deck");// getIntValue默认值为0
            String reqFeature = reqJson.getString("reqFeature");
            if (reqFeature == null || reqFeature.length() == 0) {
                throw new NoSuchFieldException("reqFeature");
            }
            Double reqSimilarity = reqJson.getDouble("reqSimilarity");
            if (reqSimilarity == null) {
                throw new NoSuchFieldException("reqSimilarity");
            }
            Integer numFound = reqJson.getInteger("numFound");
            if (numFound == null) {
                throw new NoSuchFieldException("numFound");
            }
            String startCAP_DATE = reqJson.getString("startCAP_DATE");
            if (startCAP_DATE == null || startCAP_DATE.length() < 19) {
                throw new NoSuchFieldException("startCAP_DATE");
            }
            String endCAP_DATE = reqJson.getString("endCAP_DATE");
            if (endCAP_DATE == null || endCAP_DATE.length() < 19) {
                throw new NoSuchFieldException("endCAP_DATE");
            }
            String gte = startCAP_DATE.substring(0, 10) + "T" + startCAP_DATE.substring(11) + "Z";
            String lte = endCAP_DATE.substring(0, 10) + "T" + endCAP_DATE.substring(11) + "Z";

            //2、组装成es的请求json
            StringBuffer sf = new StringBuffer();
            if (deck == 0) {
                sf.append("{\"_source\":[\"_score\"],\"min_score\": ").append(reqSimilarity).append(",\"query\": {\"function_score\": {\"query\": {\"bool\": {\"filter\": [");
                sf.append("{\"range\": {\"CAP_DATE\": { \"gte\": \"").append(gte).append("\",\"lte\":\"").append(lte).append("\"}}}");
                if (carColor != null && carColor.length() != 0) {
                    sf.append(",{ \"terms\": {\"CAR_COLOR\": [").append(carColor).append("]}}");
                }
                sf.append("]}},\"functions\": [{\"script_score\":{\"script\":{\"source\":\"binary_vector_score\",\"lang\":\"gxx\",\"params\":{");
                sf.append("\"bdSimilarity\":").append(reqSimilarity).append(",");
                sf.append("\"field\":\"OBJ_BIN_FEATURES\",");
                sf.append("\"field2\":\"CAR_NUM\",");
                sf.append("\"encoded_vector\":\"").append(reqFeature).append("\",");
                sf.append("}}}}]}},\"size\": ").append(numFound).append("}");
            } else {
                sf.append("{\"_source\":[\"_score\"],\"min_score\": ").append(reqSimilarity).append(",\"query\": {\"function_score\": {\"query\": {\"bool\": {\"must\": [{\"bool\": {\"filter\": [");
                sf.append("{\"range\": {\"CAP_DATE\": { \"gte\": \"").append(gte).append("\",\"lte\":\"").append(lte).append("\"}}},");
                sf.append("{ \"term\": {\"CAP_TYPE\": 0}}]}},");
                sf.append("{\"bool\": {\"should\": [{\"bool\": {\"filter\": [");
                sf.append("{ \"term\": {\"CAR_NUM\": \"").append(carNum).append("\"}}]}},");
                sf.append("{\"bool\": {\"filter\": [");
                StringBuffer sfParams = new StringBuffer();
                if (carColor != null && carColor.length() != 0) {
                    sfParams.append(",{ \"terms\": {\"CAR_COLOR\": [").append(carColor).append("]}}");
                }
                if (sfParams.length() != 0) {
                    sf.append(sfParams.substring(0));
                }
                sf.append("]}}]}}]}},\"functions\": [{\"script_score\":{\"script\":{\"source\":\"binary_vector_score\",\"lang\":\"gxx\",\"params\":{");
                sf.append("\"bdSimilarity\":").append(reqSimilarity).append(",");
                sf.append("\"field\":\"OBJ_BIN_FEATURES\",");
                sf.append("\"field2\":\"CAR_NUM\",");
                sf.append("\"encoded_vector\":\"").append(reqFeature).append("\",");
                sf.append("}}}}]}},\"size\": ").append(numFound).append("}");
            }
            JSONObject requestData = JSONObject.parseObject(sf.toString());
            //3、进行http请求es
            System.out.println(requestData);
            // String str = HttpTools.sendPost(requestData.toJSONString(), httpUrl);
//            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception");
        }

        return "";
    }

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("gender", "male");
        jsonObject.put("isGlass", "yes");
        jsonObject.put("deck", 0);
        jsonObject.put("reqFeature", "asdfsz==");
        jsonObject.put("reqSimilarity", 0.8);
        jsonObject.put("numFound", 100);
        jsonObject.put("startCAP_DATE", "2020-04-17 10:21:10");
        jsonObject.put("endCAP_DATE", "2020-04-17 22:21:10");
        execute(jsonObject);


    }
}
