package com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.InvestmentBusinessStructure;
import com.zjrcu.iras.bi.globaloverview.mapper.KeyBusinessMonitoring.InvestmentBusinessStructureMapper;
import com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.IInvestmentBusinessStructureService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestmentBusinessStructureServiceImpl implements IInvestmentBusinessStructureService {

    @Resource
    private InvestmentBusinessStructureMapper investmentBusinessStructureMapper;
    @Override
    public List<InvestmentBusinessStructure> selectInvestmentBusinessStructure(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getDataDate() == null || globalOverviewRequestQO.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return investmentBusinessStructureMapper.selectInvestmentBusinessStructure(globalOverviewRequestQO);
    }
}
