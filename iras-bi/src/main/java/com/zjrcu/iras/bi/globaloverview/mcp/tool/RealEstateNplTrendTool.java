package com.zjrcu.iras.bi.globaloverview.mcp.tool;

import com.zjrcu.iras.bi.globaloverview.domain.mcp.McpTool;
import com.zjrcu.iras.bi.globaloverview.domain.qo.GlobalOverviewRequestQO;
import com.zjrcu.iras.bi.globaloverview.service.RealEstateFinanceMonitoring.IRealEstateLoanDetailService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
@Component
public class RealEstateNplTrendTool implements McpTool {
    private final IRealEstateLoanDetailService realEstateLoanDetailService;

    public RealEstateNplTrendTool(
            IRealEstateLoanDetailService realEstateLoanDetailService) {
        this.realEstateLoanDetailService = realEstateLoanDetailService;
    }

    @Override
    public String name() {
        return "query_real_estate_npl_trend";
    }

    @Override
    public String description() {
        return "查询指定时间范围内房地产不良贷款余额及不良贷款率趋势";
    }


    @Override
    public Map<String, Object> parameters() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "startDate", Map.of(
                                "type", "string",
                                "description", "开始日期，格式 yyyy-MM-dd"
                        ),
                        "endDate", Map.of(
                                "type", "string",
                                "description", "结束日期，格式 yyyy-MM-dd"
                        ),
                        "regionName", Map.of(
                                "type", "string",
                                "description", "地区名称（可选）"
                        )
                ),
                "required", List.of("startDate", "endDate")
        );
    }


    @Override
    public Object execute(Map<String, Object> args) {

        GlobalOverviewRequestQO qo = new GlobalOverviewRequestQO();
        qo.setStartDate((String) args.get("startDate"));
        qo.setEndDate((String) args.get("endDate"));
        qo.setRegionName((String) args.get("regionName"));

        return realEstateLoanDetailService
                .selectRealEstateNonPerformingLoan(qo);
    }
}
