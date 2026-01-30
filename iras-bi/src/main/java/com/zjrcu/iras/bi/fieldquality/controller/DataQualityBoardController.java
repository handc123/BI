package com.zjrcu.iras.bi.fieldquality.controller;

import com.zjrcu.iras.bi.fieldquality.domain.model.OrgDataQualityRankDaily;
import com.zjrcu.iras.bi.fieldquality.domain.model.OrgRuleImproveRateRank;
import com.zjrcu.iras.bi.fieldquality.service.IOrgDataQualityRankDailyService;
import com.zjrcu.iras.bi.fieldquality.service.IOrgRuleCheckSummaryService;
import com.zjrcu.iras.bi.fieldquality.service.IOrgRuleImproveRateRankService;
import com.zjrcu.iras.bi.fieldquality.service.IRegionDailyRankService;
import com.zjrcu.iras.bi.fieldquality.service.IRegionOrgCheckFailureSummaryService;
import com.zjrcu.iras.bi.fieldquality.service.IRuleInfoService;
import com.zjrcu.iras.bi.fieldquality.service.IRulePassRateDistributionService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.utils.poi.ExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "数据质量看板接口")
@RestController()
@RequestMapping("/dataQuality/dataQualityBoard")
public class DataQualityBoardController extends BaseController {

    @Resource
    private IRulePassRateDistributionService rulePassRateDistributionService;

    @Resource
    private IRegionDailyRankService regionDailyRankService;

    @Resource
    private IRegionOrgCheckFailureSummaryService regionOrgCheckFailureSummaryService;

    @Resource
    private IOrgDataQualityRankDailyService orgDataQualityRankDailyService;

    @Resource
    private IOrgRuleImproveRateRankService orgRuleImproveRateRankService;

    @Resource
    private IRuleInfoService ruleInfoService;

    @Resource
    private IOrgRuleCheckSummaryService orgRuleCheckSummaryService;

    /**
     * 根据机构展示规则项通过率分布统计（用于看板环形图及趋势图展示）
     */
//    @PreAuthorize("@ss.hasPermi('dataquality:dataqualityBoard:list')")

    @Operation(description = "展示规则项通过率分布统计")
    @GetMapping("/distribution")
    public AjaxResult selectRulePassRateDistribution() {
        return success(rulePassRateDistributionService.selectRulePassRateDistribution());
    }

    /**
     * 展示规则校验项通过变化趋势
     */

    @Operation(description = "展示规则校验项通过率变化趋势")
    @GetMapping("/trend")
    public AjaxResult selectRulePassRateTrendByOrgId() {
        return success(rulePassRateDistributionService.selectRulePassRateTrend());
    }

    /**
     * 获取地区通过率排名
     */

    @Operation(description = "获取地区通过率排名")
    @GetMapping("/region/rank")
    public AjaxResult selectRegionDailyRank() {
        return success(regionDailyRankService.selectRegionDailyRank());
    }

    /**
     * 根据地区获取未通过校验项和未通过记录数
     */

    @Operation(description = "根据地区获取未通过校验项和未通过记录数")
    @GetMapping("/region/checkFailureSummary")
    public AjaxResult selectRegionOrgCheckFailureSummaryByRegionName(@RequestParam("regionName") String regionName) {
        return success(regionOrgCheckFailureSummaryService.selectRegionOrgCheckFailureSummaryByRegionName(regionName));
    }

    /**
     * 展示所选地区下各个行社当前数据日期日未通过的校验项数量和未通过记录数
     */

    @Operation(description = "展示所选地区下各个行社当前数据日期日未通过的校验项数量和未通过记录数")
    @GetMapping("/region/orgCheckFailureDis")
    public AjaxResult selectRegionOrgCheckFailureDisByRegionName(@RequestParam("regionName") String regionName) {
        return success(regionOrgCheckFailureSummaryService.selectRegionOrgCheckFailureDisByRegionName(regionName));
    }

    /**
     * 根据排名类别获取全省排名
     */

    @Operation(description = "根据排名类别获取全省排名")
    @GetMapping("/orgName/rank")
    public AjaxResult selectRegionDailyRankByRankType(@RequestParam("rankType") String rankType) {
        return success(orgDataQualityRankDailyService.selectRegionDailyRankByRankType(rankType));
    }


    /**
     * 根据排名类别获取全省排名分页查询
     */

    @Operation(description = "根据排名类别获取全省排名分页查询")
    @GetMapping("/orgName/rank/page")
    public TableDataInfo selectRegionDailyRankListByRankType(@RequestParam("rankType") String rankType, @RequestParam(value = "dataDate",required = false) String dataDate) {
        startPage();
        return getDataTable(orgDataQualityRankDailyService.selectPageByRankType(rankType, dataDate));
    }
    /**
     * 导出全省排名
     */
    @Operation(description = "导出全省排名")
    @PostMapping("/orgName/rank/export")
    public void exportRegionDailyRankByRankType(HttpServletResponse response, @RequestParam("rankType") String rankType, @RequestParam(value = "dataDate",required = false) String dataDate) {
        List<OrgDataQualityRankDaily> list = orgDataQualityRankDailyService.selectPageByRankTypeDataDate(rankType, dataDate);
        ExcelUtil<OrgDataQualityRankDaily> util = new ExcelUtil<OrgDataQualityRankDaily>(OrgDataQualityRankDaily.class);
        util.exportExcel(response, list, "全省排名");
    }

    /**
     * 获取本月基础数据提升率排名
     */

    @Operation(description = "获取本月基础数据提升率排名")
    @GetMapping("/orgName/improveRateRank")
    public AjaxResult selectOrgRuleImproveRateRank() {
        return success(orgRuleImproveRateRankService.selectOrgRuleImproveRateRank());
    }

    /**
     * 获取本月基础数据提升率排名分页查询
     */

    @Operation(description = "获取本月基础数据提升率排名分页查询")
    @GetMapping("/orgName/improveRateRank/page")
    public TableDataInfo selectOrgRuleImproveRateRankList(@RequestParam(value = "dataDate", required = false) String dataDate) {
        startPage();
        return getDataTable(orgRuleImproveRateRankService.selectOrgRuleImproveRateRankList(dataDate));
    }
    /**
     * 导出本月基础数据提升率排名
     */
    @Operation(description = "导出本月基础数据提升率排名")
    @PostMapping("/orgName/improveRateRank/export")
    public void exportOrgRuleImproveRateRank(HttpServletResponse response, @RequestParam(value = "dataDate", required = false) String dataDate) {
        List<OrgRuleImproveRateRank> list = orgRuleImproveRateRankService.selectOrgRuleImproveRateRankListByDataDate(dataDate);
        ExcelUtil<OrgRuleImproveRateRank> util = new ExcelUtil<OrgRuleImproveRateRank>(OrgRuleImproveRateRank.class);
        util.exportExcel(response, list, "本月基础数据提升率排名");
    }

    /**
     * 根据规则编码获取规则详情
     */

    @Operation(description = "根据规则编码获取规则详情")
    @GetMapping("/ruleInfo")
    public AjaxResult selectRuleInfo(@RequestParam("ruleCode") String ruleCode) {
        return success(ruleInfoService.selectRuleInfo(ruleCode));
    }

    /**
     * 获取当前日期校验规则项、通过项、提升通过项、未通过项
     */

    @Operation(description = "根据行社获取当前日期校验规则项、通过项、提升通过项、未通过项")
    @GetMapping("/orgRuleCheckSummary")
    public AjaxResult selectOrgRuleCheckSummary(@RequestParam("orgName") String orgName) {
        return success(orgRuleCheckSummaryService.selectRuleCheckSummary(orgName));
    }

    /**
     * 获取近30天行社通过项和未通过项
     */

    @Operation(description = "获取近30天行社通过项和未通过项")
    @GetMapping("/orgRuleCheckTrend")
    public AjaxResult selectOrgRuleCheckTrend(@RequestParam("orgName") String orgName) {
        return success(orgRuleCheckSummaryService.selectOrgRuleCheckTrend(orgName));
    }

    /**
     * 获取数据质量提升排名
     */

    @Operation(description = "获取数据质量提升排名")
    @GetMapping("/dataQualityImproveRank")
    public AjaxResult selectDataQualityImproveRank(@RequestParam("orgName") String orgName, @RequestParam("compareType") String compareType) {
        return success(orgRuleImproveRateRankService.selectDataQualityImproveRank(orgName, compareType));
    }

    /**
     * 根据规则编号获取规则详情
     */

    @Operation(description = "根据规则编号获取规则详情")
    @GetMapping("/ruleInfoByRuleCode")
    public AjaxResult selectRulePassByRuleCode(@RequestParam("orgName") String orgName, @RequestParam("ruleCode") String ruleCode) {
        return success(ruleInfoService.selectRuleInfoPassByRuleCode(orgName, ruleCode));
    }

    /**
     * 根据五项规则校验大类，分别展示当前数据日期每类问题规则数量
     */

    @Operation(description = "根据五项规则校验大类，分别展示当前数据日期每类问题规则数量")
    @GetMapping("/ruleTypeCount")
    public AjaxResult selectRuleTypeCount(@RequestParam("orgName") String orgName) {
        return success(orgRuleImproveRateRankService.selectRuleTypeCount(orgName));
    }

    /**
     * 根据校验大类，展示当前数据日期规则通过率
     */

    @Operation(description = "根据五项规则校验大类，分别展示当前数据日期规则通过率")
    @GetMapping("/ruleTypePassRate")
    public AjaxResult selectRuleTypePassRate(@RequestParam("orgName") String orgName, @RequestParam("ruleTypeBig") String ruleTypeBig) {
        return success(orgRuleImproveRateRankService.selectRuleTypePassRate(orgName, ruleTypeBig));
    }

    /**
     * 根据校验大类，分页展示当前数据日期规则通过率
     */

    @Operation(description = "根据五项规则校验大类，分页展示当前数据日期规则通过率")
    @GetMapping("/ruleTypePassRate/page")
    public TableDataInfo selectRuleTypePassRateList(@RequestParam("orgName") String orgName, @RequestParam("ruleTypeBig") String ruleTypeBig, @RequestParam(value = "dataDate", required = false) String dataDate) {
        startPage();
        return getDataTable(orgRuleImproveRateRankService.selectRuleTypePassRateList(orgName, ruleTypeBig, dataDate));
    }
    /**
     * 根据校验大类，导出当前数据日期规则通过率
     */
    @Operation(description = "导出当前数据日期规则通过率")
    @PostMapping("/ruleTypePassRate/export")
    public void exportRuleTypePassRateList(HttpServletResponse response, @RequestParam("orgName") String orgName, @RequestParam("ruleTypeBig") String ruleTypeBig, @RequestParam(value = "dataDate", required = false) String dataDate) {
        List<OrgRuleImproveRateRank> list = orgRuleImproveRateRankService.selectRuleTypePassRateList(orgName, ruleTypeBig, dataDate);
        ExcelUtil<OrgRuleImproveRateRank> util = new ExcelUtil<OrgRuleImproveRateRank>(OrgRuleImproveRateRank.class);
        util.exportExcel(response, list, "行社一表通指标质量排名数据");
    }

}
