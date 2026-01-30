package com.zjrcu.iras.bi.platform.cache;

import java.io.Serializable;

/**
 * 缓存统计信息
 *
 * @author iras
 */
public class CacheStats implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 缓存命中次数
     */
    private long hitCount;

    /**
     * 缓存未命中次数
     */
    private long missCount;

    /**
     * 缓存总数
     */
    private long cacheCount;

    /**
     * 缓存命中率
     */
    private double hitRate;

    public CacheStats() {
    }

    public CacheStats(long hitCount, long missCount, long cacheCount) {
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.cacheCount = cacheCount;
        this.hitRate = calculateHitRate(hitCount, missCount);
    }

    /**
     * 计算命中率
     */
    private double calculateHitRate(long hitCount, long missCount) {
        long total = hitCount + missCount;
        if (total == 0) {
            return 0.0;
        }
        return (double) hitCount / total * 100;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
        this.hitRate = calculateHitRate(this.hitCount, this.missCount);
    }

    public long getMissCount() {
        return missCount;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
        this.hitRate = calculateHitRate(this.hitCount, this.missCount);
    }

    public long getCacheCount() {
        return cacheCount;
    }

    public void setCacheCount(long cacheCount) {
        this.cacheCount = cacheCount;
    }

    public double getHitRate() {
        return hitRate;
    }

    public void setHitRate(double hitRate) {
        this.hitRate = hitRate;
    }

    @Override
    public String toString() {
        return "CacheStats{" +
                "hitCount=" + hitCount +
                ", missCount=" + missCount +
                ", cacheCount=" + cacheCount +
                ", hitRate=" + String.format("%.2f", hitRate) + "%" +
                '}';
    }
}
