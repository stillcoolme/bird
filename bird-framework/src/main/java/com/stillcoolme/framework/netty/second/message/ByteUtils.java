package com.stillcoolme.framework.netty.second.message;

import io.netty.buffer.ByteBuf;

import java.io.*;

/**
 * @author: stillcoolme
 * @date: 2019/10/24 15:28
 * @description:
 */
public class ByteUtils {

    /**
     * serialize
     * @param object
     * @return
     */
    public static byte[] objectToBytes(Object object) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oo = null;
        try{
            oo = new ObjectOutputStream(byteArrayOutputStream);
            oo.writeObject(object);
            bytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                oo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * deserialize
     * @param bytes
     * @return
     */
    public static Object bytesToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream oi = null;
        try {
            oi = new ObjectInputStream(bi);
            obj = oi.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                oi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    public static byte[] read(ByteBuf datas) {
        byte[] bytes = new byte[datas.readableBytes()];
        datas.readBytes(bytes);
        return bytes;
    }
}
