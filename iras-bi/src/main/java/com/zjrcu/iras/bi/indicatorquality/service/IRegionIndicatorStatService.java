package com.zjrcu.iras.bi.indicatorquality.service;

import com.zjrcu.iras.bi.indicatorquality.domain.vo.RegionIndicatorRankVo;

import java.util.List;

public interface IRegionIndicatorStatService {
    List<RegionIndicatorRankVo> selectRegionIndicatorDeviationRank();
}
