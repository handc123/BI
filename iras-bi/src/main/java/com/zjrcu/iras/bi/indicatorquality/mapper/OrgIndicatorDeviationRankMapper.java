package com.zjrcu.iras.bi.indicatorquality.mapper;

import com.zjrcu.iras.bi.indicatorquality.domain.model.OrgIndicatorDeviationRank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrgIndicatorDeviationRankMapper {
    List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRank();

    List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRankPage(@Param("dataDate") String dateDate);

    List<OrgIndicatorDeviationRank> selectOrgIndicatorDeviationRankByOrgName(@Param("orgName") String orgName, @Param("dataDate") String dataDate);



}
