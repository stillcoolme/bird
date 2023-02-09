package com.stillcoolme.framework.scheduler;


import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * https://michael728.github.io/2020/09/06/java-middleware-quartz-basic/
 * 学习一下人家源码的写法
 */
public class QuartzDemo {

    public static void main(String[] args) throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        // 创建调度器实例Scheduler
        Scheduler scheduler = sf.getScheduler();
        // 定义任务
        JobDetail jobDetail = JobBuilder.newJob(RamJob.class)
                .withIdentity("detail01", "group01")  //组成唯一标识
                .withDescription("this is a jobDetail")
                .usingJobData("level", "老手")  //  向具体执行的作业类传值方式一
                .build();

        //向具体执行的作业类传值方式二
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put("name", "张三");
        jobDataMap.put("job", "司机");
        // 定义触发器
        Date time = new Date(System.currentTimeMillis() + 6 * 1000L);
        Trigger trigger = (Trigger) TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group01")   //组成唯一标识
                .startAt(time)             //开始时间
                .withDescription("this is a trigger")
//               .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//               .withIntervalInSeconds(2).withRepeatCount(3))
//                表达式调度器，每5秒执行一次作业类逻辑代码
                .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ? "))
                .build();

        // 注入作业类以及触发器到调度器中
        scheduler.scheduleJob(jobDetail, trigger);
        // 启动调度器，开启一个线程
        scheduler.start();
        // 执行任务调度的核心类是 QuartzSchedulerThread

    }


}
