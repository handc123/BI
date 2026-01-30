package com.zjrcu.iras.bi.indicatorquality.service.Impl;

import com.zjrcu.iras.bi.indicatorquality.domain.model.IndicatorDeviationStat;
import com.zjrcu.iras.bi.indicatorquality.mapper.IndicatorDeviationStatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndicatorDeviationStatServiceImpl implements com.zjrcu.iras.bi.indicatorquality.service.IIndicatorDeviationStatService {
    @Autowired
    private IndicatorDeviationStatMapper indicatorDeviationStatMapper;


    @Override
    public List<IndicatorDeviationStat> selectIndicatorDeviationStat(String indicatorDeviationType) {
        return indicatorDeviationStatMapper.selectIndicatorDeviationStat(indicatorDeviationType);
    }
}
