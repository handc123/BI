package com.zjrcu.iras.bi.globaloverview.service.RealEstateMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateMonitoring.RealEstateLoanPerson;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateMonitoring.RealEstateLoanVo;
import com.zjrcu.iras.bi.globaloverview.mapper.RealEstateMonitoring.RealEstateLoanMapper;
import com.zjrcu.iras.bi.globaloverview.mapper.RealEstateMonitoring.RealEstateLoanPersonMapper;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateMonitoring.IRealEstateLoanService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RealEstateLoanServiceImpl implements IRealEstateLoanService {
    @Resource
    private RealEstateLoanMapper realEstateLoanMapper;
    @Resource
    private RealEstateLoanPersonMapper realEstateLoanPersonMapper;
    @Override
    public List<RealEstateLoanVo> selectRealEstateLoanBalance(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return realEstateLoanMapper.selectRealEstateLoanBalance(globalOverviewRequestQO);
    }

    @Override
    public List<RealEstateLoanPerson> selectRealEstateLoanPersonRatio(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getDataDate() == null || globalOverviewRequestQO.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return realEstateLoanPersonMapper.selectRealEstateLoanPersonRatio(globalOverviewRequestQO);
    }
}
