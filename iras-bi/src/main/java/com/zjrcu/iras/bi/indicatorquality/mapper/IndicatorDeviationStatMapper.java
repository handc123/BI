package com.zjrcu.iras.bi.indicatorquality.mapper;

import com.zjrcu.iras.bi.indicatorquality.domain.model.IndicatorDeviationStat;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IndicatorDeviationStatMapper {



    List<IndicatorDeviationStat> selectIndicatorDeviationStat(String indicatorDeviationType);
}
