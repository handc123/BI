package com.zjrcu.iras.bi.globaloverview.service.FiveMajorArticlesMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.FiveMajorArticlesMonitoring.StrategicLoan;
import com.zjrcu.iras.bi.globaloverview.mapper.FiveMajorArticlesMonitoring.StrategicLoanMapper;
import com.zjrcu.iras.bi.globaloverview.service.FiveMajorArticlesMonitoring.IStrategicLoanService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StrategicLoanServiceImpl implements IStrategicLoanService {
    @Resource
    private StrategicLoanMapper  strategicLoanMapper;
    @Override
    public List<StrategicLoan> selectStrategicLoan(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getFinanceType() == null || globalOverviewRequestQO.getFinanceType().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return strategicLoanMapper.selectStrategicLoan(globalOverviewRequestQO);
    }
}
