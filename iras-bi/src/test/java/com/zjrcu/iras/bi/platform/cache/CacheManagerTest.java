package com.zjrcu.iras.bi.platform.cache;

import com.zjrcu.iras.bi.platform.cache.impl.CacheManagerImpl;
import com.zjrcu.iras.bi.platform.domain.dto.QueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CacheManager单元测试
 *
 * @author iras
 */
class CacheManagerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CacheManagerImpl cacheManager;

    private QueryResult testResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testResult = new QueryResult();
        testResult.setSuccess(true);
        
        // 创建符合 List<Map<String, Object>> 类型的测试数据
        Map<String, Object> row1 = new HashMap<>();
        row1.put("date", "2024-01-01");
        row1.put("value", 1000);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("date", "2024-01-02");
        row2.put("value", 1500);
        
        testResult.setData(Arrays.asList(row1, row2));
    }

    @Test
    void testGet_CacheHit() {
        // 准备测试数据
        String cacheKey = "bi:dataset:1:filters:abc123";
        when(valueOperations.get(cacheKey)).thenReturn(testResult);

        // 执行测试
        QueryResult result = cacheManager.get(cacheKey);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(valueOperations, times(1)).get(cacheKey);
    }

    @Test
    void testGet_CacheMiss() {
        // 准备测试数据
        String cacheKey = "bi:dataset:1:filters:xyz789";
        when(valueOperations.get(cacheKey)).thenReturn(null);

        // 执行测试
        QueryResult result = cacheManager.get(cacheKey);

        // 验证结果
        assertNull(result);
        verify(valueOperations, times(1)).get(cacheKey);
    }

    @Test
    void testGet_EmptyKey() {
        // 执行测试
        QueryResult result = cacheManager.get("");

        // 验证结果
        assertNull(result);
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void testPut_Success() {
        // 准备测试数据
        String cacheKey = "bi:dataset:1:filters:abc123";
        int ttl = 300;

        // 执行测试
        cacheManager.put(cacheKey, testResult, ttl);

        // 验证结果
        verify(valueOperations, times(1)).set(cacheKey, testResult, ttl, TimeUnit.SECONDS);
    }

    @Test
    void testPut_WithInvalidTtl() {
        // 准备测试数据
        String cacheKey = "bi:dataset:1:filters:abc123";
        int invalidTtl = -1;

        // 执行测试
        cacheManager.put(cacheKey, testResult, invalidTtl);

        // 验证结果 - 应该使用默认TTL
        verify(valueOperations, times(1)).set(eq(cacheKey), eq(testResult), eq(300), eq(TimeUnit.SECONDS));
    }

    @Test
    void testPut_EmptyKey() {
        // 执行测试
        cacheManager.put("", testResult, 300);

        // 验证结果
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testPut_NullResult() {
        // 执行测试
        cacheManager.put("bi:dataset:1:filters:abc123", null, 300);

        // 验证结果
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testInvalidate_Success() {
        // 准备测试数据
        Long datasetId = 1L;
        String pattern = "bi:dataset:1:*";
        Set<String> keys = new HashSet<>(Arrays.asList(
            "bi:dataset:1:filters:abc123",
            "bi:dataset:1:filters:xyz789"
        ));
        when(redisTemplate.keys(pattern)).thenReturn(keys);
        when(redisTemplate.delete(keys)).thenReturn(2L);

        // 执行测试
        cacheManager.invalidate(datasetId);

        // 验证结果
        verify(redisTemplate, times(1)).keys(pattern);
        verify(redisTemplate, times(1)).delete(keys);
    }

    @Test
    void testInvalidate_NoKeys() {
        // 准备测试数据
        Long datasetId = 1L;
        String pattern = "bi:dataset:1:*";
        when(redisTemplate.keys(pattern)).thenReturn(new HashSet<>());

        // 执行测试
        cacheManager.invalidate(datasetId);

        // 验证结果
        verify(redisTemplate, times(1)).keys(pattern);
        verify(redisTemplate, never()).delete(any(Set.class));
    }

    @Test
    void testInvalidateKey_Success() {
        // 准备测试数据
        String cacheKey = "bi:dataset:1:filters:abc123";
        when(redisTemplate.delete(cacheKey)).thenReturn(true);

        // 执行测试
        cacheManager.invalidateKey(cacheKey);

        // 验证结果
        verify(redisTemplate, times(1)).delete(cacheKey);
    }

    @Test
    void testClear_Success() {
        // 准备测试数据
        String pattern = "bi:dataset:*";
        Set<String> keys = new HashSet<>(Arrays.asList(
            "bi:dataset:1:filters:abc123",
            "bi:dataset:2:filters:xyz789"
        ));
        when(redisTemplate.keys(pattern)).thenReturn(keys);
        when(redisTemplate.delete(keys)).thenReturn(2L);

        // 执行测试
        cacheManager.clear();

        // 验证结果
        verify(redisTemplate, times(1)).keys(pattern);
        verify(redisTemplate, times(1)).delete(keys);
    }

    @Test
    void testGenerateCacheKey_Success() {
        // 执行测试
        String cacheKey = cacheManager.generateCacheKey(1L, "abc123");

        // 验证结果
        assertEquals("bi:dataset:1:filters:abc123", cacheKey);
    }

    @Test
    void testGenerateCacheKey_EmptyFilterHash() {
        // 执行测试
        String cacheKey = cacheManager.generateCacheKey(1L, "");

        // 验证结果
        assertEquals("bi:dataset:1:filters:nofilter", cacheKey);
    }

    @Test
    void testGenerateCacheKey_NullDatasetId() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            cacheManager.generateCacheKey(null, "abc123");
        });
    }

    @Test
    void testGetStats_Success() {
        // 准备测试数据
        String pattern = "bi:dataset:*";
        Set<String> keys = new HashSet<>(Arrays.asList(
            "bi:dataset:1:filters:abc123",
            "bi:dataset:2:filters:xyz789"
        ));
        when(redisTemplate.keys(pattern)).thenReturn(keys);

        // 模拟一些缓存命中和未命中
        when(valueOperations.get("bi:dataset:1:filters:abc123")).thenReturn(testResult);
        cacheManager.get("bi:dataset:1:filters:abc123"); // 命中
        when(valueOperations.get("bi:dataset:2:filters:xyz789")).thenReturn(null);
        cacheManager.get("bi:dataset:2:filters:xyz789"); // 未命中

        // 执行测试
        CacheStats stats = cacheManager.getStats();

        // 验证结果
        assertNotNull(stats);
        assertEquals(1, stats.getHitCount());
        assertEquals(1, stats.getMissCount());
        assertEquals(2, stats.getCacheCount());
        assertEquals(50.0, stats.getHitRate(), 0.01);
    }
}
