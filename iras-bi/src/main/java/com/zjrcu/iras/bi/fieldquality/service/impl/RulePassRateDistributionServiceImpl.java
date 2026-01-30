package com.zjrcu.iras.bi.fieldquality.service.impl;

import com.zjrcu.iras.bi.fieldquality.domain.model.RulePassRateDistribution;
import com.zjrcu.iras.bi.fieldquality.domain.vo.MonthlyPassRateSummary;
import com.zjrcu.iras.bi.fieldquality.mapper.RulePassRateDistributionMapper;
import com.zjrcu.iras.bi.fieldquality.service.IRulePassRateDistributionService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.DateUtils;
import com.zjrcu.iras.system.mapper.SysDeptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 规则项通过率分布统计（用于看板环形图及趋势图展示）Service业务层处理
 *
 * @author ruoyi
 * @date 2025-07-28
 */
@Service
public class RulePassRateDistributionServiceImpl implements IRulePassRateDistributionService {
    @Autowired
    private RulePassRateDistributionMapper rulePassRateDistributionMapper;
    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public List<RulePassRateDistribution> selectRulePassRateDistribution() {

        Date date = new Date();
        date = DateUtils.addDays(date, -1);
        String localDate = DateUtils.parseDateToStr("yyyy-MM-dd", date);
        List<RulePassRateDistribution> rulePassRateDistribution = rulePassRateDistributionMapper.selectRulePassRateDistributionLocalDateByOrgId(localDate);
        if (rulePassRateDistribution == null){
            throw new ServiceException("没有该机构数据");
        }
        return rulePassRateDistribution;
    }



    @Override
    public List<MonthlyPassRateSummary> selectRulePassRateTrend() {

        Date currentDate = new Date();
        LocalDate currentLocalDate = currentDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        List<LocalDate> targetDates = new ArrayList<>();

        targetDates.add(currentLocalDate);
        for (int i = 1; i < 6; i++) {
            YearMonth previousMonth = YearMonth.from(currentLocalDate).minusMonths(i);
            targetDates.add(previousMonth.atEndOfMonth());
        }
        Collections.reverse(targetDates);
        System.out.println(targetDates);
        List<MonthlyPassRateSummary> monthlyPassRateSummaryList = new ArrayList<>();
        for (LocalDate targetDate : targetDates) {
            String date = targetDate.toString();
            String month = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            List<RulePassRateDistribution> rulePassRateDistributionList = rulePassRateDistributionMapper.selectRulePassRateDistributionLocalDateByOrgId(date);
            MonthlyPassRateSummary monthlyPassRateSummary = new MonthlyPassRateSummary();
            monthlyPassRateSummary.setMonth(month);
            monthlyPassRateSummary.setRulePassRateDistribution(rulePassRateDistributionList);
            monthlyPassRateSummaryList.add(monthlyPassRateSummary);
        }
        return monthlyPassRateSummaryList;
    }
}
