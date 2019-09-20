package com.stillcoolme.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    public static void main(String[] args) {


    }
}
