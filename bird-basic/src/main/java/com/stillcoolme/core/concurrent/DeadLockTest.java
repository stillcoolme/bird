package com.stillcoolme.core.concurrent;

/**
 * 模拟死锁的实现
 */
public class DeadLockTest extends Thread{

    String first;
    String second;
    public DeadLockTest(String name, String first, String second){
        super(name);
        this.first = first;
        this.second = second;
    }

    public void run(){
        synchronized (first){
            System.out.printf(this.getName() + " obtian " + first + "\n");
            try{
                Thread.sleep(1000L);
                synchronized (second){
                    System.out.printf(this.getName() + " obtian " + second + "\n");
                }
            }catch (Exception e){

            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String lockA = "lockA";
        String lockB = "lockB";
        DeadLockTest thread1 = new DeadLockTest("Thread1", lockA, lockB);
        DeadLockTest thread2 = new DeadLockTest("Thread2", lockB, lockA);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

    }

}
