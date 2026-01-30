package com.zjrcu.iras.bi.fieldquality.mapper;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgDataQualityRankDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 行社数据质量每日排名Mapper接口
 *
 * @author ruoyi
 * @date 2025-07-31
 */
@Mapper
public interface OrgDataQualityRankDailyMapper {

    List<OrgDataQualityRankDaily> selectRegionDailyRankByRankType(@Param("rankType") String rankType);

    List<OrgDataQualityRankDaily> selectPageByRankTypeDataDate(@Param("rankType") String rankType, @Param("dataDate") String dataDate);
}
