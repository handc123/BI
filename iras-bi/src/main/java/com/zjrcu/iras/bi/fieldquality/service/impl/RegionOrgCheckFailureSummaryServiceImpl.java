package com.zjrcu.iras.bi.fieldquality.service.impl;

import com.zjrcu.iras.bi.fieldquality.domain.vo.RegionOrgCheckFailureDisVo;
import com.zjrcu.iras.bi.fieldquality.domain.vo.RegionOrgCheckFailureSummaryVo;
import com.zjrcu.iras.bi.fieldquality.mapper.RegionOrgCheckFailureSummaryMapper;
import com.zjrcu.iras.bi.fieldquality.service.IRegionOrgCheckFailureSummaryService;
import com.zjrcu.iras.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地区行社未通过项（统计未通过规则项数和记录数）Service业务层处理
 *
 * @author ruoyi
 * @date 2025-07-30
 */
@Service
public class RegionOrgCheckFailureSummaryServiceImpl implements IRegionOrgCheckFailureSummaryService {
    @Autowired
    private RegionOrgCheckFailureSummaryMapper regionOrgCheckFailureSummaryMapper;


    @Override
    public RegionOrgCheckFailureSummaryVo selectRegionOrgCheckFailureSummaryByRegionName(String regionName) {
        if (regionName == null || regionName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return regionOrgCheckFailureSummaryMapper.selectRegionOrgCheckFailureSummaryByRegionName(regionName);
    }

    @Override
    public List<RegionOrgCheckFailureDisVo> selectRegionOrgCheckFailureDisByRegionName(String regionName) {
        if (regionName == null || regionName.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return regionOrgCheckFailureSummaryMapper.selectRegionOrgCheckFailureDisByRegionName(regionName);
    }
}
