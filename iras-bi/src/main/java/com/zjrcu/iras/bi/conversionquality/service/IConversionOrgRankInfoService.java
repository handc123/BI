package com.zjrcu.iras.bi.conversionquality.service;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionOrgRankInfo;

import java.util.List;

public interface IConversionOrgRankInfoService {
    List<ConversionOrgRankInfo> selectConversionOrgRankInfo(String rankType);

    List<ConversionOrgRankInfo> selectConversionOrgRankInfoPage(String rankType, String dataDate);
}
