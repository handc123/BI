package com.zjrcu.iras.bi.conversionquality.mapper;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionQualitySummary;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConversionQualitySummaryMapper {
    List<ConversionQualitySummary> selectPassRateStat();
}
