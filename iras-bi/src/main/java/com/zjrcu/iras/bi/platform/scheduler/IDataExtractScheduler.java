package com.zjrcu.iras.bi.platform.scheduler;

import com.zjrcu.iras.bi.platform.domain.dto.ExtractResult;

/**
 * 数据抽取调度器接口
 * 管理抽取数据集的定时任务
 *
 * @author iras
 */
public interface IDataExtractScheduler {

    /**
     * 调度抽取任务
     *
     * @param datasetId 数据集ID
     * @param cronExpression Cron表达式
     */
    void scheduleExtract(Long datasetId, String cronExpression);

    /**
     * 执行立即抽取
     *
     * @param datasetId 数据集ID
     * @return 抽取结果
     */
    ExtractResult executeExtract(Long datasetId);

    /**
     * 取消调度任务
     *
     * @param datasetId 数据集ID
     */
    void cancelSchedule(Long datasetId);

    /**
     * 检查任务是否存在
     *
     * @param datasetId 数据集ID
     * @return true表示任务已调度
     */
    boolean isScheduled(Long datasetId);
}
