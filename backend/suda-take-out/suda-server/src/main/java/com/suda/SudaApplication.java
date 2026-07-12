package com.suda;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j//开启日志注解，log.info("server started") 打印日志
@EnableCaching//开启缓存注解，@Cacheable("empCache") 缓存员工
@EnableScheduling//开启定时任务注解，@Scheduled(cron = "0 0 0 * * ?") 每天凌晨0点执行一次
public class SudaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SudaApplication.class, args);
        log.info("server started");
    }

    //cpolar 启动：cpolar http 8080
}
