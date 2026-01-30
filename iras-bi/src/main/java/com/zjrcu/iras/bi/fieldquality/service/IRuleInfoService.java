package com.zjrcu.iras.bi.fieldquality.service;

import com.zjrcu.iras.bi.fieldquality.domain.model.RuleInfo;
import com.zjrcu.iras.bi.fieldquality.domain.vo.RuleInfoVo;





public interface IRuleInfoService {
    RuleInfo selectRuleInfo(String ruleCode);

    RuleInfoVo selectRuleInfoPassByRuleCode(String orgName, String ruleCode);
}
