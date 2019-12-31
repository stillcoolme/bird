package com.stillcoolme.framework.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * @author: stillcoolme
 * @date: 2019/12/31 10:43
 * @description:
 * https://www.throwable.club/2018/12/16/zookeeper-curator-usage/
 */
@Slf4j
public class CuratorUtils {

    /**
     * 创建会话
     * @param connectionString
     * @param namespace
     * @return
     */
    public static CuratorFramework buildClient(String connectionString, String namespace) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder
                .connectString(connectionString)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy);
        //命令空间
        if (namespace != null && !namespace.isEmpty()) {
            builder.namespace(namespace);
        }
        CuratorFramework client = builder.build();
        client.start();
        return client;
    }

    /**
     * 创建Node
     *
     * @return path
     * @throws Exception
     */
    public static String createNode(CuratorFramework client, String path, CreateMode createMode) throws Exception {
        if (checkPathExists(client, path)) {
            return path;
        }

        log.info("create znode, Path:{}, CreateMode:{}", path, createMode);
        String createdPath = client.create()
                .creatingParentsIfNeeded()
//                .withMode(CreateMode.PERSISTENT)
//                .withMode(CreateMode.EPHEMERAL)
                .withMode(createMode)
                .forPath(path);

        return createdPath;
    }

    /**
     * 检查Znode是否存在
     *
     * @param client
     * @param path
     * @return 存在:true, 不存在:false
     * @throws Exception
     */
    public static boolean checkPathExists(CuratorFramework client, String path) throws Exception {
        Stat stat = client.checkExists()
                .forPath(path);

        return stat != null;
    }

    /**
     * 检查ZNode是否存在, 不存在则创建
     *
     * @param zk
     * @param path
     * @param createMode
     * @return [创建成功:true; 创建失败:false]
     */
    public static boolean checkAndCreateNode(CuratorFramework zk, String path, CreateMode createMode) {
        try {
            //创建节点
            if (!CuratorUtils.checkPathExists(zk, path)) {
                CuratorUtils.createNode(zk, path, createMode);
                return true;
            } else {
                log.info("Zk path Already Exists:{}", path);
            }
        } catch (Exception e) {
            log.error("checkAndCreateNode 失败", e);
        }

        return false;
    }

    /**
     * 锁住并创建ZNode
     *
     * @param zk
     * @param lockPath
     * @param createPath
     * @throws Exception
     */
    public static void lockAndCreateNode(CuratorFramework zk, String lockPath, String createPath) throws Exception {
        lockAndCreateNode(zk, lockPath, createPath, null);
    }

    /**
     * 锁住并创建ZNode
     * @param zk
     * @param lockPath
     * @param createPath
     * @param data
     * @return [创建并写入成功:true; else:false]
     * @throws Exception
     */
    public static boolean lockAndCreateNode(CuratorFramework zk, String lockPath, String createPath, byte[] data) throws Exception {
        // 不可重入分布式锁
        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(zk, lockPath);
        lock.acquire();

        try {
            boolean created = checkAndCreateNode(zk, createPath, CreateMode.EPHEMERAL);
            if (created && data != null) {
                writeToPath(zk, createPath, data);
                return true;
            }
        } catch (Exception e) {
            log.error("lockAndCreateNode 失败", e);
        } finally {
            lock.release();
        }

        return false;
    }

    /**
     * 写入数据
     *
     * @param client
     * @param path
     * @param bytes
     * @throws Exception
     */
    public static void writeToPath(CuratorFramework client, String path, byte[] bytes) throws Exception {
//        create(client, path, CreateMode.PERSISTENT);

        log.info("写入数据到ZNode, path:{}, byte长度:{}", path, bytes.length);
        client.setData()
                .forPath(path, bytes);
    }

    /**
     * 读取数据
     *
     * @param client
     * @param path
     * @return
     * @throws Exception
     */
    public static byte[] readFromPath(CuratorFramework client, String path) throws Exception {
        byte[] bytes = client.getData()
                .forPath(path);

        return bytes;
    }

    /**
     * 删除节点数据
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void deletePath(CuratorFramework client, String path) throws Exception {
        log.info("delete ZNode, Path:{}", path);

        client.delete()
                .deletingChildrenIfNeeded()
                .forPath(path);
    }

    /**
     * 检查节点是否存在, 若存在则删除
     * @param client
     * @param path
     * @throws Exception
     */
    public static void checkAndDeletePath(CuratorFramework client, String path) throws  Exception{
        boolean exists = checkPathExists(client, path);
        if (exists) {
            log.info("checkAndDelete - Path存在, 正在删除");
            deletePath(client, path);
        }else{
            log.info("checkAndDelete - Path不存在, 无需删除");
        }
    }

    //--------------------NodeCache 监听器--------------------

    /**
     * 添加NodeCache监听器
     *
     * @param client
     * @param path
     * @throws Exception
     */
    public static void addNodeCacheListener(CuratorFramework client, String path, NodeCacheListener nodeCacheListener) throws Exception {
        if (nodeCacheListener == null) {
            addNodeCacheListener(client, path);
        } else {
            NodeCache nodeCache = new NodeCache(client, path);
            nodeCache.getListenable().addListener(nodeCacheListener);
            nodeCache.start();
        }
    }

    /**
     * 添加NodeCache监听器
     *
     * @param nodeCache
     * @param nodeCacheListener
     * @throws Exception
     */
    public static void addNodeCacheListener(NodeCache nodeCache, NodeCacheListener nodeCacheListener) throws Exception {
        nodeCache.getListenable().addListener(nodeCacheListener);
        nodeCache.start();
    }

    /**
     * 添加NodeCache监听器
     *
     * @param client
     * @param path
     */
    public static NodeCache addNodeCacheListener(CuratorFramework client, String path) throws Exception {
        log.info("addNodeCacheListener, Path:{}", path);
        final NodeCache nodeCache = new NodeCache(client, path);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                // do something
            }
        });

        nodeCache.start();
        return nodeCache;
    }

    //--------------------ConnectionStateListener 监听器--------------------

    /**
     * 添加 连接状态监听器
     *
     * @param client
     * @param stateListener
     */
    public static void addConnectionStateListener(CuratorFramework client, ConnectionStateListener stateListener) {
        log.info("addConnectionStateListener");
        client.getConnectionStateListenable().addListener(stateListener);
    }
}
