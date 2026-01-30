package com.zjrcu.iras.bi.platform.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 查询性能监控器
 * 监控查询执行性能指标
 *
 * @author iras
 */
@Component
public class QueryPerformanceMonitor {

    private static final Logger log = LoggerFactory.getLogger(QueryPerformanceMonitor.class);

    /**
     * 数据集查询统计
     * Key: datasetId, Value: QueryStats
     */
    private final Map<Long, QueryStats> datasetStats = new ConcurrentHashMap<>();

    /**
     * 全局查询统计
     */
    private final QueryStats globalStats = new QueryStats();

    /**
     * 记录查询执行
     *
     * @param datasetId 数据集ID
     * @param executionTime 执行时间(毫秒)
     * @param rowCount 返回行数
     * @param fromCache 是否来自缓存
     * @param success 是否成功
     */
    public void recordQuery(Long datasetId, long executionTime, long rowCount, boolean fromCache, boolean success) {
        if (datasetId == null) {
            return;
        }

        // 更新数据集统计
        QueryStats stats = datasetStats.computeIfAbsent(datasetId, k -> new QueryStats());
        stats.record(executionTime, rowCount, fromCache, success);

        // 更新全局统计
        globalStats.record(executionTime, rowCount, fromCache, success);

        // 记录慢查询
        if (executionTime > 3000 && !fromCache) {
            log.warn("慢查询检测: datasetId={}, executionTime={}ms, rowCount={}", 
                    datasetId, executionTime, rowCount);
        }
    }

    /**
     * 获取数据集查询统计
     *
     * @param datasetId 数据集ID
     * @return 查询统计信息
     */
    public QueryStatsSnapshot getDatasetStats(Long datasetId) {
        QueryStats stats = datasetStats.get(datasetId);
        return stats != null ? stats.snapshot() : new QueryStatsSnapshot();
    }

    /**
     * 获取全局查询统计
     *
     * @return 全局查询统计信息
     */
    public QueryStatsSnapshot getGlobalStats() {
        return globalStats.snapshot();
    }

    /**
     * 重置数据集统计
     *
     * @param datasetId 数据集ID
     */
    public void resetDatasetStats(Long datasetId) {
        datasetStats.remove(datasetId);
        log.info("数据集查询统计已重置: datasetId={}", datasetId);
    }

    /**
     * 重置全局统计
     */
    public void resetGlobalStats() {
        datasetStats.clear();
        globalStats.reset();
        log.info("全局查询统计已重置");
    }

    /**
     * 查询统计信息
     */
    private static class QueryStats {
        private final LongAdder totalQueries = new LongAdder();
        private final LongAdder successQueries = new LongAdder();
        private final LongAdder failedQueries = new LongAdder();
        private final LongAdder cachedQueries = new LongAdder();
        private final LongAdder totalExecutionTime = new LongAdder();
        private final LongAdder totalRowCount = new LongAdder();
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxExecutionTime = new AtomicLong(0);

        public void record(long executionTime, long rowCount, boolean fromCache, boolean success) {
            totalQueries.increment();
            
            if (success) {
                successQueries.increment();
            } else {
                failedQueries.increment();
            }

            if (fromCache) {
                cachedQueries.increment();
            }

            totalExecutionTime.add(executionTime);
            totalRowCount.add(rowCount);

            // 更新最小执行时间
            minExecutionTime.updateAndGet(current -> Math.min(current, executionTime));

            // 更新最大执行时间
            maxExecutionTime.updateAndGet(current -> Math.max(current, executionTime));
        }

        public void reset() {
            totalQueries.reset();
            successQueries.reset();
            failedQueries.reset();
            cachedQueries.reset();
            totalExecutionTime.reset();
            totalRowCount.reset();
            minExecutionTime.set(Long.MAX_VALUE);
            maxExecutionTime.set(0);
        }

        public QueryStatsSnapshot snapshot() {
            long total = totalQueries.sum();
            long success = successQueries.sum();
            long failed = failedQueries.sum();
            long cached = cachedQueries.sum();
            long totalTime = totalExecutionTime.sum();
            long totalRows = totalRowCount.sum();
            long min = minExecutionTime.get();
            long max = maxExecutionTime.get();

            // 计算平均值
            double avgTime = total > 0 ? (double) totalTime / total : 0;
            double avgRows = total > 0 ? (double) totalRows / total : 0;

            // 计算成功率和缓存命中率
            double successRate = total > 0 ? (double) success / total * 100 : 0;
            double cacheHitRate = total > 0 ? (double) cached / total * 100 : 0;

            return new QueryStatsSnapshot(
                total, success, failed, cached,
                min == Long.MAX_VALUE ? 0 : min,
                max, avgTime, avgRows,
                successRate, cacheHitRate
            );
        }
    }
}
