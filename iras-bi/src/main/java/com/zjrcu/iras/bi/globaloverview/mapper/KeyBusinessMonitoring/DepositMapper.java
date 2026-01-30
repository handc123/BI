package com.zjrcu.iras.bi.globaloverview.mapper.KeyBusinessMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.KeyBusinessMonitoring.Deposit;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DepositMapper {
    List<Deposit> selectDeposit(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);
}
