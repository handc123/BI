package com.zjrcu.iras.bi.indicatorquality.controller;


import com.zjrcu.iras.bi.indicatorquality.domain.model.OrgIndicatorDeviationRank;
import com.zjrcu.iras.bi.indicatorquality.domain.model.OrgIndicatorRank;
import com.zjrcu.iras.bi.indicatorquality.service.IIndicatorDeviationStatService;
import com.zjrcu.iras.bi.indicatorquality.service.IOrgIndicatorDeviationService;
import com.zjrcu.iras.bi.indicatorquality.service.IOrgIndicatorRankService;
import com.zjrcu.iras.bi.indicatorquality.service.IOrgIndicatorService;
import com.zjrcu.iras.bi.indicatorquality.service.IRegionIndicatorStatService;
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

@Tag(name = "指标质量看板接口")
@RestController
@RequestMapping("/dataQuality/indicatorQualityBoard")
public class IndicatorQualityBoardController extends BaseController {
    @Resource
    private IIndicatorDeviationStatService indicatorDeviationStatService;
    @Resource
    private IRegionIndicatorStatService regionIndicatorStatService;
    @Resource
    private IOrgIndicatorService orgIndicatorService;
    @Resource
    private IOrgIndicatorRankService orgIndicatorRankService;
    @Resource
    private IOrgIndicatorDeviationService orgIndicatorDeviationService;
    /**
     * 展示1104指标对比偏离度整体情况
     */

    @Operation(description = "展示1104指标对比偏离度整体情况")
    @GetMapping("/indicatorDeviationStat")
    public AjaxResult selectIndicatorDeviationStat(@Parameter(description = "NO_DEVIATION、DEVIATION_GT_2") @RequestParam("indicatorDeviationType") String indicatorDeviationType) {
        return success(indicatorDeviationStatService.selectIndicatorDeviationStat(indicatorDeviationType));
    }
    /**
     * 展示地区1104指标对比排名及整体情况
     */

    @Operation(description = "展示地区1104指标对比排名及整体情况")
    @GetMapping("/regionIndicatorDeviationRank")
    public AjaxResult selectRegionIndicatorDeviationRank() {
        return success(regionIndicatorStatService.selectRegionIndicatorDeviationRank());
    }
    /**
     * 展示所选地区下每个行社的指标分类个数情况
     */

    @Operation(description = "展示所选地区下每个行社的指标分类个数情况")
    @GetMapping("/orgIndicatorClassifyCount")
    public AjaxResult selectOrgIndicatorClassifyCount(@RequestParam("regionName") String regionName) {
        return success(orgIndicatorService.selectOrgIndicatorClassifyCount(regionName));
    }
    /**
     * 展示行社一表通指标质量排名，排名维度标识（如：无偏离指标数,偏离度通过指标数,提升指标数）,
     */

    @Operation(summary = "展示行社一表通指标质量排名，排名维度标识（如：无偏离指标数,偏离度通过指标数,提升指标数）")
    @GetMapping("/orgIndicatorRank")
    public AjaxResult selectOrgIndicatorRank(@Parameter(description = "NO_DEVIATION、DEVIATION_PASS、IMPROVED") @RequestParam("rankType") String rankType) {
        return success(orgIndicatorRankService.selectOrgIndicatorRank(rankType));
    }
    /**
     * 分页获取行社一表通指标质量排名，排名维度标识（如：无偏离指标数,偏离度通过指标数,提升指标数）,
     */


    @Operation(description = "分页获取行社一表通指标质量排名，排名维度标识（如：无偏离指标数,偏离度通过指标数,提升指标数）")
    @GetMapping("/orgIndicatorRank/page")
    public TableDataInfo selectOrgIndicatorRankPage(@Parameter(description = "NO_DEVIATION、DEVIATION_PASS、IMPROVED")@RequestParam("rankType") String rankType,
                                                    @RequestParam("dataDate") String dataDate) {
        startPage();
        return getDataTable(orgIndicatorRankService.selectOrgIndicatorRankPage(rankType, dataDate));
    }
    /**
     * 行社一表通指标质量排名导出
     */

    @Operation(description = "行社一表通指标质量排名导出")
    @PostMapping("/orgIndicatorRank/export")
    public void exportOrgIndicatorRank(HttpServletResponse response, @RequestParam(value = "dataDate", required = false) String dataDate, @RequestParam("rankType") String rankType){
        List<OrgIndicatorRank> list = orgIndicatorRankService.selectOrgIndicatorRankPage(rankType, dataDate);
        ExcelUtil<OrgIndicatorRank> util = new ExcelUtil<OrgIndicatorRank>(OrgIndicatorRank.class);
        util.exportExcel(response, list, "行社一表通指标质量排名数据");
    }
    /**
     * 获取行社指标偏离度排名
     */

    @Operation(description = "获取行社指标偏离度排名")
    @GetMapping("/orgIndicatorDeviationRank")
    public AjaxResult selectOrgIndicatorDeviationRank() {
        return success(orgIndicatorDeviationService.selectOrgIndicatorDeviationRank());
    }
    /**
     * 分页获取行社指标偏离度排名
     */

    @Operation(description = "分页获取行社指标偏离度排名")
    @GetMapping("/orgIndicatorDeviationRank/page")
    public TableDataInfo selectOrgIndicatorDeviationRankPage(@RequestParam(value = "dataDate", required = false) String dateDate) {
        startPage();
        return getDataTable(orgIndicatorDeviationService.selectOrgIndicatorDeviationRankPage(dateDate));
    }
    /**
     * 行社指标分离度排名列表导出
     */

    @Operation(description = "行社指标分离度排名列表导出")
    @PostMapping("/orgIndicatorDeviationRank/export")
    public void exportOrgIndicatorDeviationRank(HttpServletResponse  response, @RequestParam(value = "dataDate", required = false) String dataDate){
        List<OrgIndicatorDeviationRank> list = orgIndicatorDeviationService.selectOrgIndicatorDeviationRankPage(dataDate);
        ExcelUtil<OrgIndicatorDeviationRank> util = new ExcelUtil<OrgIndicatorDeviationRank>(OrgIndicatorDeviationRank.class);
        util.exportExcel(response, list, "行社指标分离度排名数据");
    }
    /**
     * 获取行社对比指标数量
     */

    @Operation(description = "获取行社对比指标数量")
    @GetMapping("/orgIndicatorDeviationCount")
    public AjaxResult selectOrgIndicatorDeviationCount(@RequestParam("orgName") String orgName) {
        return success(orgIndicatorService.selectOrgIndicatorDeviationCountByOrgName(orgName));
    }
    /**
     * 根据行社获取指标偏离度排名
     */

    @Operation(description = "根据行社获取指标偏离度排名")
    @GetMapping("/orgName/orgIndicatorDeviationRank")
    public AjaxResult selectOrgNameOrgIndicatorDeviationRank(@RequestParam("orgName") String orgName) {
        return success(orgIndicatorDeviationService.selectOrgIndicatorDeviationRankByOrgName(orgName));
    }
    /**
     * 根据行社分页获取指标偏离度排名
     */

    @Operation(description = "根据行社分页获取指标偏离度排名")
    @GetMapping("/orgName/orgIndicatorDeviationRank/page")
    public TableDataInfo selectOrgNameOrgIndicatorDeviationRankPage(@RequestParam("orgName") String orgName,
                                                                   @RequestParam(value = "dataDate", required = false) String dataDate) {
        startPage();
        return getDataTable(orgIndicatorDeviationService.selectOrgIndicatorDeviationRankPageByOrgName(orgName, dataDate));
    }
    /**
     * 根据行社分页获取指标偏离度排名导出
     */

    @Operation(description = "根据行社分页获取指标偏离度排名导出")
    @PostMapping("/orgName/orgIndicatorDeviationRank/export")
    public void exportOrgNameOrgIndicatorDeviationRank(HttpServletResponse response, @RequestParam("orgName") String orgName,
                                                       @RequestParam(value = "dataDate", required = false) String dataDate){
        List<OrgIndicatorDeviationRank> list = orgIndicatorDeviationService.selectOrgIndicatorDeviationRankPageByOrgName(orgName, dataDate);
        ExcelUtil<OrgIndicatorDeviationRank> util = new ExcelUtil<OrgIndicatorDeviationRank>(OrgIndicatorDeviationRank.class);
        util.exportExcel(response, list, "行社指标偏离度排名数据");
    }
    /**
     * 获取指标偏离度变动趋势
     */

    @Operation(description = "获取指标偏离度变动趋势")
    @GetMapping("/orgIndicatorDeviationTrend")
    public AjaxResult selectOrgIndicatorDeviationTrend(@RequestParam("orgName") String orgName) {
        return success(orgIndicatorService.selectOrgIndicatorDeviationTrend(orgName));
    }

}
