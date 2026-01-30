package com.zjrcu.iras.bi.conversionquality.service;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionQualitySummary;

import java.util.List;

public interface IConversionQualityService {
    List<ConversionQualitySummary> selectPassRateStat();
}
