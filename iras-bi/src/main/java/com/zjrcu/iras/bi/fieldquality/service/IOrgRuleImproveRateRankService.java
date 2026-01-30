package com.zjrcu.iras.bi.fieldquality.service;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgRuleImproveRateRank;

import com.zjrcu.iras.bi.fieldquality.domain.vo.RuleTypeCountVo;

import java.util.List;



/**
 * 行社规则提升率排名（用于本月基础数据提升率排名及数据质量提升排名）Service接口
 *
 * @author ruoyi
 * @date 2025-07-31
 */
public interface IOrgRuleImproveRateRankService {

    List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRank();

    List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRankList(String dataDate);

    List<OrgRuleImproveRateRank> selectDataQualityImproveRank(String orgName, String compareType);


    List<RuleTypeCountVo> selectRuleTypeCount(String orgName);

    List<OrgRuleImproveRateRank> selectRuleTypePassRate(String orgName, String ruleTypeBig);

    List<OrgRuleImproveRateRank> selectRuleTypePassRateList(String orgName, String ruleTypeBig, String dataDate);

    List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRankListByDataDate(String dataDate);
}
