package com.zjrcu.iras.bi.metric.service.impl;

import com.zjrcu.iras.bi.metric.domain.LineageNode;
import com.zjrcu.iras.bi.metric.domain.MetricLineage;
import com.zjrcu.iras.bi.metric.domain.MetricMetadata;
import com.zjrcu.iras.bi.metric.dto.LineageGraphDTO;
import com.zjrcu.iras.bi.metric.dto.LineagePathDTO;
import com.zjrcu.iras.bi.metric.mapper.LineageEdgeMapper;
import com.zjrcu.iras.bi.metric.mapper.LineageNodeMapper;
import com.zjrcu.iras.bi.metric.mapper.MetricLineageMapper;
import com.zjrcu.iras.bi.metric.mapper.MetricMetadataMapper;
import com.zjrcu.iras.bi.metric.service.IMetricLineageService;
import com.zjrcu.iras.common.core.redis.RedisCache;

import com.zjrcu.iras.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 指标血缘Service业务层处理
 *
 * @author iras
 * @date 2025-02-26
 */
@Service
@Slf4j
public class MetricLineageServiceImpl implements IMetricLineageService {

    private static final int DEFAULT_MAX_DEPTH = 5;
    private static final int MAX_ALLOWED_DEPTH = 10;
    private static final String CACHE_PREFIX = "metric:lineage:";
    private static final int CACHE_TTL_HOURS = 1;

    @Autowired
    private MetricLineageMapper metricLineageMapper;

    @Autowired
    private LineageNodeMapper lineageNodeMapper;

    @Autowired
    private LineageEdgeMapper lineageEdgeMapper;

    @Autowired
    private MetricMetadataMapper metricMetadataMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取指标的完整血缘图（带模式参数）
     *
     * @param metricId 指标ID
     * @param mode 模式: graph-全量, upstream-上游, downstream-下游
     * @return 血缘图数据
     */
    @Override
    public LineageGraphDTO getLineageGraph(Long metricId, String mode) {
        // 验证参数
        if (metricId == null) {
            throw new ServiceException("指标ID不能为空");
        }

        // 根据mode确定maxDepth
        int maxDepth;
        if ("upstream".equals(mode)) {
            maxDepth = 5;
        } else if ("downstream".equals(mode)) {
            maxDepth = 5;
        } else {
            maxDepth = 3; // graph模式使用较小的深度
        }

        // 限制最大深度
        if (maxDepth > MAX_ALLOWED_DEPTH) {
            maxDepth = MAX_ALLOWED_DEPTH;
        }

        log.info("开始构建指标血缘图, metricId={}, mode={}, maxDepth={}", metricId, mode, maxDepth);

        try {
            // 初始化血缘图
            LineageGraphDTO graph = new LineageGraphDTO();
            Set<Long> visitedNodes = new HashSet<>();
            Set<String> visitedEdges = new HashSet<>();

            // 先批量查询所有相关指标的详细信息
            Map<Long, MetricMetadata> metricMap = this.loadRelatedMetrics(metricId, maxDepth);
            log.info("加载了 {} 个相关指标信息", metricMap.size());

            // 根据mode选择遍历方式
            if ("upstream".equals(mode)) {
                // 只遍历上游
                traverseUpstream(metricId, 0, maxDepth, visitedNodes, visitedEdges, graph, metricMap);
            } else if ("downstream".equals(mode)) {
                // 只遍历下游
                traverseDownstream(metricId, 0, maxDepth, visitedNodes, visitedEdges, graph, metricMap);
            } else {
                // graph模式：双向遍历
                traverseUpstream(metricId, 0, maxDepth, visitedNodes, visitedEdges, graph, metricMap);
                traverseDownstream(metricId, 0, maxDepth, visitedNodes, visitedEdges, graph, metricMap);
            }

            // 添加元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("rootMetricId", metricId);
            metadata.put("mode", mode);
            metadata.put("maxDepth", maxDepth);
            metadata.put("nodeCount", graph.getNodes().size());
            metadata.put("edgeCount", graph.getEdges().size());
            metadata.put("buildTime", new Date());
            graph.setMetadata(metadata);

            log.info("血缘图构建完成, 节点数={}, 边数={}", graph.getNodes().size(), graph.getEdges().size());

            return graph;
        } catch (Exception e) {
            log.error("构建血缘图失败, metricId={}, mode={}, error={}", metricId, mode, e.getMessage(), e);
            throw new ServiceException("构建血缘图失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标的完整血缘图（带深度控制）
     *
     * @param metricId 指标ID
     * @param maxDepth 最大遍历深度
     * @return 血缘图数据
     */
    @Override
    public LineageGraphDTO getMetricLineageGraph(Long metricId, int maxDepth) {
        // 验证参数
        if (metricId == null) {
            throw new ServiceException("指标ID不能为空");
        }

        // 限制最大深度
        if (maxDepth <= 0 || maxDepth > MAX_ALLOWED_DEPTH) {
            maxDepth = DEFAULT_MAX_DEPTH;
        }

        log.info("开始构建指标血缘图, metricId={}, maxDepth={}", metricId, maxDepth);

        try {
            // 初始化血缘图
            LineageGraphDTO graph = new LineageGraphDTO();
            Set<Long> visitedNodes = new HashSet<>();
            Set<String> visitedEdges = new HashSet<>();

            // 先批量查询所有相关指标的详细信息
            Map<Long, MetricMetadata> metricMap = this.loadRelatedMetrics(metricId, maxDepth);
            log.info("加载了 {} 个相关指标信息", metricMap.size());

            // 使用BFS遍历上游和下游血缘
            traverseUpstream(metricId, 0, maxDepth, visitedNodes, visitedEdges, graph, metricMap);
            traverseDownstream(metricId, 0, maxDepth, visitedNodes, visitedEdges, graph, metricMap);

            // 添加元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("rootMetricId", metricId);
            metadata.put("maxDepth", maxDepth);
            metadata.put("nodeCount", graph.getNodes().size());
            metadata.put("edgeCount", graph.getEdges().size());
            metadata.put("buildTime", new Date());
            graph.setMetadata(metadata);

            log.info("血缘图构建完成, 节点数={}, 边数={}", graph.getNodes().size(), graph.getEdges().size());

            return graph;
        } catch (Exception e) {
            log.error("构建血缘图失败, metricId={}, error={}", metricId, e.getMessage(), e);
            throw new ServiceException("构建血缘图失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标的上游血缘路径
     *
     * @param metricId 指标ID
     * @param maxDepth 最大深度
     * @return 上游指标列表
     */
    @Override
    public List<MetricLineage> getUpstreamLineage(Long metricId, int maxDepth) {
        if (metricId == null) {
            throw new ServiceException("指标ID不能为空");
        }

        if (maxDepth <= 0 || maxDepth > MAX_ALLOWED_DEPTH) {
            maxDepth = DEFAULT_MAX_DEPTH;
        }

        log.info("获取上游血缘, metricId={}, maxDepth={}", metricId, maxDepth);

        try {
            List<MetricLineage> allLineages = new ArrayList<>();
            Set<Long> visitedMetrics = new HashSet<>();
            Queue<Long> queue = new LinkedList<>();
            Map<Long, Integer> depthMap = new HashMap<>();

            queue.offer(metricId);
            visitedMetrics.add(metricId);
            depthMap.put(metricId, 0);

            while (!queue.isEmpty()) {
                Long currentMetricId = queue.poll();
                int currentDepth = depthMap.get(currentMetricId);

                if (currentDepth >= maxDepth) {
                    continue;
                }

                // 获取直接上游
                List<MetricLineage> upstreamLineages = metricLineageMapper.selectUpstreamLineage(currentMetricId);

                for (MetricLineage lineage : upstreamLineages) {
                    Long upstreamId = lineage.getUpstreamMetricId();

                    if (!visitedMetrics.contains(upstreamId)) {
                        visitedMetrics.add(upstreamId);
                        depthMap.put(upstreamId, currentDepth + 1);
                        queue.offer(upstreamId);
                        allLineages.add(lineage);
                    }
                }
            }

            log.info("上游血缘获取完成, 总数={}", allLineages.size());
            return allLineages;
        } catch (Exception e) {
            log.error("获取上游血缘失败, metricId={}, error={}", metricId, e.getMessage(), e);
            throw new ServiceException("获取上游血缘失败: " + e.getMessage());
        }
    }

    /**
     * 获取指标的下游血缘路径
     *
     * @param metricId 指标ID
     * @param maxDepth 最大深度
     * @return 下游指标列表
     */
    @Override
    public List<MetricLineage> getDownstreamLineage(Long metricId, int maxDepth) {
        if (metricId == null) {
            throw new ServiceException("指标ID不能为空");
        }

        if (maxDepth <= 0 || maxDepth > MAX_ALLOWED_DEPTH) {
            maxDepth = DEFAULT_MAX_DEPTH;
        }

        log.info("获取下游血缘, metricId={}, maxDepth={}", metricId, maxDepth);

        try {
            List<MetricLineage> allLineages = new ArrayList<>();
            Set<Long> visitedMetrics = new HashSet<>();
            Queue<Long> queue = new LinkedList<>();
            Map<Long, Integer> depthMap = new HashMap<>();

            queue.offer(metricId);
            visitedMetrics.add(metricId);
            depthMap.put(metricId, 0);

            while (!queue.isEmpty()) {
                Long currentMetricId = queue.poll();
                int currentDepth = depthMap.get(currentMetricId);

                if (currentDepth >= maxDepth) {
                    continue;
                }

                // 获取直接下游
                List<MetricLineage> downstreamLineages = metricLineageMapper.selectDownstreamLineage(currentMetricId);

                for (MetricLineage lineage : downstreamLineages) {
                    Long downstreamId = lineage.getDownstreamMetricId();

                    if (!visitedMetrics.contains(downstreamId)) {
                        visitedMetrics.add(downstreamId);
                        depthMap.put(downstreamId, currentDepth + 1);
                        queue.offer(downstreamId);
                        allLineages.add(lineage);
                    }
                }
            }

            log.info("下游血缘获取完成, 总数={}", allLineages.size());
            return allLineages;
        } catch (Exception e) {
            log.error("获取下游血缘失败, metricId={}, error={}", metricId, e.getMessage(), e);
            throw new ServiceException("获取下游血缘失败: " + e.getMessage());
        }
    }

    /**
     * 查找两个指标之间的血缘路径
     *
     * @param sourceId 源指标ID
     * @param targetId 目标指标ID
     * @param maxDepth 最大搜索深度
     * @return 路径列表，按路径长度排序
     */
    @Override
    public List<LineagePathDTO> findLineagePath(Long sourceId, Long targetId, int maxDepth) {
        if (sourceId == null || targetId == null) {
            throw new ServiceException("源指标ID和目标指标ID不能为空");
        }

        if (maxDepth <= 0 || maxDepth > MAX_ALLOWED_DEPTH * 2) {
            maxDepth = MAX_ALLOWED_DEPTH * 2;
        }

        log.info("查找血缘路径, sourceId={}, targetId={}, maxDepth={}", sourceId, targetId, maxDepth);

        try {
            // 使用BFS查找所有路径
            List<LineagePathDTO> paths = new ArrayList<>();
            Queue<PathNode> queue = new LinkedList<>();
            Set<Long> visited = new HashSet<>();

            // 从源节点开始
            PathNode startNode = new PathNode(sourceId, null, 0);
            queue.offer(startNode);
            visited.add(sourceId);

            while (!queue.isEmpty()) {
                PathNode currentNode = queue.poll();

                // 找到目标节点，构建路径
                if (currentNode.metricId.equals(targetId)) {
                    LineagePathDTO path = buildPath(currentNode);
                    if (path.isValid()) {
                        paths.add(path);
                    }
                    continue;
                }

                // 超过最大深度，停止搜索
                if (currentNode.depth >= maxDepth) {
                    continue;
                }

                // 获取下游节点
                List<Long> downstreamIds = metricLineageMapper.selectDirectDownstreamMetricIds(currentNode.metricId);

                for (Long downstreamId : downstreamIds) {
                    if (!visited.contains(downstreamId) || downstreamId.equals(targetId)) {
                        PathNode nextNode = new PathNode(downstreamId, currentNode, currentNode.depth + 1);
                        queue.offer(nextNode);
                        if (!downstreamId.equals(targetId)) {
                            visited.add(downstreamId);
                        }
                    }
                }
            }

            // 按路径长度排序
            paths.sort(Comparator.comparingInt(LineagePathDTO::getLength));

            log.info("路径查找完成, 找到{}条路径", paths.size());
            return paths;
        } catch (Exception e) {
            log.error("查找血缘路径失败, sourceId={}, targetId={}, error={}", sourceId, targetId, e.getMessage(), e);
            throw new ServiceException("查找血缘路径失败: " + e.getMessage());
        }
    }

    /**
     * 创建血缘关系
     *
     * @param lineage 血缘关系
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertLineage(MetricLineage lineage) {
        return insertMetricLineage(lineage);
    }

    /**
     * 创建血缘关系（别名方法）
     *
     * @param lineage 血缘关系
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMetricLineage(MetricLineage lineage) {
        if (lineage == null) {
            throw new ServiceException("血缘关系不能为空");
        }

        if (lineage.getUpstreamMetricId() == null || lineage.getDownstreamMetricId() == null) {
            throw new ServiceException("上游指标ID和下游指标ID不能为空");
        }

        // 检查循环依赖
        if (hasCircularDependency(lineage.getDownstreamMetricId())) {
            throw new ServiceException("存在循环依赖，无法创建血缘关系");
        }

        // 检查是否已存在相同的关系
        MetricLineage query = new MetricLineage();
        query.setUpstreamMetricId(lineage.getUpstreamMetricId());
        query.setDownstreamMetricId(lineage.getDownstreamMetricId());
        List<MetricLineage> existing = metricLineageMapper.selectMetricLineageList(query);
        if (existing != null && !existing.isEmpty()) {
            throw new ServiceException("血缘关系已存在");
        }

        int result = metricLineageMapper.insertLineage(lineage);

        // 清除缓存
        if (result > 0) {
            clearLineageCache(lineage.getUpstreamMetricId());
            clearLineageCache(lineage.getDownstreamMetricId());
        }

        return result;
    }

    /**
     * 批量创建血缘关系
     *
     * @param lineages 血缘关系列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertLineage(List<MetricLineage> lineages) {
        if (lineages == null || lineages.isEmpty()) {
            throw new ServiceException("血缘关系列表不能为空");
        }

        // 检查循环依赖
        for (MetricLineage lineage : lineages) {
            if (hasCircularDependency(lineage.getDownstreamMetricId())) {
                throw new ServiceException("存在循环依赖，无法创建血缘关系");
            }
        }

        int result = metricLineageMapper.batchInsertLineage(lineages);

        // 清除所有相关缓存
        if (result > 0) {
            for (MetricLineage lineage : lineages) {
                clearLineageCache(lineage.getUpstreamMetricId());
                clearLineageCache(lineage.getDownstreamMetricId());
            }
        }

        return result;
    }

    /**
     * 删除血缘关系
     *
     * @param id 血缘关系ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLineageById(Long id) {
        if (id == null) {
            throw new ServiceException("血缘关系ID不能为空");
        }

        MetricLineage lineage = metricLineageMapper.selectMetricLineageById(id);
        if (lineage == null) {
            throw new ServiceException("血缘关系不存在");
        }

        int result = metricLineageMapper.deleteLineageById(id);

        // 清除缓存
        if (result > 0) {
            clearLineageCache(lineage.getUpstreamMetricId());
            clearLineageCache(lineage.getDownstreamMetricId());
        }

        return result;
    }

    /**
     * 批量删除血缘关系
     *
     * @param ids 血缘关系ID列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLineageByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("血缘关系ID列表不能为空");
        }

        // 获取所有血缘关系以清除缓存
        Set<Long> metricIds = new HashSet<>();
        for (Long id : ids) {
            MetricLineage lineage = metricLineageMapper.selectMetricLineageById(id);
            if (lineage != null) {
                metricIds.add(lineage.getUpstreamMetricId());
                metricIds.add(lineage.getDownstreamMetricId());
            }
        }

        int result = metricLineageMapper.deleteLineageByIds(ids);

        // 清除缓存
        if (result > 0) {
            for (Long metricId : metricIds) {
                clearLineageCache(metricId);
            }
        }

        return result;
    }

    /**
     * 删除指标的所有血缘关系
     *
     * @param metricId 指标ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLineageByMetricId(Long metricId) {
        if (metricId == null) {
            throw new ServiceException("指标ID不能为空");
        }

        int result = metricLineageMapper.deleteLineageByMetricId(metricId);

        // 清除缓存
        if (result > 0) {
            clearLineageCache(metricId);
        }

        return result;
    }

    /**
     * 更新血缘关系
     *
     * @param lineage 血缘关系
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateLineage(MetricLineage lineage) {
        if (lineage == null || lineage.getId() == null) {
            throw new ServiceException("血缘关系ID不能为空");
        }

        MetricLineage existing = metricLineageMapper.selectMetricLineageById(lineage.getId());
        if (existing == null) {
            throw new ServiceException("血缘关系不存在");
        }

        int result = metricLineageMapper.updateLineage(lineage);

        // 清除缓存
        if (result > 0) {
            clearLineageCache(existing.getUpstreamMetricId());
            clearLineageCache(existing.getDownstreamMetricId());
            if (lineage.getUpstreamMetricId() != null) {
                clearLineageCache(lineage.getUpstreamMetricId());
            }
            if (lineage.getDownstreamMetricId() != null) {
                clearLineageCache(lineage.getDownstreamMetricId());
            }
        }

        return result;
    }

    /**
     * 验证血缘关系是否有效
     *
     * @param lineage 血缘关系
     * @return 是否有效
     */
    @Override
    public boolean validateLineage(MetricLineage lineage) {
        if (lineage == null) {
            return false;
        }

        if (lineage.getUpstreamMetricId() == null || lineage.getDownstreamMetricId() == null) {
            return false;
        }

        // 不能自引用
        if (lineage.getUpstreamMetricId().equals(lineage.getDownstreamMetricId())) {
            return false;
        }

        // 检查循环依赖
        if (hasCircularDependency(lineage.getDownstreamMetricId())) {
            return false;
        }

        return true;
    }

    /**
     * 检测血缘关系中是否存在循环依赖
     *
     * @param metricId 指标ID
     * @return 是否存在循环依赖
     */
    @Override
    public boolean hasCircularDependency(Long metricId) {
        if (metricId == null) {
            return false;
        }

        try {
            Integer count = metricLineageMapper.checkCircularDependency(metricId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检测循环依赖失败, metricId={}, error={}", metricId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取指标的直接上游指标列表
     *
     * @param metricId 指标ID
     * @return 上游指标ID列表
     */
    @Override
    public List<Long> getDirectUpstreamMetricIds(Long metricId) {
        if (metricId == null) {
            return new ArrayList<>();
        }

        try {
            return metricLineageMapper.selectDirectUpstreamMetricIds(metricId);
        } catch (Exception e) {
            log.error("获取直接上游指标失败, metricId={}, error={}", metricId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取指标的直接下游指标列表
     *
     * @param metricId 指标ID
     * @return 下游指标ID列表
     */
    @Override
    public List<Long> getDirectDownstreamMetricIds(Long metricId) {
        if (metricId == null) {
            return new ArrayList<>();
        }

        try {
            return metricLineageMapper.selectDirectDownstreamMetricIds(metricId);
        } catch (Exception e) {
            log.error("获取直接下游指标失败, metricId={}, error={}", metricId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 清除血缘图缓存
     *
     * @param metricId 指标ID，如果为null则清除所有缓存
     */
    @Override
    public void clearLineageCache(Long metricId) {
        try {
            if (metricId == null) {
                // 清除所有血缘缓存
                Collection<String> keys = redisCache.keys(CACHE_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisCache.deleteObject(keys);
                    log.info("已清除所有血缘缓存, 总数={}", keys.size());
                }
            } else {
                // 清除特定指标的缓存
                List<String> keysToDelete = new ArrayList<>();
                Collection<String> allKeys = redisCache.keys(CACHE_PREFIX + "*");

                if (allKeys != null) {
                    for (String key : allKeys) {
                        if (key.contains(":" + metricId + ":")) {
                            keysToDelete.add(key);
                        }
                    }
                    if (!keysToDelete.isEmpty()) {
                        redisCache.deleteObject(keysToDelete);
                        log.info("已清除指标血缘缓存, metricId={}, 数量={}", metricId, keysToDelete.size());
                    }
                }
            }
        } catch (Exception e) {
            log.error("清除缓存失败, metricId={}, error={}", metricId, e.getMessage(), e);
        }
    }

    /**
     * 同步血缘节点数据
     *
     * @param metricId 指标ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncLineageNode(Long metricId) {
        if (metricId == null) {
            throw new ServiceException("指标ID不能为空");
        }

        try {
            int result = lineageNodeMapper.syncMetricToLineageNode(metricId);

            // 清除缓存
            if (result > 0) {
                clearLineageCache(metricId);
            }

            log.info("同步血缘节点完成, metricId={}, result={}", metricId, result);
            return result;
        } catch (Exception e) {
            log.error("同步血缘节点失败, metricId={}, error={}", metricId, e.getMessage(), e);
            throw new ServiceException("同步血缘节点失败: " + e.getMessage());
        }
    }

    /**
     * 加载所有相关指标的详细信息
     */
    private Map<Long, MetricMetadata> loadRelatedMetrics(Long metricId, int maxDepth) {
        Map<Long, MetricMetadata> metricMap = new HashMap<>();

        // 先添加根节点
        MetricMetadata rootMetric = metricMetadataMapper.selectMetricMetadataById(metricId);
        if (rootMetric != null) {
            metricMap.put(metricId, rootMetric);
        }

        // 收集所有需要查询的指标ID
        Set<Long> metricIdsToQuery = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.offer(metricId);

        while (!queue.isEmpty() && metricIdsToQuery.size() < 100) {  // 限制最多查询100个指标
            Long currentId = queue.poll();

            // 查询上游
            List<MetricLineage> upstreamList = metricLineageMapper.selectUpstreamLineage(currentId);
            for (MetricLineage lineage : upstreamList) {
                Long upstreamId = lineage.getUpstreamMetricId();
                if (!metricIdsToQuery.contains(upstreamId)) {
                    metricIdsToQuery.add(upstreamId);
                    queue.offer(upstreamId);
                }
            }

            // 查询下游
            List<MetricLineage> downstreamList = metricLineageMapper.selectDownstreamLineage(currentId);
            for (MetricLineage lineage : downstreamList) {
                Long downstreamId = lineage.getDownstreamMetricId();
                if (!metricIdsToQuery.contains(downstreamId)) {
                    metricIdsToQuery.add(downstreamId);
                    queue.offer(downstreamId);
                }
            }
        }

        // 批量查询所有指标
        if (!metricIdsToQuery.isEmpty()) {
            List<MetricMetadata> metrics = metricMetadataMapper.selectMetricMetadataByIds(new ArrayList<>(metricIdsToQuery));
            for (MetricMetadata metric : metrics) {
                metricMap.put(metric.getId(), metric);
            }
        }

        return metricMap;
    }

    /**
     * BFS遍历上游血缘
     */
    private void traverseUpstream(Long metricId, int currentDepth, int maxDepth,
                                  Set<Long> visitedNodes, Set<String> visitedEdges,
                                  LineageGraphDTO graph,
                                  Map<Long, MetricMetadata> metricMap) {
        if (currentDepth >= maxDepth) {
            return;
        }

        // 先添加当前节点
        if (!visitedNodes.contains(metricId)) {
            visitedNodes.add(metricId);
            MetricMetadata metric = metricMap.get(metricId);
            String nodeName = (metric != null) ? metric.getMetricName() : "Metric-" + metricId;
            LineageGraphDTO.Node node = createNode(metricId, "metric", nodeName);
            graph.addNode(node);
        }

        // 获取直接上游
        List<MetricLineage> upstreamLineages = metricLineageMapper.selectUpstreamLineage(metricId);

        for (MetricLineage lineage : upstreamLineages) {
            Long upstreamId = lineage.getUpstreamMetricId();

            // 添加节点 - 使用metricMap获取真实的指标名称
            if (!visitedNodes.contains(upstreamId)) {
                visitedNodes.add(upstreamId);
                MetricMetadata metric = metricMap.get(upstreamId);
                String nodeName = (metric != null) ? metric.getMetricName() : "Metric-" + upstreamId;
                LineageGraphDTO.Node node = createNode(upstreamId, "metric", nodeName);
                graph.addNode(node);
            }

            // 添加边
            String edgeKey = upstreamId + "->" + metricId;
            if (!visitedEdges.contains(edgeKey)) {
                visitedEdges.add(edgeKey);
                LineageGraphDTO.Edge edge = new LineageGraphDTO.Edge(
                        UUID.randomUUID().toString(),
                        String.valueOf(upstreamId),
                        String.valueOf(metricId),
                        "flows_to"
                );
                edge.setLabel(lineage.getTransformationType());
                edge.addAttribute("transformationLogic", lineage.getTransformationLogic());
                edge.addAttribute("dependencyStrength", lineage.getDependencyStrength());
                graph.addEdge(edge);
            }

            // 递归遍历上游
            traverseUpstream(upstreamId, currentDepth + 1, maxDepth, visitedNodes, visitedEdges, graph, metricMap);
        }
    }

    /**
     * BFS遍历下游血缘
     */
    private void traverseDownstream(Long metricId, int currentDepth, int maxDepth,
                                    Set<Long> visitedNodes, Set<String> visitedEdges,
                                    LineageGraphDTO graph,
                                    Map<Long, MetricMetadata> metricMap) {
        if (currentDepth >= maxDepth) {
            return;
        }

        // 先添加当前节点
        if (!visitedNodes.contains(metricId)) {
            visitedNodes.add(metricId);
            MetricMetadata metric = metricMap.get(metricId);
            String nodeName = (metric != null) ? metric.getMetricName() : "Metric-" + metricId;
            LineageGraphDTO.Node node = createNode(metricId, "metric", nodeName);
            graph.addNode(node);
        }

        // 获取直接下游
        List<MetricLineage> downstreamLineages = metricLineageMapper.selectDownstreamLineage(metricId);

        for (MetricLineage lineage : downstreamLineages) {
            Long downstreamId = lineage.getDownstreamMetricId();

            // 添加节点 - 使用metricMap获取真实的指标名称
            if (!visitedNodes.contains(downstreamId)) {
                visitedNodes.add(downstreamId);
                MetricMetadata metric = metricMap.get(downstreamId);
                String nodeName = (metric != null) ? metric.getMetricName() : "Metric-" + downstreamId;
                LineageGraphDTO.Node node = createNode(downstreamId, "metric", nodeName);
                graph.addNode(node);
            }

            // 添加边
            String edgeKey = metricId + "->" + downstreamId;
            if (!visitedEdges.contains(edgeKey)) {
                visitedEdges.add(edgeKey);
                LineageGraphDTO.Edge edge = new LineageGraphDTO.Edge(
                        UUID.randomUUID().toString(),
                        String.valueOf(metricId),
                        String.valueOf(downstreamId),
                        "derived_from"
                );
                edge.setLabel(lineage.getTransformationType());
                edge.addAttribute("transformationLogic", lineage.getTransformationLogic());
                edge.addAttribute("dependencyStrength", lineage.getDependencyStrength());
                graph.addEdge(edge);
            }

            // 递归遍历下游
            traverseDownstream(downstreamId, currentDepth + 1, maxDepth, visitedNodes, visitedEdges, graph, metricMap);
        }
    }

    /**
     * 创建图节点
     */
    private LineageGraphDTO.Node createNode(Long id, String type, String name) {
        LineageGraphDTO.Node node = new LineageGraphDTO.Node();
        node.setId(String.valueOf(id));
        node.setType(type);
        node.setName(name);
        node.setSize(30);

        // 根据类型设置颜色
        if ("metric".equals(type)) {
            node.setColor("#1890ff");
        }

        return node;
    }

    /**
     * 构建路径
     */
    private LineagePathDTO buildPath(PathNode targetNode) {
        LineagePathDTO path = new LineagePathDTO();
        List<Long> nodeIds = new ArrayList<>();
        List<String> nodeNames = new ArrayList<>();

        PathNode current = targetNode;
        while (current != null) {
            nodeIds.add(0, current.metricId);
            nodeNames.add(0, "Metric-" + current.metricId);
            current = current.parent;
        }

        path.setNodeIds(nodeIds);
        path.setNodeNames(nodeNames);
        path.setLength(nodeIds.size() - 1);
        path.setPathType("downstream");

        return path;
    }

    /**
     * 路径节点内部类，用于BFS路径查找
     */
    private static class PathNode {
        Long metricId;
        PathNode parent;
        int depth;

        PathNode(Long metricId, PathNode parent, int depth) {
            this.metricId = metricId;
            this.parent = parent;
            this.depth = depth;
        }
    }
}
