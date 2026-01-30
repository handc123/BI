package com.zjrcu.iras.bi.indicatorquality.mapper;

import com.zjrcu.iras.bi.indicatorquality.domain.model.OrgIndicatorRank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrgIndicatorRankMapper {
    List<OrgIndicatorRank> selectOrgIndicatorRank(String rankType);

    List<OrgIndicatorRank> selectOrgIndicatorRankPage(@Param("rankType") String rankType, @Param("dataDate") String dataDate);
}
