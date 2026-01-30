package com.zjrcu.iras.bi.indicatorquality.service;

import com.zjrcu.iras.bi.indicatorquality.domain.model.OrgIndicatorDeviationRank;

import java.util.List;

public interface IOrgIndicatorDeviationService {
    List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRank();

    List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRankPage(String dateDate);

    List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRankByOrgName(String orgName);

    List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRankPageByOrgName(String orgName, String dataDate);
}
