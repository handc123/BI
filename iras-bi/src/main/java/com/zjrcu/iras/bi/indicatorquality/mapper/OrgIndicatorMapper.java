package com.zjrcu.iras.bi.indicatorquality.mapper;

import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorBarChartVo;
import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorDevationCountVo;
import com.zjrcu.iras.bi.indicatorquality.domain.vo.OrgIndicatorDeviationTrendItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrgIndicatorMapper {
    List<OrgIndicatorBarChartVo> selectOrgIndicatorClassifyCount(String regionName);

    List<OrgIndicatorDevationCountVo> selectOrgIndicatorDeviationCountByOrgName(String orgName);

    List<OrgIndicatorDeviationTrendItem> selectNoDeviationTrend(String orgName);

    List<OrgIndicatorDeviationTrendItem> selectDeviationLt2Trend(String orgName);

    List<OrgIndicatorDeviationTrendItem> selectDeviationGt2Trend(String orgName);

}
