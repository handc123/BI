package com.zjrcu.iras.bi.platform.monitor;

import java.io.Serializable;

/**
 * 查询统计快照
 * 不可变的统计信息快照
 *
 * @author iras
 */
public class QueryStatsSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总查询次数
     */
    private final long totalQueries;

    /**
     * 成功查询次数
     */
    private final long successQueries;

    /**
     * 失败查询次数
     */
    private final long failedQueries;

    /**
     * 缓存命中次数
     */
    private final long cachedQueries;

    /**
     * 最小执行时间(毫秒)
     */
    private final long minExecutionTime;

    /**
     * 最大执行时间(毫秒)
     */
    private final long maxExecutionTime;

    /**
     * 平均执行时间(毫秒)
     */
    private final double avgExecutionTime;

    /**
     * 平均返回行数
     */
    private final double avgRowCount;

    /**
     * 成功率(%)
     */
    private final double successRate;

    /**
     * 缓存命中率(%)
     */
    private final double cacheHitRate;

    public QueryStatsSnapshot() {
        this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public QueryStatsSnapshot(long totalQueries, long successQueries, long failedQueries, 
                             long cachedQueries, long minExecutionTime, long maxExecutionTime,
                             double avgExecutionTime, double avgRowCount,
                             double successRate, double cacheHitRate) {
        this.totalQueries = totalQueries;
        this.successQueries = successQueries;
        this.failedQueries = failedQueries;
        this.cachedQueries = cachedQueries;
        this.minExecutionTime = minExecutionTime;
        this.maxExecutionTime = maxExecutionTime;
        this.avgExecutionTime = avgExecutionTime;
        this.avgRowCount = avgRowCount;
        this.successRate = successRate;
        this.cacheHitRate = cacheHitRate;
    }

    public long getTotalQueries() {
        return totalQueries;
    }

    public long getSuccessQueries() {
        return successQueries;
    }

    public long getFailedQueries() {
        return failedQueries;
    }

    public long getCachedQueries() {
        return cachedQueries;
    }

    public long getMinExecutionTime() {
        return minExecutionTime;
    }

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public double getAvgExecutionTime() {
        return avgExecutionTime;
    }

    public double getAvgRowCount() {
        return avgRowCount;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public double getCacheHitRate() {
        return cacheHitRate;
    }

    @Override
    public String toString() {
        return "QueryStatsSnapshot{" +
                "totalQueries=" + totalQueries +
                ", successQueries=" + successQueries +
                ", failedQueries=" + failedQueries +
                ", cachedQueries=" + cachedQueries +
                ", minExecutionTime=" + minExecutionTime + "ms" +
                ", maxExecutionTime=" + maxExecutionTime + "ms" +
                ", avgExecutionTime=" + String.format("%.2f", avgExecutionTime) + "ms" +
                ", avgRowCount=" + String.format("%.2f", avgRowCount) +
                ", successRate=" + String.format("%.2f", successRate) + "%" +
                ", cacheHitRate=" + String.format("%.2f", cacheHitRate) + "%" +
                '}';
    }
}
