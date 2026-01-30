package com.zjrcu.iras.bi.globaloverview.mapper.NonPerformingMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.NonPerformingMonitoring.NonPerformingLoanTrendVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface NonPerformingLoanMapper {
    BigDecimal selectNonPerformingLoanRate(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<NonPerformingLoanTrendVo> selectNonPerformingLoanRateMonth(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);

    List<String> selectRegionByOrgName(@Param("orgName") String orgName);
}

