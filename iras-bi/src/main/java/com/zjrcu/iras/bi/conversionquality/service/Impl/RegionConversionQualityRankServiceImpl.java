package com.zjrcu.iras.bi.conversionquality.service.Impl;

import com.zjrcu.iras.bi.conversionquality.domain.model.RegionConversionQualityRank;
import com.zjrcu.iras.bi.conversionquality.mapper.RegionConversionQualityRankMapper;
import com.zjrcu.iras.bi.conversionquality.service.IRegionConversionQualityRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionConversionQualityRankServiceImpl implements IRegionConversionQualityRankService {
    @Autowired
    private RegionConversionQualityRankMapper regionConversionQualityRankMapper;
    @Override
    public List<RegionConversionQualityRank> selectRegionConversionQualityRank() {
        return regionConversionQualityRankMapper.selectRegionConversionQualityRank();
    }
}
