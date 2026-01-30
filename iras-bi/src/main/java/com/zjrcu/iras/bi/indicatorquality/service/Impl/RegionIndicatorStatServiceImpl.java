package com.zjrcu.iras.bi.indicatorquality.service.Impl;

import com.zjrcu.iras.bi.indicatorquality.domain.vo.RegionIndicatorRankVo;
import com.zjrcu.iras.bi.indicatorquality.mapper.RegionIndicatorStatMapper;
import com.zjrcu.iras.bi.indicatorquality.service.IRegionIndicatorStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionIndicatorStatServiceImpl implements IRegionIndicatorStatService {
    @Autowired
    private RegionIndicatorStatMapper regionIndicatorStatMapper;
    @Override
    public List<RegionIndicatorRankVo> selectRegionIndicatorDeviationRank() {
        return regionIndicatorStatMapper.selectRegionIndicatorDeviationRank();
    }
}
