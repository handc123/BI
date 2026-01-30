package com.zjrcu.iras.bi.fieldquality.mapper;

import com.zjrcu.iras.bi.fieldquality.domain.vo.RegionOrgCheckFailureDisVo;
import com.zjrcu.iras.bi.fieldquality.domain.vo.RegionOrgCheckFailureSummaryVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 地区行社未通过项（统计未通过规则项数和记录数）Mapper接口
 *
 * @author ruoyi
 * @date 2025-07-30
 */
@Mapper
public interface RegionOrgCheckFailureSummaryMapper {

    RegionOrgCheckFailureSummaryVo selectRegionOrgCheckFailureSummaryByRegionName(  String regionName);

    List<RegionOrgCheckFailureDisVo> selectRegionOrgCheckFailureDisByRegionName( String regionName);
}
