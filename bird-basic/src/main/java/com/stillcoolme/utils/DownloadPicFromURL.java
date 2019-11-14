package com.stillcoolme.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * @author: stillcoolme
 * @date: 2019/9/20 10:56
 * @description:
 */
public class DownloadPicFromURL {
    public static void main(String[] args) {
        List<String> pic = FileUtils.readFile("C:\\Users\\zhangjianhua\\Desktop\\image\\pic.txt");
        // downloadPicture(pic, "C:\\Users\\zhangjianhua\\Desktop\\image\\picthTest");
        getPicture(pic, "C:\\Users\\zhangjianhua\\Desktop\\image\\picthTest\\失败");
    }

    //链接url下载图片
    private static void downloadPicture(List<String> urlList, String path) {
        URL url = null;
        for (int i = 0; i < urlList.size(); i++) {
            try {
                url = new URL(urlList.get(i));
                DataInputStream dataInputStream = new DataInputStream(url.openStream());

                FileOutputStream fileOutputStream = new FileOutputStream(new File(path + "\\" + UUID.randomUUID().toString() + ".jpg"));
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int length;

                while ((length = dataInputStream.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                fileOutputStream.write(output.toByteArray());
                dataInputStream.close();
                fileOutputStream.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //文件名保存图片
    private static void getPicture(List<String> urlList, String path) {
        URL url = null;
        for (int i = 0; i < urlList.size(); i++) {
            try {
                InputStream inputStream = new FileInputStream(urlList.get(i));

                FileOutputStream fileOutputStream = new FileOutputStream(new File(path + "\\" + UUID.randomUUID().toString() + ".jpg"));
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                fileOutputStream.write(output.toByteArray());
                inputStream.close();
                fileOutputStream.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
