package com.zjrcu.iras.bi.conversionquality.controller;

import com.zjrcu.iras.bi.conversionquality.domain.model.ConversionOrgRankInfo;
import com.zjrcu.iras.bi.conversionquality.service.IConversionOrgRankInfoService;
import com.zjrcu.iras.bi.conversionquality.service.IConversionQualityService;
import com.zjrcu.iras.bi.conversionquality.service.IConversionTableItemQualityService;
import com.zjrcu.iras.bi.conversionquality.service.IOrgConversionQualityCheckFailureDetailService;
import com.zjrcu.iras.bi.conversionquality.service.IOrgConversionTableService;
import com.zjrcu.iras.bi.conversionquality.service.IRegionConversionQualityRankService;
import com.zjrcu.iras.bi.conversionquality.service.IRegionOrgConversionQualityCheckFailureSummaryService;
import com.zjrcu.iras.common.core.controller.BaseController;
import com.zjrcu.iras.common.core.domain.AjaxResult;
import com.zjrcu.iras.common.core.page.TableDataInfo;
import com.zjrcu.iras.common.utils.poi.ExcelUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@Tag(name = "转换质量看板接口")
@RestController
@RequestMapping("/dataQuality/conversionQualityBoard")
public class ConversionQualityBoardController extends BaseController {

    @Resource
    private IConversionQualityService conversionQualityService;
    @Resource
    private IRegionConversionQualityRankService regionConversionQualityRankService;
    @Resource
    private IRegionOrgConversionQualityCheckFailureSummaryService regionOrgConversionQualityCheckFailureSummaryService;
    @Resource
    private IOrgConversionQualityCheckFailureDetailService orgConversionQualityCheckFailureDetailService;
    @Resource
    private IConversionOrgRankInfoService conversionOrgRankInfoService;
    @Resource
    private IOrgConversionTableService orgConversionTableService;
    @Resource
    private IConversionTableItemQualityService conversionTableItemQualityService;
    /**
     * 展示一键转1104通过整体情况
     */

    @Operation(description = "展示一键转1104通过整体情况")
    @GetMapping("/passRateStat")
    public AjaxResult selectPassRateStat(){
        return success(conversionQualityService.selectPassRateStat());
    }
    /**
     * 获取地区管理部排名
     */

    @Operation(description = "获取地区管理部排名")
    @GetMapping("/regionConversionQualityRank")
    public AjaxResult selectRegionConversionQualityRank(){
        return success(regionConversionQualityRankService.selectRegionConversionQualityRank());
    }
    /**
     * 根据地区获取行社未通过表格数和未通过数据项数
     */
    @Operation(summary = "根据地区获取行社未通过表格数和未通过数据项数")
    @GetMapping("/regionOrgConversionQualityCheckFailureSummary")
    public AjaxResult selectRegionOrgConversionQualityCheckFailureSummary(@RequestParam("regionName") String regionName){
        return success(regionOrgConversionQualityCheckFailureSummaryService.selectRegionOrgConversionQualityCheckFailureSummary(regionName));
    }
    /**
     * 获取行社未通过数据项详情
     */
    @Operation(summary = "获取行社未通过数据项详情")
    @GetMapping("/orgConversionQualityCheckFailureDetail")
    public AjaxResult selectOrgConversionQualityCheckFailureDetail(@RequestParam("orgName") String orgName){
        return success(orgConversionQualityCheckFailureDetailService.selectOrgConversionQualityCheckFailureDetail(orgName));
    }
    /**
     * 获取行社数据项通过项、表格通过项、提升通过项排名
     */
    @Operation(summary = "获取行社数据项通过项、表格通过项、提升通过项排名")
    @GetMapping("/orgConversionQualityRank")
    public AjaxResult selectOrgConversionQualityRank(@Parameter(description = "rankType:ITEM-行社数据项通过项、TABLE-行社表格通过项、IMPROVE-行社提升通过项") @RequestParam("rankType") String rankType){
        return success(conversionOrgRankInfoService.selectConversionOrgRankInfo(rankType));
    }
    /**
     * 获取行社数据项通过项、表格通过项、提升通过项排名分页列表
     */
    @Operation(summary = "获取行社数据项通过项、表格通过项、提升通过项排名分页列表")
    @GetMapping("/orgConversionQualityRank/page")
    public TableDataInfo selectOrgConversionQualityRankPage(@Parameter(description = "rankType:ITEM-行社数据项通过项、TABLE-行社表格通过项、IMPROVE-行社提升通过项") @RequestParam("rankType") String rankType,
                                                            @RequestParam(value = "dataDate", required = false) String dataDate){
        startPage();
        return getDataTable(conversionOrgRankInfoService.selectConversionOrgRankInfoPage(rankType, dataDate));
    }
    /**
     * 导出行社数据项通过项、表格通过项、提升通过项排名分页列表
     */
    @Operation(summary = "导出行社数据项通过项、表格通过项、提升通过项排名分页列表")
    @PostMapping("/orgConversionQualityRank/export")
    public void exportOrgConversionQualityRankPage(HttpServletResponse response, @Parameter(description = "rankType:ITEM-行社数据项通过项、TABLE-行社表格通过项、IMPROVE-行社提升通过项") @RequestParam("rankType") String rankType,
                                                   @RequestParam(value = "dataDate", required = false) String dataDate){
        List<ConversionOrgRankInfo> list = conversionOrgRankInfoService.selectConversionOrgRankInfoPage(rankType, dataDate);
        ExcelUtil<ConversionOrgRankInfo> util = new ExcelUtil<ConversionOrgRankInfo>(ConversionOrgRankInfo.class);
        util.exportExcel(response, list, "行社一表通指标质量排名数据");
    }
    /**
     * 获取行社一键转通过表格整体情况
     */
    @Operation(summary = "获取行社一键转通过表格整体情况")
    @GetMapping("/orgConversionTable")
    public AjaxResult selectOrgConversionTable(@RequestParam("orgName") String orgName){
        return success(orgConversionTableService.selectOrgConversionTable(orgName));
    }
    /**
     * 获取行社一键转1104各表格质量情况
     */
    @Operation(summary = "获取行社一键转1104各表格质量情况")
    @GetMapping("/orgConversionTableQuality")
    public AjaxResult selectOrgConversionTableQuality(@RequestParam("orgName") String orgName){
        return success(conversionTableItemQualityService.selectOrgConversionTableQuality(orgName));
    }

}
