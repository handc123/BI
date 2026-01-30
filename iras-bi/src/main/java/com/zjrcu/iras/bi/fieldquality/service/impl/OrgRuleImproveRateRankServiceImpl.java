package com.zjrcu.iras.bi.fieldquality.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgRuleImproveRateRank;

import com.zjrcu.iras.bi.fieldquality.domain.vo.RuleTypeCountVo;

import com.zjrcu.iras.bi.fieldquality.mapper.OrgRuleImproveRateRankMapper;
import com.zjrcu.iras.bi.fieldquality.service.IOrgRuleImproveRateRankService;
import com.zjrcu.iras.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 行社规则提升率排名（用于本月基础数据提升率排名及数据质量提升排名）Service业务层处理
 *
 * @author ruoyi
 * @date 2025-07-31
 */
@Service
public class OrgRuleImproveRateRankServiceImpl implements IOrgRuleImproveRateRankService {
    @Autowired
    private OrgRuleImproveRateRankMapper orgRuleImproveRateRankMapper;


    @Override
    public List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRank() {
        List<OrgRuleImproveRateRank> orgRuleImproveRateRank =orgRuleImproveRateRankMapper.selectOrgRuleImproveRateRank();
        return orgRuleImproveRateRank.stream().limit(10).collect(Collectors.toList());
    }

    @Override
    public List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRankList(String dataDate) {
        return orgRuleImproveRateRankMapper.selectOrgRuleImproveRateRankListByDataDate(dataDate);
    }

    @Override
    public List<OrgRuleImproveRateRank> selectDataQualityImproveRank(String orgName, String compareType) {
        if (orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (compareType == null || compareType.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        List<OrgRuleImproveRateRank> orgRuleImproveRateRank =orgRuleImproveRateRankMapper.selectOrgRuleImproveRateRankByOrgName(orgName, compareType);
        return orgRuleImproveRateRank.stream().limit(20).collect(Collectors.toList());
    }





    @Override
    public List<RuleTypeCountVo> selectRuleTypeCount(String orgName) {
        if ( orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return orgRuleImproveRateRankMapper.selectRuleTypeCount(orgName);
    }

    @Override
    public List<OrgRuleImproveRateRank> selectRuleTypePassRate(String orgName, String ruleTypeBig) {
        if (orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (ruleTypeBig == null || ruleTypeBig.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        List<OrgRuleImproveRateRank> orgRuleImproveRateRanks = orgRuleImproveRateRankMapper.selectRuleTypePassRate(orgName, ruleTypeBig);
        return orgRuleImproveRateRanks.stream().limit(10).collect(Collectors.toList());
    }

    @Override
    public List<OrgRuleImproveRateRank> selectRuleTypePassRateList(String orgName, String ruleTypeBig, String dataDate) {
        if (orgName == null || orgName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        if (ruleTypeBig == null || ruleTypeBig.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return orgRuleImproveRateRankMapper.selectRuleTypePassRateByDataDate(orgName, ruleTypeBig, dataDate);
    }

    @Override
    public List<OrgRuleImproveRateRank> selectOrgRuleImproveRateRankListByDataDate(String dataDate) {
        return orgRuleImproveRateRankMapper.selectOrgRuleImproveRateRankListByDataDate(dataDate);
    }
}
