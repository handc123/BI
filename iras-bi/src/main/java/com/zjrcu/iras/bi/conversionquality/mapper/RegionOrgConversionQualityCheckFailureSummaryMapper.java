package com.zjrcu.iras.bi.conversionquality.mapper;

import com.zjrcu.iras.bi.conversionquality.domain.model.RegionOrgConversionQualityCheckFailureSummary;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegionOrgConversionQualityCheckFailureSummaryMapper {
    List<RegionOrgConversionQualityCheckFailureSummary> selectRegionOrgConversionQualityCheckFailureSummary(String regionName);
}
