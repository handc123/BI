package com.zjrcu.iras.bi.fieldquality.service;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgRuleCheckSummary;

import java.util.List;

public interface IOrgRuleCheckSummaryService {
    OrgRuleCheckSummary selectRuleCheckSummary(String orgName);

    List<OrgRuleCheckSummary> selectOrgRuleCheckTrend(String orgName);
}
