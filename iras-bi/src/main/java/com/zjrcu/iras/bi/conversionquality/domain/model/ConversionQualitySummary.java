package com.zjrcu.iras.bi.conversionquality.domain.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 行社转换质量汇总对象 conversion_quality_summary
 *
 * @author ruoyi
 * @date 2025-08-07
 */
public class ConversionQualitySummary extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 数据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "数据日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dataDate;

    /**
     * 通过的表格数量
     */
    @Excel(name = "通过的表格数量")
    private Long passedTableCount;

    /**
     * 行社通过率
     */
    @Excel(name = "行社通过率")
    private BigDecimal orgPassRate;

    /**
     * 行社通过率分档（例："0-20%"、"20-40%"、"40-60%"...）
     */
    @Excel(name = "行社通过率分档", readConverterExp = "例：0-20%、20-40%、40-60%...")
    private String passRange;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
    @Excel(name = "总表数量")
    private Long totalCount;



    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setPassedTableCount(Long passedTableCount) {
        this.passedTableCount = passedTableCount;
    }

    public Long getPassedTableCount() {
        return passedTableCount;
    }

    public void setOrgPassRate(BigDecimal orgPassRate) {
        this.orgPassRate = orgPassRate;
    }

    public BigDecimal getOrgPassRate() {
        return orgPassRate;
    }

    public void setPassRange(String passRange) {
        this.passRange = passRange;
    }

    public String getPassRange() {
        return passRange;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("dataDate", getDataDate())
                .append("passedTableCount", getPassedTableCount())
                .append("orgPassRate", getOrgPassRate())
                .append("passRange", getPassRange())
                .toString();
    }
}
