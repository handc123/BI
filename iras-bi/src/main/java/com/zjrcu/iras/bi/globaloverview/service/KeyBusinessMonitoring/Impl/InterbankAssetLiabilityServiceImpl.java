package com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.InterbankAssetLiability;
import com.zjrcu.iras.bi.globaloverview.mapper.KeyBusinessMonitoring.InterbankAssetLiabilityMapper;
import com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.IInterbankAssetLiabilityService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterbankAssetLiabilityServiceImpl implements IInterbankAssetLiabilityService {

    @Resource
    private InterbankAssetLiabilityMapper interbankAssetLiabilityMapper;
    @Override
    public List<InterbankAssetLiability> selectInterbankAssetLiability(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getDataDate() == null || globalOverviewRequestQO.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return interbankAssetLiabilityMapper.selectInterbankAssetLiability(globalOverviewRequestQO);
    }
}
