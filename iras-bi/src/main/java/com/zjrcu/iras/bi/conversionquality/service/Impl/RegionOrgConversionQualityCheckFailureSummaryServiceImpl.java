package com.zjrcu.iras.bi.conversionquality.service.Impl;

import com.zjrcu.iras.bi.conversionquality.domain.model.RegionOrgConversionQualityCheckFailureSummary;
import com.zjrcu.iras.bi.conversionquality.mapper.RegionOrgConversionQualityCheckFailureSummaryMapper;
import com.zjrcu.iras.bi.conversionquality.service.IRegionOrgConversionQualityCheckFailureSummaryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionOrgConversionQualityCheckFailureSummaryServiceImpl implements IRegionOrgConversionQualityCheckFailureSummaryService {
    @Resource
    private RegionOrgConversionQualityCheckFailureSummaryMapper regionOrgConversionQualityCheckFailureSummaryMapper;
    @Override
    public List<RegionOrgConversionQualityCheckFailureSummary> selectRegionOrgConversionQualityCheckFailureSummary(String regionName) {
        return regionOrgConversionQualityCheckFailureSummaryMapper.selectRegionOrgConversionQualityCheckFailureSummary(regionName);
    }
}
