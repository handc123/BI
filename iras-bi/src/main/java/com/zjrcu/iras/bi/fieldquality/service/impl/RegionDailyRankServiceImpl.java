package com.zjrcu.iras.bi.fieldquality.service.impl;

import com.zjrcu.iras.bi.fieldquality.domain.model.RegionDailyRank;
import com.zjrcu.iras.bi.fieldquality.mapper.RegionDailyRankMapper;
import com.zjrcu.iras.bi.fieldquality.service.IRegionDailyRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 地区管理部排名（地区通过率排名）Service业务层处理
 *
 * @author ruoyi
 * @date 2025-07-30
 */
@Service
public class RegionDailyRankServiceImpl implements IRegionDailyRankService {
    @Autowired
    private RegionDailyRankMapper regionDailyRankMapper;


    @Override
    public List<RegionDailyRank> selectRegionDailyRank() {
        return regionDailyRankMapper.selectRegionDailyRank();
    }
}
