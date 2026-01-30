package com.zjrcu.iras.bi.platform.scheduler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.ExtractResult;
import com.zjrcu.iras.bi.platform.engine.DataSourceManager;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.bi.platform.scheduler.DataExtractJob;
import com.zjrcu.iras.bi.platform.scheduler.IDataExtractScheduler;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

/**
 * 数据抽取调度器实现
 *
 * @author iras
 */
@Service
public class DataExtractSchedulerImpl implements IDataExtractScheduler {
    private static final Logger log = LoggerFactory.getLogger(DataExtractSchedulerImpl.class);

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRIES = 3;

    /**
     * 查询超时时间(秒)
     */
    private static final int QUERY_TIMEOUT = 300;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private DataSourceManager dataSourceManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调度抽取任务
     *
     * @param datasetId 数据集ID
     * @param cronExpression Cron表达式
     */
    @Override
    public void scheduleExtract(Long datasetId, String cronExpression) {
        if (datasetId == null) {
            throw new ServiceException("数据集ID不能为空");
        }

        if (StringUtils.isEmpty(cronExpression)) {
            throw new ServiceException("Cron表达式不能为空");
        }

        try {
            // 1. 验证Cron表达式
            if (!CronExpression.isValidExpression(cronExpression)) {
                throw new ServiceException("无效的Cron表达式: " + cronExpression);
            }

            // 2. 创建JobDetail
            String jobName = "extract-job-" + datasetId;
            String jobGroup = "extract-group";

            JobDetail jobDetail = JobBuilder.newJob(DataExtractJob.class)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData("datasetId", datasetId)
                    .storeDurably()
                    .build();

            // 3. 创建Trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("extract-trigger-" + datasetId, jobGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            // 4. 如果任务已存在,先删除
            JobKey jobKey = new JobKey(jobName, jobGroup);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("删除已存在的抽取任务: datasetId={}", datasetId);
            }

            // 5. 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

            log.info("调度抽取任务成功: datasetId={}, cron={}", datasetId, cronExpression);

        } catch (SchedulerException e) {
            log.error("调度抽取任务失败: datasetId={}, error={}", datasetId, e.getMessage(), e);
            throw new ServiceException("调度抽取任务失败: " + e.getMessage());
        }
    }

    /**
     * 执行立即抽取
     *
     * @param datasetId 数据集ID
     * @return 抽取结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExtractResult executeExtract(Long datasetId) {
        if (datasetId == null) {
            return ExtractResult.failure("数据集ID不能为空");
        }

        long startTime = System.currentTimeMillis();
        ExtractResult result = new ExtractResult();
        result.setStartTime(new Date());

        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                // 1. 获取数据集配置
                Dataset dataset = datasetMapper.selectDatasetById(datasetId);
                if (dataset == null) {
                    return ExtractResult.failure("数据集不存在: " + datasetId);
                }

                if (!dataset.isExtract()) {
                    return ExtractResult.failure("数据集类型不是抽取类型");
                }

                if ("1".equals(dataset.getStatus())) {
                    return ExtractResult.failure("数据集已停用");
                }

                // 2. 从数据源抽取数据
                List<Map<String, Object>> data = fetchDataFromSource(dataset);

                // 3. 存储到抽取表
                storeToExtractTable(datasetId, data);

                // 4. 更新数据集元数据
                updateExtractMetadata(datasetId, data.size());

                // 5. 设置成功结果
                result.setSuccess(true);
                result.setRowCount(data.size());
                result.setRetryCount(retryCount);
                result.setEndTime(new Date());
                result.setExecutionTime(System.currentTimeMillis() - startTime);

                log.info("数据抽取成功: datasetId={}, rows={}, duration={}ms, retries={}",
                        datasetId, data.size(), result.getExecutionTime(), retryCount);

                return result;

            } catch (Exception e) {
                retryCount++;
                log.error("数据抽取失败(第{}次重试): datasetId={}, error={}",
                        retryCount, datasetId, e.getMessage(), e);

                if (retryCount >= MAX_RETRIES) {
                    // 达到最大重试次数,返回失败结果
                    result.setSuccess(false);
                    result.setErrorMessage("数据抽取失败(已重试" + MAX_RETRIES + "次): " + e.getMessage());
                    result.setRetryCount(retryCount);
                    result.setEndTime(new Date());
                    result.setExecutionTime(System.currentTimeMillis() - startTime);

                    // TODO: 发送通知给管理员
                    log.error("数据抽取失败,已达到最大重试次数: datasetId={}", datasetId);

                    return result;
                }

                // 指数退避
                try {
                    long sleepTime = 1000L * (long) Math.pow(2, retryCount);
                    log.info("等待{}ms后重试...", sleepTime);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return ExtractResult.failure("抽取任务被中断");
                }
            }
        }

        return ExtractResult.failure("数据抽取失败");
    }

    /**
     * 取消调度任务
     *
     * @param datasetId 数据集ID
     */
    @Override
    public void cancelSchedule(Long datasetId) {
        if (datasetId == null) {
            throw new ServiceException("数据集ID不能为空");
        }

        try {
            String jobName = "extract-job-" + datasetId;
            String jobGroup = "extract-group";
            JobKey jobKey = new JobKey(jobName, jobGroup);

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("取消抽取任务成功: datasetId={}", datasetId);
            } else {
                log.warn("抽取任务不存在: datasetId={}", datasetId);
            }

        } catch (SchedulerException e) {
            log.error("取消抽取任务失败: datasetId={}, error={}", datasetId, e.getMessage(), e);
            throw new ServiceException("取消抽取任务失败: " + e.getMessage());
        }
    }

    /**
     * 检查任务是否存在
     *
     * @param datasetId 数据集ID
     * @return true表示任务已调度
     */
    @Override
    public boolean isScheduled(Long datasetId) {
        if (datasetId == null) {
            return false;
        }

        try {
            String jobName = "extract-job-" + datasetId;
            String jobGroup = "extract-group";
            JobKey jobKey = new JobKey(jobName, jobGroup);

            return scheduler.checkExists(jobKey);

        } catch (SchedulerException e) {
            log.error("检查任务状态失败: datasetId={}, error={}", datasetId, e.getMessage());
            return false;
        }
    }

    /**
     * 从数据源抽取数据
     *
     * @param dataset 数据集
     * @return 数据列表
     */
    private List<Map<String, Object>> fetchDataFromSource(Dataset dataset) throws Exception {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // 1. 获取数据源连接
            connection = dataSourceManager.getConnection(dataset.getDatasourceId());

            // 2. 构建查询SQL
            String sql = buildExtractSql(dataset);
            log.debug("抽取SQL: {}", sql);

            // 3. 执行查询
            statement = connection.createStatement();
            statement.setQueryTimeout(QUERY_TIMEOUT);
            resultSet = statement.executeQuery(sql);

            // 4. 解析结果
            return parseResultSet(resultSet);

        } finally {
            closeResources(resultSet, statement, connection);
        }
    }

    /**
     * 构建抽取SQL
     *
     * @param dataset 数据集
     * @return SQL语句
     */
    private String buildExtractSql(Dataset dataset) {
        Map<String, Object> queryConfig = dataset.getQueryConfigMap();
        Map<String, Object> extractConfig = dataset.getExtractConfigMap();

        StringBuilder sql = new StringBuilder();

        // 1. 构建SELECT子句
        sql.append("SELECT * FROM ");

        // 2. 构建FROM子句
        String sourceType = (String) queryConfig.get("sourceType");
        if ("table".equalsIgnoreCase(sourceType)) {
            String tableName = (String) queryConfig.get("tableName");
            sql.append(tableName);
        } else if ("sql".equalsIgnoreCase(sourceType)) {
            String customSql = (String) queryConfig.get("sql");
            sql.append("(").append(customSql).append(") AS t");
        }

        // 3. 增量抽取条件
        Boolean incremental = (Boolean) extractConfig.getOrDefault("incremental", false);
        if (incremental) {
            String incrementalField = (String) extractConfig.get("incrementalField");
            if (StringUtils.isNotEmpty(incrementalField)) {
                // 获取上次抽取时间
                Date lastExtractTime = dataset.getLastExtractTime();
                if (lastExtractTime != null) {
                    sql.append(" WHERE ").append(incrementalField)
                            .append(" > '").append(lastExtractTime).append("'");
                }
            }
        }

        return sql.toString();
    }

    /**
     * 解析ResultSet为数据列表
     *
     * @param resultSet 结果集
     * @return 数据列表
     */
    private List<Map<String, Object>> parseResultSet(ResultSet resultSet) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            data.add(row);
        }

        return data;
    }

    /**
     * 存储数据到抽取表
     *
     * @param datasetId 数据集ID
     * @param data 数据列表
     */
    private void storeToExtractTable(Long datasetId, List<Map<String, Object>> data) throws Exception {
        // 注意: 这是简化实现
        // 完整实现应该:
        // 1. 先删除该数据集的旧数据(如果不是增量抽取)
        // 2. 批量插入新数据到bi_extract_data表
        // 3. 使用PreparedStatement批处理提高性能

        Connection connection = null;
        Statement statement = null;

        try {
            // 获取系统数据库连接
            // TODO: 这里应该使用系统数据库连接,而不是数据集的数据源连接
            // 暂时使用datasetId=1的数据源作为系统数据库
            connection = dataSourceManager.getConnection(1L);

            statement = connection.createStatement();

            // 1. 删除旧数据(非增量抽取)
            String deleteSql = "DELETE FROM bi_extract_data WHERE dataset_id = " + datasetId;
            statement.executeUpdate(deleteSql);
            log.debug("删除旧抽取数据: datasetId={}", datasetId);

            // 2. 插入新数据
            for (Map<String, Object> row : data) {
                String dataContent = objectMapper.writeValueAsString(row);
                String insertSql = String.format(
                        "INSERT INTO bi_extract_data (dataset_id, data_content, extract_time) VALUES (%d, '%s', NOW())",
                        datasetId, dataContent.replace("'", "''")
                );
                statement.executeUpdate(insertSql);
            }

            log.debug("存储抽取数据成功: datasetId={}, rows={}", datasetId, data.size());

        } finally {
            closeResources(null, statement, connection);
        }
    }

    /**
     * 更新数据集抽取元数据
     *
     * @param datasetId 数据集ID
     * @param rowCount 行数
     */
    private void updateExtractMetadata(Long datasetId, int rowCount) {
        Dataset dataset = new Dataset();
        dataset.setId(datasetId);
        dataset.setLastExtractTime(new Date());
        dataset.setRowCount((long) rowCount);

        datasetMapper.updateDataset(dataset);

        log.debug("更新抽取元数据: datasetId={}, rows={}", datasetId, rowCount);
    }

    /**
     * 关闭资源
     *
     * @param resultSet 结果集
     * @param statement 语句
     * @param connection 连接
     */
    private void closeResources(ResultSet resultSet, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception e) {
                log.warn("关闭ResultSet失败: {}", e.getMessage());
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
                log.warn("关闭Statement失败: {}", e.getMessage());
            }
        }

        if (connection != null) {
            dataSourceManager.releaseConnection(connection);
        }
    }
}
