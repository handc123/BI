package com.zjrcu.iras.bi.indicatorquality.service;

import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorBarChartVo;
import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorDevationCountVo;
import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorDeviationTrendVo;

import java.util.List;

public interface IOrgIndicatorService {
    List<OrgIndicatorBarChartVo> selectOrgIndicatorClassifyCount(String regionName);

    List<OrgIndicatorDevationCountVo> selectOrgIndicatorDeviationCountByOrgName(String orgName);

    OrgIndicatorDeviationTrendVo selectOrgIndicatorDeviationTrend(String orgName);
}
