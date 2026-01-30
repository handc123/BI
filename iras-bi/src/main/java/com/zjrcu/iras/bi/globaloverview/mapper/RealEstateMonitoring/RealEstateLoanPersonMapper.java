package com.zjrcu.iras.bi.globaloverview.mapper.RealEstateMonitoring;

import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.domain.model.RealEstateMonitoring.RealEstateLoanPerson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RealEstateLoanPersonMapper {
    List<RealEstateLoanPerson> selectRealEstateLoanPersonRatio(@Param("globalOverviewRequestQO") GlobalOverviewRequestQO globalOverviewRequestQO);
}
