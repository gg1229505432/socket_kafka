package com.yenanren.socket_kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TaskSchedulerConfig {
    private static ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    public static ThreadPoolTaskScheduler taskScheduler() {
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("task-scheduler-");
        scheduler.initialize();

        return scheduler;
    }

}
