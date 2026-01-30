package com.zjrcu.iras.bi.conversionquality.service.Impl;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionTableItemQuality;
import com.zjrcu.iras.bi.conversionquality.mapper.ConversionTableItemQualityMapper;
import com.zjrcu.iras.bi.conversionquality.service.IConversionTableItemQualityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversionTableItemQualityServiceImpl implements IConversionTableItemQualityService {

    @Resource
    private ConversionTableItemQualityMapper conversionTableItemQualityMapper;
    @Override
    public List<ConversionTableItemQuality> selectOrgConversionTableQuality(String orgName) {
        return conversionTableItemQualityMapper.selectOrgConversionTableQuality(orgName);
    }
}
