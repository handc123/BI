package com.zjrcu.iras.bi.fieldquality.service.impl;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgDataQualityRankDaily;
import com.zjrcu.iras.bi.fieldquality.mapper.OrgDataQualityRankDailyMapper;
import com.zjrcu.iras.bi.fieldquality.service.IOrgDataQualityRankDailyService;
import com.zjrcu.iras.common.exception.ServiceException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 行社数据质量每日排名Service业务层处理
 *
 * @author ruoyi
 * @date 2025-07-31
 */
@Service
public class OrgDataQualityRankDailyServiceImpl implements IOrgDataQualityRankDailyService {
    @Resource
    private OrgDataQualityRankDailyMapper orgDataQualityRankDailyMapper;


    @Override
    public List<OrgDataQualityRankDaily> selectRegionDailyRankByRankType(String rankType) {
        if (rankType == null || rankType.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        List<OrgDataQualityRankDaily> regionDailyRank = orgDataQualityRankDailyMapper.selectRegionDailyRankByRankType(rankType);
        return regionDailyRank.stream().limit(10).collect(Collectors.toList());
    }

    @Override
    public List<OrgDataQualityRankDaily> selectPageByRankType(String rankType, String dataDate) {
        if (rankType == null || rankType.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return orgDataQualityRankDailyMapper.selectPageByRankTypeDataDate(rankType, dataDate);
    }

    @Override
    public List<OrgDataQualityRankDaily> selectPageByRankTypeDataDate(String rankType, String dataDate) {
        if (rankType == null || rankType.isEmpty()){
            throw new ServiceException("参数不能为空");
        }
        return orgDataQualityRankDailyMapper.selectPageByRankTypeDataDate(rankType, dataDate);
    }
}
