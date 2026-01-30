package com.zjrcu.iras.bi.fieldquality.service;

import java.util.List;

import com.zjrcu.iras.bi.fieldquality.domain.model.RulePassRateDistribution;
import com.zjrcu.iras.bi.fieldquality.domain.vo.MonthlyPassRateSummary;

/**
 * 规则项通过率分布统计（用于看板环形图及趋势图展示）Service接口
 *
 * @author ruoyi
 * @date 2025-07-28
 */
public interface IRulePassRateDistributionService {

    List<RulePassRateDistribution> selectRulePassRateDistribution();

    List<MonthlyPassRateSummary> selectRulePassRateTrend();
}
