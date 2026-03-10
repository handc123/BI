package com.zjrcu.iras.bi.platform.service.impl;

import com.zjrcu.iras.bi.metric.domain.MetricMetadata;
import com.zjrcu.iras.bi.metric.service.IMetricMetadataService;
import com.zjrcu.iras.bi.platform.domain.dto.*;
import com.zjrcu.iras.bi.platform.service.IDatasetService;
import com.zjrcu.iras.bi.platform.service.IDrillService;
import com.zjrcu.iras.bi.platform.service.IQueryExecutor;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.SecurityUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 穿透明细服务实现
 */
@Service
public class DrillServiceImpl implements IDrillService {

    @Autowired
    private IMetricMetadataService metricMetadataService;

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private IQueryExecutor queryExecutor;

    @Override
    public DrillConfigDTO getDrillConfig(Long metricId) {
        if (metricId == null) {
            throw new ServiceException("metricId不能为空");
        }

        MetricMetadata metric = metricMetadataService.selectMetricMetadataById(metricId);
        if (metric == null) {
            throw new ServiceException("指标不存在: " + metricId);
        }
        if (metric.getDatasetId() == null) {
            throw new ServiceException("指标未关联数据集");
        }

        DrillConfigDTO config = new DrillConfigDTO();
        config.setMetricId(metric.getId());
        config.setMetricCode(metric.getMetricCode());
        config.setMetricName(metric.getMetricName());
        config.setDatasetId(metric.getDatasetId());
        config.setTechnicalFormula(metric.getTechnicalFormula());
        config.setCalculationLogic(metric.getCalculationLogic());
        config.setFields(datasetService.getDatasetFields(metric.getDatasetId()));
        return config;
    }

    @Override
    public Map<String, Object> executeDrillQuery(DrillQueryRequestDTO request) {
        if (request == null || request.getMetricId() == null) {
            throw new ServiceException("metricId不能为空");
        }

        MetricMetadata metric = metricMetadataService.selectMetricMetadataById(request.getMetricId());
        if (metric == null) {
            throw new ServiceException("指标不存在: " + request.getMetricId());
        }

        Long datasetId = request.getDatasetId() != null ? request.getDatasetId() : metric.getDatasetId();
        if (datasetId == null) {
            throw new ServiceException("指标未关联数据集");
        }

        // 带入条件先下推到数据库侧，减少回传数据量
        // 若用户规则中出现同字段，按需求“用户规则优先”覆盖带入条件
        Set<String> overrideFields = extractRuleFields(request.getRuleGroups());
        List<DrillConditionDTO> inherited = request.getInheritedConditions();
        if (inherited != null && !inherited.isEmpty() && !overrideFields.isEmpty()) {
            inherited = inherited.stream()
                .filter(c -> c == null || StringUtils.isEmpty(c.getField()) || !overrideFields.contains(c.getField().toLowerCase()))
                .collect(Collectors.toList());
        }
        List<Filter> inheritedFilters = toExecutorFilters(inherited);

        QueryResult queryResult = queryExecutor.executeQuery(
            datasetId,
            inheritedFilters,
            SecurityUtils.getLoginUser() != null ? SecurityUtils.getLoginUser().getUser() : null
        );
        if (!queryResult.isSuccess()) {
            throw new ServiceException(StringUtils.isNotEmpty(queryResult.getMessage()) ? queryResult.getMessage() : "明细查询失败");
        }

        List<Map<String, Object>> allRows = queryResult.getData() != null ? queryResult.getData() : Collections.emptyList();
        List<Map<String, Object>> filteredRows = applyRuleGroups(allRows, request.getRuleGroups());

        int pageNum = request.getPageNum() != null && request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 20;
        int from = Math.max((pageNum - 1) * pageSize, 0);
        int to = Math.min(from + pageSize, filteredRows.size());
        List<Map<String, Object>> pageRows = from >= filteredRows.size() ? Collections.emptyList() : filteredRows.subList(from, to);

        Map<String, Object> result = new HashMap<>();
        result.put("metricId", request.getMetricId());
        result.put("datasetId", datasetId);
        result.put("total", filteredRows.size());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("rows", pageRows);
        result.put("fields", queryResult.getFields());
        return result;
    }

    private List<Filter> toExecutorFilters(List<DrillConditionDTO> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return Collections.emptyList();
        }
        List<Filter> filters = new ArrayList<>();
        for (DrillConditionDTO item : conditions) {
            if (item == null || StringUtils.isEmpty(item.getField())) {
                continue;
            }
            Filter f = new Filter();
            f.setField(item.getField());
            f.setOperator(item.getOperator());

            if (item.getValues() != null && !item.getValues().isEmpty()) {
                f.setValues(item.getValues());
            } else if ("between".equalsIgnoreCase(item.getOperator()) && item.getValue() instanceof String) {
                String[] parts = String.valueOf(item.getValue()).split(",", 2);
                if (parts.length == 2) {
                    f.setValues(Arrays.asList(parts[0].trim(), parts[1].trim()));
                } else {
                    f.setValue(item.getValue());
                }
            } else {
                f.setValue(item.getValue());
            }
            filters.add(f);
        }
        return filters;
    }

    /**
     * 规则组表达式按 left-associative 计算：
     * g1 (rel2 g2) (rel3 g3) ...
     */
    private List<Map<String, Object>> applyRuleGroups(List<Map<String, Object>> rows, List<DrillRuleGroupDTO> groups) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        if (groups == null || groups.isEmpty()) {
            return rows;
        }

        return rows.stream()
            .filter(row -> evaluateRuleGroups(row, groups))
            .collect(Collectors.toList());
    }

    private Set<String> extractRuleFields(List<DrillRuleGroupDTO> groups) {
        Set<String> fields = new HashSet<>();
        if (groups == null) {
            return fields;
        }
        for (DrillRuleGroupDTO group : groups) {
            if (group == null || group.getRules() == null) {
                continue;
            }
            for (DrillConditionDTO rule : group.getRules()) {
                if (rule != null && StringUtils.isNotEmpty(rule.getField())) {
                    fields.add(rule.getField().toLowerCase());
                }
            }
        }
        return fields;
    }

    private boolean evaluateRuleGroups(Map<String, Object> row, List<DrillRuleGroupDTO> groups) {
        boolean result = evaluateGroup(row, groups.get(0));
        for (int i = 1; i < groups.size(); i++) {
            DrillRuleGroupDTO group = groups.get(i);
            String rel = group != null ? group.getRelationWithPrev() : null;
            boolean current = evaluateGroup(row, group);
            if ("OR".equalsIgnoreCase(rel)) {
                result = result || current;
            } else {
                result = result && current;
            }
        }
        return result;
    }

    private boolean evaluateGroup(Map<String, Object> row, DrillRuleGroupDTO group) {
        if (group == null || group.getRules() == null || group.getRules().isEmpty()) {
            return true;
        }
        for (DrillConditionDTO rule : group.getRules()) {
            if (!evaluateRule(row, rule)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateRule(Map<String, Object> row, DrillConditionDTO rule) {
        if (rule == null || StringUtils.isEmpty(rule.getField())) {
            return true;
        }
        Object left = getFieldValue(row, rule.getField());
        String op = StringUtils.isEmpty(rule.getOperator()) ? "=" : rule.getOperator().trim().toUpperCase();
        Object right = rule.getValue();
        List<Object> rights = rule.getValues();

        switch (op) {
            case "=":
            case "EQ":
                return compare(left, right) == 0;
            case "!=":
            case "<>":
            case "NE":
                return compare(left, right) != 0;
            case ">":
            case "GT":
                return compare(left, right) > 0;
            case ">=":
            case "GTE":
                return compare(left, right) >= 0;
            case "<":
            case "LT":
                return compare(left, right) < 0;
            case "<=":
            case "LTE":
                return compare(left, right) <= 0;
            case "LIKE":
                return left != null && right != null && String.valueOf(left).contains(String.valueOf(right));
            case "IN":
                return rights != null && rights.stream().anyMatch(v -> compare(left, v) == 0);
            case "NOT IN":
                return rights == null || rights.stream().noneMatch(v -> compare(left, v) == 0);
            case "BETWEEN":
                if (rights != null && rights.size() == 2) {
                    return compare(left, rights.get(0)) >= 0 && compare(left, rights.get(1)) <= 0;
                }
                if (right instanceof String) {
                    String[] parts = String.valueOf(right).split(",", 2);
                    if (parts.length == 2) {
                        return compare(left, parts[0].trim()) >= 0 && compare(left, parts[1].trim()) <= 0;
                    }
                }
                return true;
            case "IS NULL":
                return left == null || String.valueOf(left).trim().isEmpty();
            case "IS NOT NULL":
                return left != null && !String.valueOf(left).trim().isEmpty();
            default:
                return true;
        }
    }

    private Object getFieldValue(Map<String, Object> row, String field) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        if (row.containsKey(field)) {
            return row.get(field);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (field.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private int compare(Object left, Object right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }

        BigDecimal lNum = asBigDecimal(left);
        BigDecimal rNum = asBigDecimal(right);
        if (lNum != null && rNum != null) {
            return lNum.compareTo(rNum);
        }

        return String.valueOf(left).compareTo(String.valueOf(right));
    }

    private BigDecimal asBigDecimal(Object value) {
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignore) {
            return null;
        }
    }
}
