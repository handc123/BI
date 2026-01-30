package com.zjrcu.iras.bi.conversionquality.mapper;

import com.zjrcu.iras.bi.conversionquality.domain.model.RegionConversionQualityRank;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegionConversionQualityRankMapper {
    List<RegionConversionQualityRank> selectRegionConversionQualityRank();
}
