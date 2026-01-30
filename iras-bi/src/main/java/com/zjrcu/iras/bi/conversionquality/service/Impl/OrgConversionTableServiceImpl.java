package com.zjrcu.iras.bi.conversionquality.service.Impl;

import com.zjrcu.iras.bi.conversionquality.domain.model.OrgConversionTable;
import com.zjrcu.iras.bi.conversionquality.mapper.OrgConversionTableMapper;
import com.zjrcu.iras.bi.conversionquality.service.IOrgConversionTableService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class OrgConversionTableServiceImpl implements IOrgConversionTableService {

    @Resource
    private OrgConversionTableMapper orgConversionTableMapper;
    @Override
    public OrgConversionTable selectOrgConversionTable(String orgName) {
        return orgConversionTableMapper.selectOrgConversionTable(orgName);
    }
}
