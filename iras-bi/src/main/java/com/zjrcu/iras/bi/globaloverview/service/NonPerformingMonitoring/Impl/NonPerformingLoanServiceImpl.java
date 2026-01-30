package com.zjrcu.iras.bi.globaloverview.service.NonPerformingMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.NonPerformingMonitoring.NonPerformingLoanTrendVo;
import com.zjrcu.iras.bi.globaloverview.mapper.NonPerformingMonitoring.NonPerformingLoanMapper;
import com.zjrcu.iras.bi.globaloverview.service.NonPerformingMonitoring.INonPerformingLoanService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.system.mapper.SysDeptMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NonPerformingLoanServiceImpl implements INonPerformingLoanService {
    @Resource
    private NonPerformingLoanMapper nonPerformingLoanMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Override
    public BigDecimal selectNonPerformingLoanRate(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return nonPerformingLoanMapper.selectNonPerformingLoanRate(globalOverviewRequestQO);
    }

    @Override
    public List<NonPerformingLoanTrendVo> selectNonPerformingLoanRateMonth(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return nonPerformingLoanMapper.selectNonPerformingLoanRateMonth(globalOverviewRequestQO);
    }

    @Override
    public List<String> selectRegionByOrgName(String orgName) {
        return nonPerformingLoanMapper.selectRegionByOrgName(orgName);
    }

}
