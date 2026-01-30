package com.zjrcu.iras.bi.globaloverview.mapper.FiveMajorArticlesMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.FiveMajorArticlesMonitoring.StrategicLoan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategicLoanMapper {
    List<StrategicLoan> selectStrategicLoan(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);
}
