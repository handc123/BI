package com.zjrcu.iras.bi.fieldquality.service.impl;

import com.zjrcu.iras.bi.fieldquality.domain.model.RuleInfo;
import com.zjrcu.iras.bi.fieldquality.domain.vo.RuleInfoVo;
import com.zjrcu.iras.bi.fieldquality.mapper.RuleInfoMapper;
import com.zjrcu.iras.bi.fieldquality.service.IRuleInfoService;
import com.zjrcu.iras.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RuleInfoServiceImpl implements IRuleInfoService {
    @Autowired
    RuleInfoMapper ruleInfoMapper;
    @Override
    public RuleInfo selectRuleInfo(String ruleCode) {
        if (ruleCode == null || ruleCode.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return ruleInfoMapper.selectRuleInfoByRuleCode(ruleCode);
    }

    @Override
    public RuleInfoVo selectRuleInfoPassByRuleCode(String orgName, String ruleCode) {
        if (orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (ruleCode == null || ruleCode.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return ruleInfoMapper.selectRuleInfoPassByRuleCode(orgName, ruleCode);
    }


}
