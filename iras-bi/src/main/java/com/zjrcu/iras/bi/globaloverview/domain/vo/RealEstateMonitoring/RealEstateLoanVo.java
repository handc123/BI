package com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateMonitoring;

import java.math.BigDecimal;

public class RealEstateLoanVo {

    /**
     * 数据日期
     */
    private String dataDate;
    /**
     * 总贷款余额
     */
    private BigDecimal totalRealEstateLoanBalance;
    /**
     * 国企房地产贷款余额
     */
    private BigDecimal stateOwnedRealEstateLoanBalance;
    /**
     * 民企房地产贷款余额
     */
    private BigDecimal privateRealEstateLoanBalance;

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public BigDecimal getTotalRealEstateLoanBalance() {
        return totalRealEstateLoanBalance;
    }

    public void setTotalRealEstateLoanBalance(BigDecimal totalRealEstateLoanBalance) {
        this.totalRealEstateLoanBalance = totalRealEstateLoanBalance;
    }

    public BigDecimal getStateOwnedRealEstateLoanBalance() {
        return stateOwnedRealEstateLoanBalance;
    }

    public void setStateOwnedRealEstateLoanBalance(BigDecimal stateOwnedRealEstateLoanBalance) {
        this.stateOwnedRealEstateLoanBalance = stateOwnedRealEstateLoanBalance;
    }

    public BigDecimal getPrivateRealEstateLoanBalance() {
        return privateRealEstateLoanBalance;
    }

    public void setPrivateRealEstateLoanBalance(BigDecimal privateRealEstateLoanBalance) {
        this.privateRealEstateLoanBalance = privateRealEstateLoanBalance;
    }


}
