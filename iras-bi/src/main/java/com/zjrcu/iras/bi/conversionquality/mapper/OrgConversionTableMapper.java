package com.zjrcu.iras.bi.conversionquality.mapper;

import com.zjrcu.iras.bi.conversionquality.domain.model.OrgConversionTable;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrgConversionTableMapper {
    OrgConversionTable selectOrgConversionTable(String orgName);
}
