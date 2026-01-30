package com.zjrcu.iras.bi.fieldquality.mapper;

import java.util.List;

import com.zjrcu.iras.bi.fieldquality.domain.model.RulePassRateDistribution;
import org.apache.ibatis.annotations.Mapper;

/**
 * 规则项通过率分布统计（用于看板环形图及趋势图展示）Mapper接口
 *
 * @author ruoyi
 * @date 2025-07-28
 */
@Mapper
public interface RulePassRateDistributionMapper {


    List<RulePassRateDistribution> selectRulePassRateDistributionLocalDateByOrgId(String date);



}
