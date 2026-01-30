package com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateFinanceMonitoring;

import java.math.BigDecimal;

public class RealEstateLoanCategoryVO {
    /**
     * 房地产贷款类别
     */
    private String category;
    /**
     * 贷款余额
     */
    private BigDecimal loanBalance;

    public BigDecimal getLoanBalance() {
        return loanBalance;
    }

    public void setLoanBalance(BigDecimal loanBalance) {
        this.loanBalance = loanBalance;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


}
