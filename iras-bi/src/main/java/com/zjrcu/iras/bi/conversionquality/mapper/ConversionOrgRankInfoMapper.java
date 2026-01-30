package com.zjrcu.iras.bi.conversionquality.mapper;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionOrgRankInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversionOrgRankInfoMapper {
    List<ConversionOrgRankInfo> selectConversionOrgRankInfo(String rankType);

    List<ConversionOrgRankInfo> selectConversionOrgRankInfoPage(@Param("rankType") String rankType, @Param("dataDate") String dataDate);
}
