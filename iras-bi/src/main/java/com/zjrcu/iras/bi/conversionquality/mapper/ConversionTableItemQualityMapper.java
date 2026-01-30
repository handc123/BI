package com.zjrcu.iras.bi.conversionquality.mapper;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionTableItemQuality;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConversionTableItemQualityMapper {
    List<ConversionTableItemQuality> selectOrgConversionTableQuality(String orgName);
}
