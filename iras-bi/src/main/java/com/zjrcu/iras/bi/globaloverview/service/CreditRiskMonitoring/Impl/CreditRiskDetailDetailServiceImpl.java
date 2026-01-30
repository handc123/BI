package com.zjrcu.iras.bi.globaloverview.service.CreditRiskMonitoring.Impl;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.CreditRiskMonitoring.*;
import com.zjrcu.iras.bi.globaloverview.mapper.CreditRiskMonitoring.CreditRiskDetailMapper;
import com.zjrcu.iras.bi.globaloverview.service.CreditRiskMonitoring.ICreditRiskDetailService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditRiskDetailDetailServiceImpl implements ICreditRiskDetailService {

    @Resource
    private CreditRiskDetailMapper creditRiskDetailMapper;

    @Override
    public List<OverdueLoanVo> selectOverdueLoanPeriod(GlobalOverviewRequestQO qo) {
        if (qo.getRegionName() == null || qo.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (qo.getOverduePeriod() ==  null){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectOverdueLoanPeriod(qo);
    }

    @Override
    public List<OverdueLoanProportionVo> selectOverdueLoanProportion(GlobalOverviewRequestQO qo) {
        if (qo.getRegionName() == null || qo.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectOverdueLoanProportion(qo);
    }

    @Override
    public List<NonPerformingLoanIndustryVo> selectNonPerformingLoanByIndustry(GlobalOverviewRequestQO qo) {
        if (qo.getRegionName() == null || qo.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (qo.getDataDate() == null || qo.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectNonPerformingLoanByIndustry(qo);
    }

    @Override
    public List<NonPerformingLoanRegionVo> selectNonPerformingLoanByRegion(GlobalOverviewRequestQO qo) {
        if (qo.getDataDate() == null || qo.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectNonPerformingLoanByRegion(qo);
    }

    @Override
    public List<NonPerformingLoanGuaranteeVo> selectNonPerformingLoanByGuaranteeType(GlobalOverviewRequestQO qo) {
        if (qo.getRegionName() == null || qo.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (qo.getDataDate() == null || qo.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectNonPerformingLoanByGuaranteeType(qo);
    }

    @Override
    public List<NonPerformingLoanCunstomerVo> selectNonPerformingLoanByCustomerType(GlobalOverviewRequestQO qo) {
        if (qo.getRegionName() == null || qo.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (qo.getDataDate() == null || qo.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectNonPerformingLoanByCustomerType(qo);
    }

    @Override
    public List<DisposalAmountTypeVo> selectDisposalAmountByType(GlobalOverviewRequestQO qo) {
        if (qo.getRegionName() == null || qo.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectDisposalAmountByType(qo);
    }

    @Override
    public List<CharacteristicLoanVo> selectSpecialLoan(GlobalOverviewRequestQO qo) {
        if (qo.getRegionName() == null || qo.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (qo.getCharacterType() == null || qo.getCharacterType().isEmpty()){
            throw new ServiceException("特色贷款类型不能为空");
        }
        return creditRiskDetailMapper.selectSpecialLoan(qo);
    }

    @Override
    public List<NonPerformingBalanceRatioVo> selectNonPerformingLoanSummary(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectNonPerformingLoanSummary(globalOverviewRequestQO);
    }

    @Override
    public List<DailyNewNplAmountVo> selectDailyNewNplAmount(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectDailyNewNplAmount(globalOverviewRequestQO);
    }

    @Override
    public List<DailyNewNplAmountOrgNameVo> selectDailyNewNplAmountOrgName(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getOrgName() == null || globalOverviewRequestQO.getOrgName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getDataDate() == null || globalOverviewRequestQO.getDataDate().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (globalOverviewRequestQO.getOrgName().equals("浙江农商联合银行")){
            return creditRiskDetailMapper.selectDailyNewNplAmountOrgName(globalOverviewRequestQO);
        }
        return null;
    }

    @Override
    public List<DisposalAmountVo> selectDisposalAmount(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectDisposalAmount(globalOverviewRequestQO);
    }

    @Override
    public List<OverdueLoanSummaryVo> selectOverdueLoanCondition(GlobalOverviewRequestQO globalOverviewRequestQO) {
        if (globalOverviewRequestQO.getRegionName() == null || globalOverviewRequestQO.getRegionName().isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return creditRiskDetailMapper.selectOverdueLoanCondition(globalOverviewRequestQO);
    }

}
