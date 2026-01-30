package com.zjrcu.iras.bi.fieldquality.mapper;

import com.zjrcu.iras.bi.fieldquality.domain.model.RuleInfo;
import com.zjrcu.iras.bi.fieldquality.domain.vo.RuleInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * 校验规则（支持拉链多版本管理，记录规则的创建、更新及历史版本）Mapper接口
 *
 * @author ruoyi
 * @date 2025-08-01
 */
@Mapper
public interface RuleInfoMapper {

    RuleInfo selectRuleInfoByRuleCode(String ruleCode);

    RuleInfoVo selectRuleInfoPassByRuleCode(@Param("orgName") String orgName, @Param("ruleCode") String ruleCode);


}
