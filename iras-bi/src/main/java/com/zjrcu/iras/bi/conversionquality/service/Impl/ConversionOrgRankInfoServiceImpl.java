package com.zjrcu.iras.bi.conversionquality.service.Impl;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionOrgRankInfo;
import com.zjrcu.iras.bi.conversionquality.mapper.ConversionOrgRankInfoMapper;
import com.zjrcu.iras.bi.conversionquality.service.IConversionOrgRankInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversionOrgRankInfoServiceImpl implements IConversionOrgRankInfoService {
    @Resource
    private ConversionOrgRankInfoMapper conversionOrgRankInfoMapper;
    @Override
    public List<ConversionOrgRankInfo> selectConversionOrgRankInfo(String rankType) {
        return conversionOrgRankInfoMapper.selectConversionOrgRankInfo(rankType)
                .stream().limit(10).collect(Collectors.toList());
    }

    @Override
    public List<ConversionOrgRankInfo> selectConversionOrgRankInfoPage(String rankType, String dataDate) {
        return conversionOrgRankInfoMapper.selectConversionOrgRankInfoPage(rankType,dataDate);
    }
}
