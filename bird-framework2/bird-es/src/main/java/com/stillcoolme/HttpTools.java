package com.stillcoolme;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/*
* http 工具类
*/
public class HttpTools {
    /*
     *  用post方式发送json请求到指定url
     */
    public static String sendJsonPost(String url,String json_str) {
        CloseableHttpClient client=null;
        String returnResult =null;
        try {
            // HTTP POST请求
            client =  HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            //添加请求头
            StringEntity requestEntity = new StringEntity(json_str, "utf-8");
            requestEntity.setContentEncoding("UTF-8");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(requestEntity);
            returnResult = client.execute(httpPost, responseHandler);
        }catch (Exception e){
            e.printStackTrace();
            returnResult =null;
        }finally {
            if(client!=null) {
                try {client.close();} catch (IOException e) {e.printStackTrace();}
            }
        }
        return returnResult;
    }

    public static void main(String[] args){
        String url = "http://68.32.128.15:8088/g1/M00/000003E8/20190109/RCCAD1w1aMOITpGDAAFGRnDMt08AD1_qAJxT1UAAUZe171.jpg";
        String file= url.substring(url.lastIndexOf("/")+1,url.length());
        String url1 =url.replaceAll("/"+file, "");
        String date= url1.substring(url1.lastIndexOf("/")+1,url1.length());
        System.out.println(url1);
        System.out.println(date);
        System.out.println(url1+"/"+date.substring(0,4)+"/"+date.substring(4,6)+"/"+date.substring(6,8)+"/"+file);
    }

   //gl
    public static String sendPost(String sendMsg, String sendUrl) {
        HttpPost httpPost = new HttpPost(sendUrl);
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
        StringEntity entity;
        String resData = null;
        try {
            entity = new StringEntity(sendMsg, "UTF-8"); //解决参数中文乱码问题
            entity.setContentEncoding("UTF-8");//设置编码格式
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(60000)// 连接主机服务超时时间
                    .setConnectionRequestTimeout(60000)// 请求超时时间
                    .setSocketTimeout(60000)// 数据读取超时时间
                    .build();
            httpPost.setConfig(requestConfig);
            // 发起请求
            HttpResponse response = closeableHttpClient.execute(httpPost);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                // 请求结束，返回结果。并解析json。
                resData = EntityUtils.toString(response.getEntity(),"UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != closeableHttpClient) {
                try {
                    closeableHttpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resData;
    }

}
