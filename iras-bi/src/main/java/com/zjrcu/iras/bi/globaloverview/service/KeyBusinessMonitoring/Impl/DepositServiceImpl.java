package com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.Deposit;
import com.zjrcu.iras.bi.globaloverview.mapper.KeyBusinessMonitoring.DepositMapper;
import com.zjrcu.iras.bi.globaloverview.service.KeyBusinessMonitoring.IDepositService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepositServiceImpl implements IDepositService {

    @Resource
    private DepositMapper depositMapper;
    @Override
    public List<Deposit> selectDeposit(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName()== null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return depositMapper.selectDeposit(globalOverviewRequestQO);
    }
}
