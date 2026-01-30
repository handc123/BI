package com.zjrcu.iras.bi.indicatorquality.service.Impl;

import com.zjrcu.iras.bi.indicatorquality.domain.model.OrgIndicatorRank;
import com.zjrcu.iras.bi.indicatorquality.mapper.OrgIndicatorRankMapper;
import com.zjrcu.iras.bi.indicatorquality.service.IOrgIndicatorRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrgIndicatorRankServiceImpl implements IOrgIndicatorRankService {
    @Autowired
    private OrgIndicatorRankMapper orgIndicatorRankMapper;
    @Override
    public List<OrgIndicatorRank> selectOrgIndicatorRank(String rankType) {
        List<OrgIndicatorRank> orgIndicatorRanks = orgIndicatorRankMapper.selectOrgIndicatorRank(rankType);
        return orgIndicatorRanks.stream().limit(10).collect(Collectors.toList());
    }

    @Override
    public List<OrgIndicatorRank> selectOrgIndicatorRankPage(String rankType, String dateDate) {
        return orgIndicatorRankMapper.selectOrgIndicatorRankPage(rankType, dateDate);
    }
}
