package com.stillcoolme.basic.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


/**
 * @author: stillcoolme
 * @date: 2020/3/9 10:14
 * Function:
 *  https://blog.csdn.net/moakun/article/details/80552626
 */
public class FileCommonUtils {

    public static void main(String[] args) {

        String fileName = "C://11.txt";
        File file = new File(fileName);
        String fileContent = "";
        try {
            fileContent = FileUtils.readFileToString(file, "GBK");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileContent += "Helloworld";
        try {
            FileUtils.writeStringToFile(file, fileContent, "GBK");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
