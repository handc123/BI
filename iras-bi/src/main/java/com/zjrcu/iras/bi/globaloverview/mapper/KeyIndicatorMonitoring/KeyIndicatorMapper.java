package com.zjrcu.iras.bi.globaloverview.mapper.KeyIndicatorMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyIndicatorMonitoring.KeyIndicator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KeyIndicatorMapper {
    List<KeyIndicator> selectKeyIndicator(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);
}
