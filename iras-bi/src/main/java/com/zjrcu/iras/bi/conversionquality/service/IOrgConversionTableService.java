package com.zjrcu.iras.bi.conversionquality.service;

import com.zjrcu.iras.bi.conversionquality.domain.model.OrgConversionTable;

public interface IOrgConversionTableService {
    OrgConversionTable selectOrgConversionTable(String orgName);
}
