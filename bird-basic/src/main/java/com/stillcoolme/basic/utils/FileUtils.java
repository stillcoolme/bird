package com.stillcoolme.basic.utils;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author: stillcoolme
 * @date: 2019/9/19 15:36
 * @description:
 */
public class FileUtils {

    /**
     * 读取文件夹下的文件名，得到文件名全路径列表
     * @param path
     * @return
     */
    public static List<String> getAllFilePath(String path) {
        List filePathList = new ArrayList();
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    System.out.println("文件夹:" + file2.getAbsolutePath());
                    list.add(file2);
                    folderNum ++;
                } else {
                    // System.out.println("文件:" + file2.getAbsolutePath());
                    filePathList.add(file2.getAbsolutePath());
                    fileNum++;
                }
            }
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        //System.out.println("文件夹:" + file2.getAbsolutePath());
                        list.add(file2);
                        folderNum++;
                    } else {
                        //System.out.println("文件:" + file2.getAbsolutePath());
                        filePathList.add(file2.getAbsolutePath());
                        fileNum++;
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        System.out.println("文件夹共有:" + folderNum + ",文件共有:" + fileNum);
        return filePathList;
    }

    /**
     *
     * Reader/Writer与InputStream/OutputStream的区别
     * 1. 主要区别在于被读和被写入的基本数据类型；InputStream OutputStream 是面向字节的，Reader Writer 使用的字符和字符串。
     * 2. 大多数Reader 有一个构造函数的InputStream作为参数，并且大多数Writer 有一个构造函数这需要一个OutputStream作为参数。
     * 3. InputStreamReader、OutputStreamWriter 负责进行 InputStream 到 Reader 的适配和由 OutputStream 到 Writer 的适配。
     * @param fileName
     * @return
     */
    public static List<String> readFile(String fileName) {
        List<String> list = new ArrayList<>();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;  //用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
        try {
            String str = "";
            String str1 = "";
            fis = new FileInputStream(fileName);// FileInputStream
            // 从文件系统中的某个文件中获取字节
            isr = new InputStreamReader(fis);   // InputStreamReader 是字节流通向字符流的桥梁 ！！！
            br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
            while ((str = br.readLine()) != null) {
                list.add(str);
                str1 += str + "\n";
            }
        } catch (FileNotFoundException e) {
            System.out.println("找不到指定文件");
        } catch (IOException e) {
            System.out.println("读取文件失败");
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
                // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    public static void main(String[] args) {
        String dbName = "dd";
        String reqBody = "{\n" +
                "\t\"method\": \"searchImagesInRemoteDB\",\n" +
                "\t\"dbName\": \"" + dbName + "\",\n" +
                "}";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", null);
        String key2 = Optional.ofNullable(jsonObject.getString("key")).orElse("_))");
        key2 = key2.length() == 0 ? "history" : key2;
        System.out.println(key2);
    }
}
