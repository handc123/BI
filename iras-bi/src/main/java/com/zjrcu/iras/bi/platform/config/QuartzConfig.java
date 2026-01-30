package com.zjrcu.iras.bi.platform.config;

import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Quartz调度器配置
 *
 * @author iras
 */
@Configuration
public class QuartzConfig {

    /**
     * 配置SchedulerFactoryBean
     *
     * @return SchedulerFactoryBean
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setStartupDelay(10); // 延迟10秒启动
        return factory;
    }

    /**
     * 配置Scheduler
     *
     * @param schedulerFactoryBean SchedulerFactoryBean
     * @return Scheduler
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getScheduler();
    }
}
