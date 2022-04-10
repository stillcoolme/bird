package com.stillcoolme.framework.scheduler;

import org.quartz.*;

public class RamJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("定时任务执行......");
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        System.out.println("定时任务参数：name->"+jobDataMap.get("name")+",job->"
                +jobDataMap.get("job")+",level->"+jobDataMap.get("level"));
    }
}