package com.zjrcu.iras.bi.fieldquality.mapper;

import com.zjrcu.iras.bi.fieldquality.domain.model.RegionDailyRank;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;



/**
 * 地区管理部排名（地区通过率排名）Mapper接口
 *
 * @author ruoyi
 * @date 2025-07-30
 */
@Mapper
public interface RegionDailyRankMapper {

    List<RegionDailyRank> selectRegionDailyRank();
}
