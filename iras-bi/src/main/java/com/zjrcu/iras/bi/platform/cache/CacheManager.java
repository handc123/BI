package com.zjrcu.iras.bi.platform.cache;

import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;

/**
 * BI缓存管理器接口
 *
 * @author iras
 */
public interface CacheManager {

    /**
     * 获取缓存结果
     *
     * @param cacheKey 缓存键
     * @return 缓存的查询结果,如果不存在或已过期则返回null
     */
    QueryResult get(String cacheKey);

    /**
     * 存储查询结果到缓存
     *
     * @param cacheKey 缓存键
     * @param result 查询结果
     * @param ttlSeconds 生存时间(秒)
     */
    void put(String cacheKey, QueryResult result, int ttlSeconds);

    /**
     * 使数据集相关的所有缓存失效
     *
     * @param datasetId 数据集ID
     */
    void invalidate(Long datasetId);

    /**
     * 使指定缓存键失效
     *
     * @param cacheKey 缓存键
     */
    void invalidateKey(String cacheKey);

    /**
     * 清空所有BI缓存
     */
    void clear();

    /**
     * 生成缓存键
     *
     * @param datasetId 数据集ID
     * @param filterHash 筛选条件哈希值
     * @return 缓存键
     */
    String generateCacheKey(Long datasetId, String filterHash);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    CacheStats getStats();
}
