package com.zjrcu.iras.bi.fieldquality.service;

import com.zjrcu.iras.bi.fieldquality.domain.model.RegionDailyRank;

import java.util.List;



/**
 * 地区管理部排名（地区通过率排名）Service接口
 *
 * @author ruoyi
 * @date 2025-07-30
 */
public interface IRegionDailyRankService {

    List<RegionDailyRank> selectRegionDailyRank();

}
