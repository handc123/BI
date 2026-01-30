package com.zjrcu.iras.bi.fieldquality.service.impl;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgRuleCheckSummary;
import com.zjrcu.iras.bi.fieldquality.mapper.OrgRuleCheckSummaryMapper;
import com.zjrcu.iras.bi.fieldquality.service.IOrgRuleCheckSummaryService;
import com.zjrcu.iras.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgRuleCheckSummaryServiceImpl implements IOrgRuleCheckSummaryService {
    @Autowired
    private OrgRuleCheckSummaryMapper orgRuleCheckSummaryMapper;
    @Override
    public OrgRuleCheckSummary selectRuleCheckSummary(String orgName) {
        if (orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return orgRuleCheckSummaryMapper.selectRuleCheckSummaryLocalDate(orgName);
    }

    @Override
    public List<OrgRuleCheckSummary> selectOrgRuleCheckTrend(String orgName) {
        if (orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return orgRuleCheckSummaryMapper.selectOrgRuleCheckTrend(orgName);
    }
}
