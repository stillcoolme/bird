package com.stillcoolme.framework.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.CountDownLatch;

/**
 * @author: stillcoolme
 * @date: 2020/3/5 11:18
 * Function:
 *   思路1： 非公平锁
 *      某个节点尝试创建  临时 znode，此时创建成功了就获取了这个锁；
 *      这个时候别的客户端来创建锁会失败，会注册监听器来监听这个锁。
 *      释放锁就是删除这个 znode，一旦释放掉就会通知客户端，然后有一个等待着的客户端就可以再次重新加锁。
 *   测试：分别启动 ZKDistributeLock 和 ZKDistributeLock2
 *
 *   思路2： 公平锁
 *      创建带顺序的zk节点来锁
 */
@Slf4j
public class ZKDistributeLock {

    CuratorFramework curatorClient = CuratorUtils.getClient();

    public boolean tryLock(String key) {
        // 创建临时节点，创建成功则获得锁
        if (CuratorUtils.checkAndCreateNode(curatorClient, "/" + key, CreateMode.EPHEMERAL)) {
            log.info("get the lock for product[id={}]......", key);
            return true;
        } else {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                PathChildrenCache pathCache = new PathChildrenCache(curatorClient, "/", true);
                pathCache.getListenable()
                        .addListener(new NodeExistListener(countDownLatch));
                pathCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    System.out.println("等待锁释放");
                    countDownLatch.await();
                    if(CuratorUtils.checkAndCreateNode(curatorClient, "/" + key, CreateMode.EPHEMERAL)) {
                        log.info("get the lock for product[id={}]......", key);
                        countDownLatch = new CountDownLatch(1);
                        return true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // 解锁，删除临时节点
    public void unlock(String key) {
        try {
            CuratorUtils.checkAndDeletePath(curatorClient, "/" + key);
            log.info("get the lock for product[id={}]......", key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class NodeExistListener implements PathChildrenCacheListener {

        CountDownLatch countDownLatch;

        public NodeExistListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
            ChildData data = event.getData();
            switch (event.getType()) {
                case CHILD_ADDED:
                    log.info("子节点增加, path={}, data={}",
                            data.getPath(), new String(data.getData(), "UTF-8"));
                    break;
                case CHILD_UPDATED:
                    log.info("子节点更新, path={}, data={}",
                            data.getPath(), new String(data.getData(), "UTF-8"));
                    break;
                case CHILD_REMOVED:
                    countDownLatch.countDown();
                    log.info("子节点删除, path={}, data={}",
                            data.getPath(), new String(data.getData(), "UTF-8"));
                    break;
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) {
        ZKDistributeLock zkDistributeLock = new ZKDistributeLock();
        zkDistributeLock.tryLock("zjh");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        zkDistributeLock.unlock("zjh");
        while (true) {

        }
    }

}
