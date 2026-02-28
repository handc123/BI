package com.zjrcu.iras.bi.metric.mapper;

import com.zjrcu.iras.bi.metric.domain.LineageEdge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 血缘边Mapper接口
 *
 * @author iras
 * @date 2025-02-26
 */
@Mapper
public interface LineageEdgeMapper {

    /**
     * 查询血缘边列表
     *
     * @param lineageEdge 血缘边
     * @return 血缘边集合
     */
    List<LineageEdge> selectLineageEdgeList(LineageEdge lineageEdge);

    /**
     * 根据ID查询血缘边
     *
     * @param id 血缘边主键
     * @return 血缘边
     */
    LineageEdge selectLineageEdgeById(Long id);

    /**
     * 查询源节点的所有出边
     *
     * @param sourceNodeId 源节点ID
     * @return 血缘边集合
     */
    List<LineageEdge> selectEdgesBySourceNodeId(@Param("sourceNodeId") Long sourceNodeId);

    /**
     * 查询目标节点的所有入边
     *
     * @param targetNodeId 目标节点ID
     * @return 血缘边集合
     */
    List<LineageEdge> selectEdgesByTargetNodeId(@Param("targetNodeId") Long targetNodeId);

    /**
     * 新增血缘边
     *
     * @param lineageEdge 血缘边
     * @return 结果
     */
    int insertLineageEdge(LineageEdge lineageEdge);

    /**
     * 批量新增血缘边
     *
     * @param lineageEdges 血缘边列表
     * @return 插入行数
     */
    int batchInsertLineageEdges(@Param("list") List<LineageEdge> lineageEdges);

    /**
     * 修改血缘边
     *
     * @param lineageEdge 血缘边
     * @return 结果
     */
    int updateLineageEdge(LineageEdge lineageEdge);

    /**
     * 删除血缘边
     *
     * @param id 血缘边主键
     * @return 结果
     */
    int deleteLineageEdgeById(Long id);

    /**
     * 批量删除血缘边
     *
     * @param ids 需要删除的血缘边主键集合
     * @return 结果
     */
    int deleteLineageEdgeByIds(Long[] ids);

    /**
     * 删除节点的所有关联边
     *
     * @param nodeId 节点ID
     * @return 结果
     */
    int deleteEdgesByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 查询两个节点之间的边
     *
     * @param sourceNodeId 源节点ID
     * @param targetNodeId 目标节点ID
     * @return 血缘边
     */
    LineageEdge selectEdgeByNodes(@Param("sourceNodeId") Long sourceNodeId,
                                  @Param("targetNodeId") Long targetNodeId);
}
