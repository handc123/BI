package com.zjrcu.iras.bi.fieldquality.service;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgDataQualityRankDaily;

import java.util.List;

/**
 * 行社数据质量每日排名Service接口
 *
 * @author ruoyi
 * @date 2025-07-31
 */
public interface IOrgDataQualityRankDailyService {

    List<OrgDataQualityRankDaily> selectRegionDailyRankByRankType(String rankType);

    List<OrgDataQualityRankDaily> selectPageByRankType(String rankType, String dataDate);


    List<OrgDataQualityRankDaily> selectPageByRankTypeDataDate(String rankType, String dataDate);
}
