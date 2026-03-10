package com.zjrcu.iras.bi.platform.controller;

import com.zjrcu.iras.bi.platform.domain.dto.DrillConfigDTO;
import com.zjrcu.iras.bi.platform.domain.dto.DrillQueryRequestDTO;
import com.zjrcu.iras.bi.platform.service.IDrillService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 穿透明细控制器
 */
@RestController
@RequestMapping("/bi/drill")
public class DrillController extends BaseController {

    @Autowired
    private IDrillService drillService;

    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @GetMapping("/config/{metricId}")
    public AjaxResult getConfig(@PathVariable("metricId") Long metricId) {
        DrillConfigDTO config = drillService.getDrillConfig(metricId);
        return success(config);
    }

    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    @PostMapping("/query")
    public AjaxResult query(@RequestBody DrillQueryRequestDTO request) {
        return success(drillService.executeDrillQuery(request));
    }
}

