package com.zjrcu.iras.bi.conversionquality.service.Impl;

import com.zjrcu.iras.bi.conversionquality.domain.model.OrgConversionQualityCheckFailureDetail;
import com.zjrcu.iras.bi.conversionquality.mapper.OrgConversionQualityCheckFailureDetailMapper;
import com.zjrcu.iras.bi.conversionquality.service.IOrgConversionQualityCheckFailureDetailService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgConversionQualityCheckFailureDetailServiceImpl implements IOrgConversionQualityCheckFailureDetailService {

    @Resource
    private OrgConversionQualityCheckFailureDetailMapper orgConversionQualityCheckFailureDetailMapper;
    @Override
    public List<OrgConversionQualityCheckFailureDetail> selectOrgConversionQualityCheckFailureDetail(String orgName) {
        return orgConversionQualityCheckFailureDetailMapper.selectOrgConversionQualityCheckFailureDetail(orgName);

    }
}
