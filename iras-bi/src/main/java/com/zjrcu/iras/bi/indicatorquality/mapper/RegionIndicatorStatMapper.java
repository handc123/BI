package com.zjrcu.iras.bi.indicatorquality.mapper;

import com.zjrcu.iras.bi.indicatorquality.domain.vo.RegionIndicatorRankVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegionIndicatorStatMapper {
    List<RegionIndicatorRankVo> selectRegionIndicatorDeviationRank();
}
