package com.stillcoolme.basic.io.filechannel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author stillcoolme
 * @version 1.0.0
 * @createTime 2021-08-07 11:39:00
 * @Description
 * 原生的读写方式大概可以被分为三种：普通IO，FileChannel(文件通道)，MMAP(内存映射)。
 * FileChannel 存在于 java.nio 包中，属于 NIO 的一种， NIO 并不一定意味着非阻塞，这里的 FileChannel 就是阻塞的；
 *
 * FileChannel 只有在一次写入 4kb 的整数倍时，才能发挥出实际的性能，
 * 这得益于 FileChannel 采用了 ByteBuffer 这样的内存缓冲区，让我们可以非常精准的控制写盘的大小，这是普通 IO 无法实现的。
 * 4kb 一定快吗？
 * 也不严谨，这主要取决你机器的磁盘结构，并且受到操作系统，文件系统，CPU 的影响，例如中间件性能挑战赛时的那块盘，一次至少写入 64kb 才能发挥出最高的 IOPS。
 *
 * FileChannel 的高效，还由于 不直接把 ByteBuffer 中的数据写入到磁盘。
 * ByteBuffer中的数据和磁盘中的数据还隔了一层，这一层便是 PageCache，是用户内存和磁盘之间的一层缓存。
 * 我们都知道磁盘 IO 和内存 IO 的速度可是相差了好几个数量级。
 * 我们可以认为 filechannel.write 写入 PageCache 便是完成了落盘操作，但实际上，操作系统最终帮我们完成了 PageCache 到磁盘的最终写入，
 * 理解了这个概念，你就应该能够理解 FileChannel 为什么提供了一个 force() 方法，用于通知操作系统进行及时的刷盘。
 *
 * 同理，当我们使用 FileChannel 进行读操作时，同样经历了：磁盘->PageCache->用户内存这三个阶段。
 */
public class Test {

    public static void main(String[] args) throws IOException {

        URL resource = Test.class.getResource("/app.properties");
        System.out.println(resource.getPath());
        FileChannel fileChannel = new RandomAccessFile(resource.getPath(), "rw").getChannel();

        // 首先分配一个4k的缓冲区。 从FileChannel读取的数据被读入缓冲区。  bytebuffer相当于封装的 byte[]
        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);

        // 读取数据到Buffer中。 read（）方法返回的int告诉在缓冲区中有多少个字节。 如果返回-1，则达到文件结束。
        int readBytes = fileChannel.read(byteBuffer);

        long position = 1024L;
        //指定 position 读取 4kb 的数据
        int read = fileChannel.read(byteBuffer, position);



        ///////////// 写
        byte[] data = new byte[4096];
        //指定 position 写入 4kb 的数据
        fileChannel.write(ByteBuffer.wrap(data), position);
        //从当前文件指针的位置写入 4kb 的数据
        fileChannel.write(ByteBuffer.wrap(data));




    }
}
