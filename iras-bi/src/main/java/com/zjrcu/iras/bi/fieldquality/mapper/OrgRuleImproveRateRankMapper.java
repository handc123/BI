package com.zjrcu.iras.bi.fieldquality.mapper;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgRuleImproveRateRank;
import com.zjrcu.iras.bi.fieldquality.domain.vo.RuleTypeCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;



/**
 * 行社规则提升率排名（用于本月基础数据提升率排名及数据质量提升排名）Mapper接口
 *
 * @author ruoyi
 * @date 2025-07-31
 */
@Mapper
public interface OrgRuleImproveRateRankMapper {

    List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRank();

    List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRankByOrgName(@Param("orgName") String orgName, @Param("compareType") String compareType);



    List<RuleTypeCountVo> selectRuleTypeCount(String orgName);

    List<OrgRuleImproveRateRank> selectRuleTypePassRate(@Param("orgName") String orgName, @Param("ruleTypeBig") String ruleTypeBig);

    List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRankListByDataDate(String dataDate);

    List<OrgRuleImproveRateRank> selectRuleTypePassRateByDataDate(@Param("orgName") String orgName, @Param("ruleTypeBig") String ruleTypeBig, @Param("dataDate") String dataDate);
}
