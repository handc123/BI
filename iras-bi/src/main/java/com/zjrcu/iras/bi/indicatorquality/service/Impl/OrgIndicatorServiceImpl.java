package com.zjrcu.iras.bi.indicatorquality.service.Impl;

import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorBarChartVo;
import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorDevationCountVo;
import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorDeviationTrendVo;
import com.zjrcu.iras.bi.indicatorquality.mapper.OrgIndicatorMapper;
import com.zjrcu.iras.bi.indicatorquality.service.IOrgIndicatorService;
import com.zjrcu.iras.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgIndicatorServiceImpl implements IOrgIndicatorService {
    @Autowired
    private OrgIndicatorMapper orgIndicatorMapper;
    @Override
    public List<OrgIndicatorBarChartVo> selectOrgIndicatorClassifyCount(String regionName) {
        return orgIndicatorMapper.selectOrgIndicatorClassifyCount(regionName);
    }

    @Override
    public List<OrgIndicatorDevationCountVo> selectOrgIndicatorDeviationCountByOrgName(String orgName) {
        if (orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return orgIndicatorMapper.selectOrgIndicatorDeviationCountByOrgName(orgName);
    }

    @Override
    public OrgIndicatorDeviationTrendVo selectOrgIndicatorDeviationTrend(String orgName) {
        if (orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        OrgIndicatorDeviationTrendVo vo = new OrgIndicatorDeviationTrendVo();
        vo.setNoDeviation(orgIndicatorMapper.selectNoDeviationTrend(orgName));
        vo.setDeviationLt2(orgIndicatorMapper.selectDeviationLt2Trend(orgName));
        vo.setDeviationGt2(orgIndicatorMapper.selectDeviationGt2Trend(orgName));
        return vo;
    }
}
