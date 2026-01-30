package com.zjrcu.iras.bi.globaloverview.mapper.RealEstateMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.vo.RealEstateMonitoring.RealEstateLoanVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RealEstateLoanMapper {
    List<RealEstateLoanVo> selectRealEstateLoanBalance(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);


}
