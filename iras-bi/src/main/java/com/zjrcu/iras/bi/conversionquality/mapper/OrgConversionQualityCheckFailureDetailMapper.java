package com.zjrcu.iras.bi.conversionquality.mapper;

import com.zjrcu.iras.bi.conversionquality.domain.model.OrgConversionQualityCheckFailureDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrgConversionQualityCheckFailureDetailMapper {
    List<OrgConversionQualityCheckFailureDetail> selectOrgConversionQualityCheckFailureDetail(String orgName);
}
