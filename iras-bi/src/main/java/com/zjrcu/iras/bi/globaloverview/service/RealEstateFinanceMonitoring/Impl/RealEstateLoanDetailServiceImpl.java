package com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateFinanceMonitoring.RealEstateLoanDetail;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.PersonRealEstateLoanVo;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.RealEstateLoanCategoryVO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring.RealEstateNonPerformingLoanVo;
import com.zjrcu.iras.bi.globaloverview.mapper.RealEstateFinanceMonitoring.RealEstateLoanDetailMapper;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.IRealEstateLoanDetailService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RealEstateLoanDetailServiceImpl implements IRealEstateLoanDetailService {

    @Resource
    private RealEstateLoanDetailMapper realEstateLoanDetailMapper;
    @Override
    public List<RealEstateLoanDetail> selectRealEstateLoanSummary(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return realEstateLoanDetailMapper.selectRealEstateLoanSummary(globalOverviewRequestQO);
    }

    @Override
    public List<RealEstateLoanDetail> selectRealEstateCollateralLoan(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return realEstateLoanDetailMapper.selectRealEstateCollateralLoan(globalOverviewRequestQO);
    }

    @Override
    public List<RealEstateLoanCategoryVO> selectRealEstateLoanIssuanceByGroup(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getGroupName() == null || globalOverviewRequestQO.getGroupName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return realEstateLoanDetailMapper.selectRealEstateLoanIssuanceByGroup(globalOverviewRequestQO);
    }

    @Override
    public List<PersonRealEstateLoanVo> selectPersonRealEstateLoanSummary(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return realEstateLoanDetailMapper.selectPersonRealEstateLoanSummary(globalOverviewRequestQO);
    }

    @Override
    public List<RealEstateNonPerformingLoanVo> selectRealEstateNonPerformingLoan(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return realEstateLoanDetailMapper.selectRealEstateNonPerformingLoan(globalOverviewRequestQO);
    }


}
