package com.stillcoolme.basic.concurrent.countdown;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: stillcoolme
 * @date: 2019/9/6 13:49
 * @description:
 *
 * 在多线程协作完成业务功能时，有时候需要等待其他多个线程完成任务之后，主线程才能继续往下执行业务功能。
 * 在这种的业务场景下，
 * 1. 通常可以使用Thread类的join方法，让主线程等待被join的线程执行完之后，主线程才能继续往下执行。
 * 2. 使用线程间消息通信机制也可以完成。
 * 3. java并发工具类中为我们提供了类似“倒计时”这样的工具类，可以十分方便的完成所说的这种业务场景。
 *
 * 为了能够理解CountDownLatch，举一个很通俗的例子：
 * 假设有6个运动员参与跑步比赛，裁判员在终点会为这6个运动员分别计时，可以想象每当一个运动员到达终点的时候，对于裁判员来说就少了一个计时任务。
 * 直到所有运动员都到达终点了，裁判员的任务也才完成。
 * 这6个运动员可以类比成6个线程，当线程调用CountDownLatch.countDown方法时就会对计数器的值减一，直到计数器的值为0的时候，裁判员（调用await方法的线程）才能继续往下执行。
 *
 * CountDownLatch的方法不是很多，将一个个列举出来：
 * 1. await() throws InterruptedException：调用该方法的线程等到构造方法传入的N减到0的时候，才能继续往下执行；
 * 2. await(long timeout, TimeUnit unit)：与上面的await方法功能一致，只不过有了时间限制，调用该方法的线程等到指定的timeout时间后，不管N是否减至为0，都会继续往下执行；
 * 3. countDown()：使CountDownLatch初始值N减1；
 * 3. long getCount()：获取当前CountDownLatch维护的值；
 *
 */
public class CountDownLatchDemo_RunGame {
    // 用来表示起跑枪响
    private static CountDownLatch startSignal = new CountDownLatch(3);
    // 用来表示裁判员需要维护的是6个运动员
    private static CountDownLatch endSignal = new CountDownLatch(6);

    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 6; i++) {
            executorService.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " 运动员等待裁判员响哨！！！");
                    startSignal.await();
                    System.out.println(Thread.currentThread().getName() + "正在全力冲刺");
                    endSignal.countDown();
                    System.out.println(Thread.currentThread().getName() + "  到达终点");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        System.out.println("裁判员发号施令啦！！！");
        for (int i = 3; i > 0; i--) {
            System.out.println("裁判员起跑倒计时： " + i);
            if(i != 1) {
                startSignal.countDown();
            } else {
                System.out.println("跑！！！！！！！！！！！！！！！");
                startSignal.countDown();
            }
        }
        endSignal.await();
        System.out.println("所有运动员到达终点，比赛结束！");
        executorService.shutdown();

    }
}
