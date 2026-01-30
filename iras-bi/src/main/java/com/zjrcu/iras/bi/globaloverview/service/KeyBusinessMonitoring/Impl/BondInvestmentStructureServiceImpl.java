package com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.BondInvestmentStructure;
import com.zjrcu.iras.bi.globaloverview.mapper.KeyBusinessMonitoring.BondInvestmentStructureMapper;
import com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.IBondInvestmentStructureService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BondInvestmentStructureServiceImpl implements IBondInvestmentStructureService {

    @Resource
    private BondInvestmentStructureMapper bondInvestmentStructureMapper;
    @Override
    public List<BondInvestmentStructure> selectBondInvestmentStructure(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getDataDate() == null || globalOverviewRequestQO.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return bondInvestmentStructureMapper.selectBondInvestmentStructure(globalOverviewRequestQO);
    }
}
