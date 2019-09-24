package com.stillcoolme.framework.okhttp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;
import sun.misc.BASE64Encoder;

/**
 * @author: stillcoolme
 * @date: 2019/9/16 16:22
 * @description:
 */
public class OkHttpTest {

    public static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    // 测试 http://api.test.nemoface.com/api
    // https://api.cloud.deepglint.com/v2
    public static final String GL_URL = "http://api.test.nemoface.com/api";
    public static final String REPO_NAME = "3a66ba80-7c1d-4558-97b9-cdcb9e1e319a";

    OkHttpClient client = new OkHttpClient();

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("authkey", "dp-auth-v0")
                .addHeader("access_key", "0e8e3db4-ccb1-4dab-a0ff-a6aac52bba25")
                .addHeader("secret_key", "84f14e48-574c-4507-ad30-e83ff0a9b9c5")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    String getJson(String dbName, String comment) {
        return "{\n"
                + "\"RepoName\": \"" +  dbName + "\",\n"
                + "\t\"Capacity\": 100,\n"
                + "\t\"Comment\": \"" + comment + "\"\n"
                + "}";
    }

    // 92b5de6c-b7bf-4406-843c-72573745fe5f
    @Test
    public void addRepo() throws IOException {
        OkHttpTest example = new OkHttpTest();
        String json = example.getJson("grg-face-test", "grg测试库");
        String response = example.post(GL_URL + "/repo/add", json);
        JSONObject jsonObject = prettyPrint(response);
        String repoID = (String) ((JSONObject) jsonObject.get("Data")).get("RepoID");
        System.out.println(repoID);
    }

    public String addRepo(String repoName) throws IOException {
        OkHttpTest example = new OkHttpTest();
        String json = example.getJson(repoName, "grg测试库");
        String response = example.post(GL_URL + "/repo/add", json);
        JSONObject jsonObject = prettyPrint(response);
        String repoID = (String) ((JSONObject) jsonObject.get("Data")).get("RepoID");
        return repoID;
    }

    private JSONObject prettyPrint(String response) {
        JSONObject jsonObject = JSONObject.parseObject(response);
        String responseJson = JSON.toJSONString(jsonObject, true);
        System.out.println(responseJson);
        return jsonObject;
    }

    @Test
    public void addPersonNoAddRepo() throws IOException {
        String repoId = "3a66ba80-7c1d-4558-97b9-cdcb9e1e319a";

        OkHttpTest example = new OkHttpTest();
        List<String> filePath = FileUtils.getAllFilePath("C:\\Users\\zhangjianhua\\Desktop\\image\\丁坠22");
//        List<String> filePath = new ArrayList<>();
//        filePath.add("C:\\Users\\zhangjianhua\\Desktop\\提取失败\\侧脸数据\\26742a9c1c9e450097c88728431912b4_face.jpg");
        for (int i = 0; i < filePath.size(); i++) {
            String bindata = imageToBase64ByLocal(filePath.get(i));

            String json = addPersonJson(repoId, UUID.randomUUID().toString(), bindata);

            // System.out.println(json);
            String response = example.post(GL_URL + "/register/add", json);
            JSONObject jsonObject = prettyPrint(response);
            if(! jsonObject.get("Msg").equals("success")) {
                System.out.println("!!!!!!!!! 添加失败：" + jsonObject.get("Msg"));
                System.out.println(filePath.get(i));
            } else {
                System.out.println("!!!!!!!!! 添加成功：" + filePath.get(i));
            }
        }

    }

    @Test
    public void addPerson() throws IOException {
        String repoId = addRepo("grg-face-test-archive");

        OkHttpTest example = new OkHttpTest();
        List<String> filePath = FileUtils.getAllFilePath("C:\\Users\\zhangjianhua\\Desktop\\image\\丁乜堆47");
//        List<String> filePath = new ArrayList<>();
//        filePath.add("C:\\Users\\zhangjianhua\\Desktop\\提取失败\\侧脸数据\\26742a9c1c9e450097c88728431912b4_face.jpg");
        for (int i = 0; i < filePath.size(); i++) {
            String bindata = imageToBase64ByLocal(filePath.get(i));

            String json = addPersonJson(repoId, UUID.randomUUID().toString(), bindata);

            // System.out.println(json);
            String response = example.post(GL_URL + "/register/add", json);
            JSONObject jsonObject = prettyPrint(response);
            if(! jsonObject.get("Msg").equals("success")) {
                System.out.println("!!!!!!!!! 添加失败：" + jsonObject.get("Msg"));
                System.out.println("!!!!!!!!! 添加失败：" + filePath.get(i));
            } else {
                System.out.println("!!!!!!!!! 添加成功：" + filePath.get(i));
            }
        }

    }

    public String addPersonJson(String repoId, String name, String bindata) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("RepoID", repoId);
        jsonObject.put("Name", name);
        JSONArray images = new JSONArray();
        JSONObject bindataJson = new JSONObject();
        bindataJson.put("BinData", bindata);
        images.add(bindataJson);
        jsonObject.put("Images", images);
        return jsonObject.toJSONString();
    }

    @Test
    public void search() throws IOException {
        OkHttpTest example = new OkHttpTest();
        List<String> filePath = new ArrayList<>();
        filePath = FileUtils.getAllFilePath("C:\\Users\\zhangjianhua\\Desktop\\image\\丁乜堆47");
        //filePath.add("C:\\Users\\zhangjianhua\\Desktop\\image\\picthTestMore\\d5dbfd26-d7fc-49cb-bd44-b6356373cc87.jpg");
        int count = 1;
        for (int i = 0; i < filePath.size(); i++) {
            String bindata = imageToBase64ByLocal(filePath.get(i));
            String json = searchRepoJson(Arrays.asList(new String[]{REPO_NAME}), bindata, 100, 0.5f);
            //System.out.println(json);
            String response = example.post(GL_URL + "/repo/search", json);
            JSONObject jsonObject = prettyPrint(response);
            if(! jsonObject.get("Msg").equals("success")) {
                System.out.println(filePath.get(i));
                String responseJson = JSON.toJSONString(jsonObject, true);
                System.out.println(" ::: " + responseJson);
                System.out.println(count ++);
            }
        }
    }

    private static String searchRepoJson(List<String> repoIds, String bindata, int topN, float score) {
        JSONObject jsonObject = new JSONObject();
        JSONArray objects = JSONArray.parseArray(JSON.toJSONString(repoIds));
        jsonObject.put("RepoIDs", objects);

        JSONObject bindataJson = new JSONObject();
        bindataJson.put("BinData", bindata);
        jsonObject.put("Image", bindataJson);

        jsonObject.put("TopN", topN);
        jsonObject.put("Confidence", score);
        return jsonObject.toJSONString();
    }

    /**
     *
     * @param imgFile
     * @return
     */
    public static String imageToBase64ByLocal(String imgFile) {
        InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();

        return encoder.encode(data);// 返回Base64编码过的字节数组字符串
    }

}
