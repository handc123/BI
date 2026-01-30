package com.zjrcu.iras.bi.indicatorquality.service.Impl;

import com.zjrcu.iras.bi.indicatorquality.domain.model.OrgIndicatorDeviationRank;
import com.zjrcu.iras.bi.indicatorquality.mapper.OrgIndicatorDeviationRankMapper;
import com.zjrcu.iras.bi.indicatorquality.service.IOrgIndicatorDeviationService;
import com.zjrcu.iras.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrgIndicatorDeviationServiceImpl implements IOrgIndicatorDeviationService {

    @Autowired
    private OrgIndicatorDeviationRankMapper orgIndicatorDeviationRankMapper;
    @Override
    public List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRank() {
        return orgIndicatorDeviationRankMapper.selectOrgIndicatorDeviationRank();
    }

    @Override
    public List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRankPage(String dataDate) {
        return orgIndicatorDeviationRankMapper.selectOrgIndicatorDeviationRankPage(dataDate);
    }

    @Override
    public List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRankByOrgName(String orgName) {
        Date date = new Date();
        Date dataDate = DateUtils.addDays(date, -1);
        String localDate = DateUtils.parseDateToStr("yyyy-MM-dd", dataDate);
        return orgIndicatorDeviationRankMapper.selectOrgIndicatorDeviationRankByOrgName(orgName, localDate)
                .stream().limit(10).collect(Collectors.toList());
    }

    @Override
    public List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRankPageByOrgName(String orgName, String dataDate) {
        return orgIndicatorDeviationRankMapper.selectOrgIndicatorDeviationRankByOrgName(orgName, dataDate);
    }
}
