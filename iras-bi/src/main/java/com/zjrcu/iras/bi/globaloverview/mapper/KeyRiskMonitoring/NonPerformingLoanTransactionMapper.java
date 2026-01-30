package com.zjrcu.iras.bi.globaloverview.mapper.KeyRiskMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyRiskMonitoring.NonPerformingLoanTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NonPerformingLoanTransactionMapper {
    List<NonPerformingLoanTransaction> selectNonPerformingLoanTransaction(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);
}
