package com.zjrcu.iras.bi.platform.cache.impl;

import com.zjrcu.iras.bi.platform.cache.CacheManager;
import com.zjrcu.iras.bi.platform.cache.CacheStats;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import com.zjrcu.iras.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BI缓存管理器实现
 *
 * @author iras
 */
@Component
public class CacheManagerImpl implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CacheManagerImpl.class);

    /**
     * 缓存键前缀
     */
    private static final String CACHE_PREFIX = "bi:dataset:";

    /**
     * 默认TTL（秒）- 5分钟
     */
    private static final int DEFAULT_TTL = 300;

    /**
     * 最小TTL（秒）- 1分钟
     */
    private static final int MIN_TTL = 60;

    /**
     * 最大TTL（秒）- 24小时
     */
    private static final int MAX_TTL = 86400;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存命中次数
     */
    private final AtomicLong hitCount = new AtomicLong(0);

    /**
     * 缓存未命中次数
     */
    private final AtomicLong missCount = new AtomicLong(0);

    @Override
    public QueryResult get(String cacheKey) {
        if (StringUtils.isEmpty(cacheKey)) {
            log.warn("缓存键为空,无法获取缓存");
            return null;
        }

        // Redis不可用时直接返回null
        if (redisTemplate == null) {
            log.debug("Redis不可用,缓存未命中: key={}", cacheKey);
            missCount.incrementAndGet();
            return null;
        }

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && cached instanceof QueryResult) {
                hitCount.incrementAndGet();
                log.debug("缓存命中: key={}", cacheKey);
                return (QueryResult) cached;
            } else {
                missCount.incrementAndGet();
                log.debug("缓存未命中: key={}", cacheKey);
                return null;
            }
        } catch (Exception e) {
            log.error("获取缓存失败: key={}, error={}", cacheKey, e.getMessage());
            missCount.incrementAndGet();
            return null;
        }
    }

    @Override
    public void put(String cacheKey, QueryResult result, int ttlSeconds) {
        if (StringUtils.isEmpty(cacheKey)) {
            log.warn("缓存键为空,无法存储缓存");
            return;
        }

        if (result == null) {
            log.warn("查询结果为空,不进行缓存: key={}", cacheKey);
            return;
        }

        // Redis不可用时跳过缓存
        if (redisTemplate == null) {
            log.debug("Redis不可用,跳过缓存存储: key={}", cacheKey);
            return;
        }

        // 验证TTL范围
        int validTtl = validateTtl(ttlSeconds);

        try {
            redisTemplate.opsForValue().set(cacheKey, result, validTtl, TimeUnit.SECONDS);
            log.debug("缓存已存储: key={}, ttl={}秒", cacheKey, validTtl);
        } catch (Exception e) {
            log.error("存储缓存失败: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    @Override
    public void invalidate(Long datasetId) {
        if (datasetId == null) {
            log.warn("数据集ID为空,无法失效缓存");
            return;
        }

        // Redis不可用时跳过
        if (redisTemplate == null) {
            log.debug("Redis不可用,跳过缓存失效: datasetId={}", datasetId);
            return;
        }

        try {
            String pattern = CACHE_PREFIX + datasetId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("数据集缓存已失效: datasetId={}, 删除缓存数={}", datasetId, deletedCount);
            } else {
                log.debug("数据集无缓存需要失效: datasetId={}", datasetId);
            }
        } catch (Exception e) {
            log.error("失效数据集缓存失败: datasetId={}, error={}", datasetId, e.getMessage());
        }
    }

    @Override
    public void invalidateKey(String cacheKey) {
        if (StringUtils.isEmpty(cacheKey)) {
            log.warn("缓存键为空,无法失效缓存");
            return;
        }

        // Redis不可用时跳过
        if (redisTemplate == null) {
            log.debug("Redis不可用,跳过缓存失效: key={}", cacheKey);
            return;
        }

        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("缓存已失效: key={}", cacheKey);
            } else {
                log.debug("缓存不存在或已过期: key={}", cacheKey);
            }
        } catch (Exception e) {
            log.error("失效缓存失败: key={}, error={}", cacheKey, e.getMessage());
        }
    }

    @Override
    public void clear() {
        // Redis不可用时跳过
        if (redisTemplate == null) {
            log.debug("Redis不可用,跳过缓存清空");
            return;
        }

        try {
            String pattern = CACHE_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("所有BI缓存已清空: 删除缓存数={}", deletedCount);
            } else {
                log.debug("无BI缓存需要清空");
            }
        } catch (Exception e) {
            log.error("清空BI缓存失败: error={}", e.getMessage());
        }
    }

    @Override
    public String generateCacheKey(Long datasetId, String filterHash) {
        if (datasetId == null) {
            throw new IllegalArgumentException("数据集ID不能为空");
        }

        if (StringUtils.isEmpty(filterHash)) {
            filterHash = "nofilter";
        }

        return CACHE_PREFIX + datasetId + ":filters:" + filterHash;
    }

    @Override
    public CacheStats getStats() {
        // Redis不可用时返回基本统计信息
        if (redisTemplate == null) {
            log.debug("Redis不可用,返回基本统计信息");
            return new CacheStats(hitCount.get(), missCount.get(), 0);
        }

        try {
            String pattern = CACHE_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            long cacheCount = keys != null ? keys.size() : 0;

            return new CacheStats(
                hitCount.get(),
                missCount.get(),
                cacheCount
            );
        } catch (Exception e) {
            log.error("获取缓存统计信息失败: error={}", e.getMessage());
            return new CacheStats(hitCount.get(), missCount.get(), 0);
        }
    }

    /**
     * 验证并调整TTL值
     *
     * @param ttlSeconds 原始TTL值
     * @return 有效的TTL值
     */
    private int validateTtl(int ttlSeconds) {
        if (ttlSeconds <= 0) {
            log.debug("TTL值无效({}),使用默认值: {}秒", ttlSeconds, DEFAULT_TTL);
            return DEFAULT_TTL;
        }

        if (ttlSeconds < MIN_TTL) {
            log.debug("TTL值过小({}),调整为最小值: {}秒", ttlSeconds, MIN_TTL);
            return MIN_TTL;
        }

        if (ttlSeconds > MAX_TTL) {
            log.debug("TTL值过大({}),调整为最大值: {}秒", ttlSeconds, MAX_TTL);
            return MAX_TTL;
        }

        return ttlSeconds;
    }

    /**
     * 重置统计信息
     */
    public void resetStats() {
        hitCount.set(0);
        missCount.set(0);
        log.info("缓存统计信息已重置");
    }
}
