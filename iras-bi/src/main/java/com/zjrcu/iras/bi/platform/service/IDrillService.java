package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.dto.DrillConfigDTO;
import com.zjrcu.iras.bi.platform.domain.dto.DrillQueryRequestDTO;

import java.util.Map;

/**
 * 穿透明细服务
 */
public interface IDrillService {

    /**
     * 获取指标穿透配置
     */
    DrillConfigDTO getDrillConfig(Long metricId);

    /**
     * 执行穿透明细查询
     */
    Map<String, Object> executeDrillQuery(DrillQueryRequestDTO request);
}

