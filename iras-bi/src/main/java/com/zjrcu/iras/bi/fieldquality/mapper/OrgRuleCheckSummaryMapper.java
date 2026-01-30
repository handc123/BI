package com.zjrcu.iras.bi.fieldquality.mapper;


import com.zjrcu.iras.bi.fieldquality.domain.model.OrgRuleCheckSummary;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 规则校验结果总（含核心指标及折线图数据）Mapper接口
 *
 * @author ruoyi
 * @date 2025-08-01
 */
@Mapper
public interface OrgRuleCheckSummaryMapper {

    OrgRuleCheckSummary selectRuleCheckSummaryLocalDate( String orgName);

    List<OrgRuleCheckSummary> selectOrgRuleCheckTrend(String orgName);
}
