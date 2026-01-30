package com.zjrcu.iras.bi.indicatorquality.domain.model;

import java.util.Date;

import com.zjrcu.iras.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.zjrcu.iras.common.annotation.Excel;

/**
 * 指标偏离统计（记录各指标在不同月份的行社偏离情况）对象 indicator_deviation_stat
 *
 * @author ruoyi
 * @date 2025-08-05
 */
public class IndicatorDeviationStat extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 数据日期
     */
    @Excel(name = "数据日期")
    private Date dataDate;

    /**
     * 指标ID
     */
    @Excel(name = "指标ID")
    private Long indicatorId;

    /**
     * 指标编码（业务唯一标识，如IND-2025-001）
     */
    @Excel(name = "指标编码", readConverterExp = "业=务唯一标识")
    private String indicatorCode;

    public String getIndicatorDeviationType() {
        return indicatorDeviationType;
    }

    public void setIndicatorDeviationType(String indicatorDeviationType) {
        this.indicatorDeviationType = indicatorDeviationType;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    @Excel(name = "指标偏离类型", readConverterExp = "指标偏离度类别（NO_DEVIATION, DEVIATION_LT_2, DEVIATION_GT_2）")
    private String indicatorDeviationType;

    @Excel(name = "行社数量")
    private String count;

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

    public void setIndicatorId(Long indicatorId) {
        this.indicatorId = indicatorId;
    }

    public Long getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorCode(String indicatorCode) {
        this.indicatorCode = indicatorCode;
    }

    public String getIndicatorCode() {
        return indicatorCode;
    }




    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("dataDate", getDataDate())
                .append("indicatorId", getIndicatorId())
                .append("indicatorCode", getIndicatorCode())

                .toString();
    }
}
