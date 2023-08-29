package com.yenanren.socket_kafka.config;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TaskSchedulerConfig {
    private static ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    static {
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("task-scheduler-");
        scheduler.initialize();
    }

    public static ThreadPoolTaskScheduler taskScheduler() {
        return scheduler;
    }

}
