package com.zjrcu.iras.bi.conversionquality.service.Impl;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionQualitySummary;
import com.zjrcu.iras.bi.conversionquality.mapper.ConversionQualitySummaryMapper;
import com.zjrcu.iras.bi.conversionquality.service.IConversionQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversionQualityServiceImpl implements IConversionQualityService {
    @Autowired
    private ConversionQualitySummaryMapper conversionQualitySummaryMapper;
    @Override
    public List<ConversionQualitySummary> selectPassRateStat() {
        return conversionQualitySummaryMapper.selectPassRateStat();
    }
}
