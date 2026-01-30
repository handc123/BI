package com.zjrcu.iras.bi.platform.scheduler;

import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.ExtractResult;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.bi.platform.scheduler.impl.DataExtractSchedulerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DataExtractScheduler单元测试
 *
 * @author iras
 */
class DataExtractSchedulerTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private DatasetMapper datasetMapper;

    @InjectMocks
    private DataExtractSchedulerImpl dataExtractScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试调度抽取任务 - 成功场景
     */
    @Test
    void testScheduleExtract_Success() throws SchedulerException {
        // Given
        Long datasetId = 1L;
        String cronExpression = "0 0 2 * * ?"; // 每天凌晨2点

        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // When
        dataExtractScheduler.scheduleExtract(datasetId, cronExpression);

        // Then
        verify(scheduler, times(1)).scheduleJob(any(), any());
    }

    /**
     * 测试调度抽取任务 - 数据集ID为空
     */
    @Test
    void testScheduleExtract_NullDatasetId() {
        // Given
        Long datasetId = null;
        String cronExpression = "0 0 2 * * ?";

        // When & Then
        assertThrows(Exception.class, () -> {
            dataExtractScheduler.scheduleExtract(datasetId, cronExpression);
        });
    }

    /**
     * 测试调度抽取任务 - 无效的Cron表达式
     */
    @Test
    void testScheduleExtract_InvalidCronExpression() {
        // Given
        Long datasetId = 1L;
        String cronExpression = "invalid cron";

        // When & Then
        assertThrows(Exception.class, () -> {
            dataExtractScheduler.scheduleExtract(datasetId, cronExpression);
        });
    }

    /**
     * 测试调度抽取任务 - 替换已存在的任务
     */
    @Test
    void testScheduleExtract_ReplaceExisting() throws SchedulerException {
        // Given
        Long datasetId = 1L;
        String cronExpression = "0 0 2 * * ?";

        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
        when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        // When
        dataExtractScheduler.scheduleExtract(datasetId, cronExpression);

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
        verify(scheduler, times(1)).scheduleJob(any(), any());
    }

    /**
     * 测试执行立即抽取 - 数据集不存在
     */
    @Test
    void testExecuteExtract_DatasetNotFound() {
        // Given
        Long datasetId = 999L;

        when(datasetMapper.selectDatasetById(datasetId)).thenReturn(null);

        // When
        ExtractResult result = dataExtractScheduler.executeExtract(datasetId);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("数据集不存在"));
    }

    /**
     * 测试执行立即抽取 - 数据集类型不是抽取类型
     */
    @Test
    void testExecuteExtract_NotExtractType() {
        // Given
        Long datasetId = 1L;
        Dataset dataset = new Dataset();
        dataset.setId(datasetId);
        dataset.setType("direct"); // 直连类型

        when(datasetMapper.selectDatasetById(datasetId)).thenReturn(dataset);

        // When
        ExtractResult result = dataExtractScheduler.executeExtract(datasetId);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("不是抽取类型"));
    }

    /**
     * 测试执行立即抽取 - 数据集已停用
     */
    @Test
    void testExecuteExtract_DatasetDisabled() {
        // Given
        Long datasetId = 1L;
        Dataset dataset = new Dataset();
        dataset.setId(datasetId);
        dataset.setType("extract");
        dataset.setStatus("1"); // 停用状态

        when(datasetMapper.selectDatasetById(datasetId)).thenReturn(dataset);

        // When
        ExtractResult result = dataExtractScheduler.executeExtract(datasetId);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("已停用"));
    }

    /**
     * 测试取消调度任务 - 成功场景
     */
    @Test
    void testCancelSchedule_Success() throws SchedulerException {
        // Given
        Long datasetId = 1L;

        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
        when(scheduler.deleteJob(any(JobKey.class))).thenReturn(true);

        // When
        dataExtractScheduler.cancelSchedule(datasetId);

        // Then
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    /**
     * 测试取消调度任务 - 任务不存在
     */
    @Test
    void testCancelSchedule_JobNotExists() throws SchedulerException {
        // Given
        Long datasetId = 1L;

        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // When
        dataExtractScheduler.cancelSchedule(datasetId);

        // Then
        verify(scheduler, never()).deleteJob(any(JobKey.class));
    }

    /**
     * 测试检查任务是否存在 - 任务存在
     */
    @Test
    void testIsScheduled_JobExists() throws SchedulerException {
        // Given
        Long datasetId = 1L;

        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        // When
        boolean result = dataExtractScheduler.isScheduled(datasetId);

        // Then
        assertTrue(result);
    }

    /**
     * 测试检查任务是否存在 - 任务不存在
     */
    @Test
    void testIsScheduled_JobNotExists() throws SchedulerException {
        // Given
        Long datasetId = 1L;

        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // When
        boolean result = dataExtractScheduler.isScheduled(datasetId);

        // Then
        assertFalse(result);
    }

    /**
     * 测试检查任务是否存在 - 数据集ID为空
     */
    @Test
    void testIsScheduled_NullDatasetId() {
        // Given
        Long datasetId = null;

        // When
        boolean result = dataExtractScheduler.isScheduled(datasetId);

        // Then
        assertFalse(result);
    }
}
