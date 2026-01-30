package com.zjrcu.iras.bi.indicatorquality.service;

import com.zjrcu.iras.bi.indicatorquality.domain.model.IndicatorDeviationStat;

import java.util.List;

public interface IIndicatorDeviationStatService {




    List<IndicatorDeviationStat> selectIndicatorDeviationStat(String indicatorDeviationType);


}
