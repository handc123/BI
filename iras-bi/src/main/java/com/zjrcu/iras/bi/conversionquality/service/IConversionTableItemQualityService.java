package com.zjrcu.iras.bi.conversionquality.service;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionTableItemQuality;

import java.util.List;

public interface IConversionTableItemQualityService {
    List<ConversionTableItemQuality> selectOrgConversionTableQuality(String orgName);
}
