package com.zjrcu.iras.bi.metric.mapper;

import com.zjrcu.iras.bi.metric.domain.LineageNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 血缘节点Mapper接口
 *
 * @author iras
 * @date 2025-02-26
 */
@Mapper
public interface LineageNodeMapper {

    /**
     * 查询血缘节点列表
     *
     * @param lineageNode 血缘节点
     * @return 血缘节点集合
     */
    List<LineageNode> selectLineageNodeList(LineageNode lineageNode);

    /**
     * 根据ID查询血缘节点
     *
     * @param id 血缘节点主键
     * @return 血缘节点
     */
    LineageNode selectLineageNodeById(Long id);

    /**
     * 根据节点类型和业务ID查询节点
     *
     * @param nodeType 节点类型
     * @param nodeId 节点业务ID
     * @return 血缘节点
     */
    LineageNode selectLineageNodeByTypeAndId(@Param("nodeType") String nodeType,
                                              @Param("nodeId") Long nodeId);

    /**
     * 新增血缘节点
     *
     * @param lineageNode 血缘节点
     * @return 结果
     */
    int insertLineageNode(LineageNode lineageNode);

    /**
     * 批量新增血缘节点
     *
     * @param lineageNodes 血缘节点列表
     * @return 插入行数
     */
    int batchInsertLineageNodes(@Param("list") List<LineageNode> lineageNodes);

    /**
     * 修改血缘节点
     *
     * @param lineageNode 血缘节点
     * @return 结果
     */
    int updateLineageNode(LineageNode lineageNode);

    /**
     * 删除血缘节点
     *
     * @param id 血缘节点主键
     * @return 结果
     */
    int deleteLineageNodeById(Long id);

    /**
     * 批量删除血缘节点
     *
     * @param ids 需要删除的血缘节点主键集合
     * @return 结果
     */
    int deleteLineageNodeByIds(Long[] ids);

    /**
     * 根据节点类型查询节点列表
     *
     * @param nodeType 节点类型
     * @return 节点列表
     */
    List<LineageNode> selectNodesByType(@Param("nodeType") String nodeType);

    /**
     * 根据业务ID查询节点
     *
     * @param nodeId 业务ID
     * @return 节点列表
     */
    List<LineageNode> selectNodesByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 同步指标元数据到血缘节点表
     *
     * @param metricId 指标ID
     * @return 结果
     */
    int syncMetricToLineageNode(@Param("metricId") Long metricId);
}
