package com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateFinanceMonitoring.RealEstateLoanIssuance;
import com.zjrcu.iras.bi.globaloverview.mapper.RealEstateFinanceMonitoring.RealEstateLoanIssuanceMapper;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.IRealEstateLoanIssuanceService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RealEstateLoanIssuanceServiceImpl implements IRealEstateLoanIssuanceService {
    @Resource
    private RealEstateLoanIssuanceMapper realEstateLoanIssuanceMapper;
    @Override
    public List<RealEstateLoanIssuance> selectRealEstateLoanIssuance(GlobalOverviewRequestQO globalOverviewRequestQO) {

        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()) {
            throw new ServiceException("地区名称不能为空");
        }
        if (globalOverviewRequestQO.getOwnershipType() == null || globalOverviewRequestQO.getOwnershipType().isEmpty()) {
            throw new ServiceException("贷款类型不能为空");
        }
        return realEstateLoanIssuanceMapper.selectRealEstateLoanIssuance(globalOverviewRequestQO);
    }

    @Override
    public List<RealEstateLoanIssuance> selectAllRealEstateLoanIssuance(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()) {
            throw new ServiceException("地区名称不能为空");
        }
        return realEstateLoanIssuanceMapper.selectAllRealEstateLoanIssuance(globalOverviewRequestQO);
    }


}
