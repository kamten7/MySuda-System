package com.suda.task;


import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

//@Component
@Slf4j
public class MyTask {

    /**
     * Spring Task 定时任务
     * 定时任务 5秒执行一次
     */
    @Scheduled(cron="0/5 * * * * *")//秒 分 时 日 月 周
    public void task1() {
        log.info("定时任务开始执行:{}",new Date());
    }
}
