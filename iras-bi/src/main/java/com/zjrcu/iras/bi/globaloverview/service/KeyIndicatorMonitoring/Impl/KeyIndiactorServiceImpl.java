package com.zjrcu.iras.bi.globaloverview.service.KeyIndicatorMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyIndicatorMonitoring.KeyIndicator;
import com.zjrcu.iras.bi.globaloverview.mapper.KeyIndicatorMonitoring.KeyIndicatorMapper;
import com.zjrcu.iras.bi.globaloverview.service.KeyIndicatorMonitoring.IKeyIndicatorService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeyIndiactorServiceImpl implements IKeyIndicatorService {

    @Resource
    private KeyIndicatorMapper keyIndicatorMapper;
    @Override
    public List<KeyIndicator> selectKeyIndicator(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getDataDate() == null || globalOverviewRequestQO.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return keyIndicatorMapper.selectKeyIndicator(globalOverviewRequestQO);
    }
}
