package com.zjrcu.iras.bi.conversionquality.service;

import com.zjrcu.iras.bi.conversionquality.domain.model.OrgConversionQualityCheckFailureDetail;

import java.util.List;

public interface IOrgConversionQualityCheckFailureDetailService {
    List<OrgConversionQualityCheckFailureDetail> selectOrgConversionQualityCheckFailureDetail(String orgName);
}
