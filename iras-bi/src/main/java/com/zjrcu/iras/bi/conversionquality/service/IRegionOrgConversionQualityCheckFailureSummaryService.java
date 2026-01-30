package com.zjrcu.iras.bi.conversionquality.service;

import com.zjrcu.iras.bi.conversionquality.domain.model.RegionOrgConversionQualityCheckFailureSummary;

import java.util.List;

public interface IRegionOrgConversionQualityCheckFailureSummaryService {
    List<RegionOrgConversionQualityCheckFailureSummary> selectRegionOrgConversionQualityCheckFailureSummary(String regionName);
}
