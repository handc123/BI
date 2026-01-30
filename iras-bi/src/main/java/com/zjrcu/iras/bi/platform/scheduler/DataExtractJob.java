package com.zjrcu.iras.bi.platform.scheduler;

import com.zjrcu.iras.bi.platform.domain.dto.ExtractResult;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 数据抽取定时任务
 *
 * @author iras
 */
@Component
public class DataExtractJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(DataExtractJob.class);

    @Autowired
    private IDataExtractScheduler dataExtractScheduler;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long datasetId = dataMap.getLong("datasetId");

        log.info("开始执行数据抽取任务: datasetId={}", datasetId);

        try {
            ExtractResult result = dataExtractScheduler.executeExtract(datasetId);

            if (result.isSuccess()) {
                log.info("数据抽取任务执行成功: datasetId={}, rows={}, duration={}ms",
                        datasetId, result.getRowCount(), result.getExecutionTime());
            } else {
                log.error("数据抽取任务执行失败: datasetId={}, error={}",
                        datasetId, result.getErrorMessage());
                throw new JobExecutionException("数据抽取失败: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("数据抽取任务执行异常: datasetId={}, error={}", datasetId, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
