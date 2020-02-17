package com.stillcoolme.basic.io.mmap;

import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Author: stillcoolme
 * Date: 2019/9/18 15:00
 * Description:
 */
public class MmapTest {

    @Test
    public void test1() throws Exception {
        File file = new File("E://mmap");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
        MappedByteBuffer mappedByteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 1024L * 1024L);
        mappedByteBuffer.put("12345678".getBytes());
        mappedByteBuffer.putLong(1L);
        // 指定读取数据的起始位置
        mappedByteBuffer.position(0);
        byte[] content = new byte[8];
        mappedByteBuffer.get(content);
        System.out.println(new String(content));
    }

    @Test
    public void test2() throws Exception {
        File file = new File("E://kiritoDB_index");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
        MappedByteBuffer mappedByteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 1024L * 1024L);
        byte[] content = new byte[8];
        mappedByteBuffer.get(content);
        long aLong = mappedByteBuffer.getLong();
        System.out.println(new String(content));
        System.out.println(aLong);
        mappedByteBuffer.get(content);
        aLong = mappedByteBuffer.getLong();
        System.out.println(new String(content));
        System.out.println(aLong);

    }

}
