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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(DrillServiceImpl.class);
    private static final Set<String> ALLOWED_OPERATORS = new HashSet<>(Arrays.asList(
        "EQ", "NE", "GT", "GTE", "LT", "LTE", "LIKE", "IN", "NOT_IN", "BETWEEN", "IS_NULL", "IS_NOT_NULL"
    ));

    private static final Set<String> PUSH_DOWN_OPERATORS = new HashSet<>(Arrays.asList(
        "EQ", "NE", "GT", "GTE", "LT", "LTE", "LIKE", "IN", "BETWEEN"
    ));

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
        String requestId = UUID.randomUUID().toString().replace("-", "");
        log.info("[DrillQuery] start requestId={}, metricId={}, datasetId={}, pageNum={}, pageSize={}",
                requestId,
                request != null ? request.getMetricId() : null,
                request != null ? request.getDatasetId() : null,
                request != null ? request.getPageNum() : null,
                request != null ? request.getPageSize() : null);
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

        Map<String, Object> result = executeOnDataset(
            datasetId,
            request.getMetricId(),
            null,
            request.getInheritedConditions(),
            request.getRuleGroups(),
            request.getSortRules(),
            request.getPageNum(),
            request.getPageSize()
        );
        log.info("[DrillQuery] finish requestId={}, metricId={}, datasetId={}, total={}",
                requestId, request.getMetricId(), datasetId, result.get("total"));
        return result;
    }

    @Override
    public Map<String, Object> executeDrillFieldQuery(DrillFieldQueryRequestDTO request) {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        log.info("[DrillFieldQuery] start requestId={}, componentId={}, datasetId={}, metricField={}, pageNum={}, pageSize={}",
                requestId,
                request != null ? request.getComponentId() : null,
                request != null ? request.getDatasetId() : null,
                request != null ? request.getMetricField() : null,
                request != null ? request.getPageNum() : null,
                request != null ? request.getPageSize() : null);
        if (request == null || request.getDatasetId() == null) {
            throw new ServiceException("datasetId不能为空");
        }
        if (StringUtils.isEmpty(request.getMetricField())) {
            throw new ServiceException("metricField不能为空");
        }

        Map<String, Object> result = executeOnDataset(
            request.getDatasetId(),
            null,
            request.getMetricField(),
            request.getInheritedConditions(),
            request.getRuleGroups(),
            request.getSortRules(),
            request.getPageNum(),
            request.getPageSize()
        );
        log.info("[DrillFieldQuery] finish requestId={}, componentId={}, datasetId={}, metricField={}, total={}",
                requestId, request.getComponentId(), request.getDatasetId(), request.getMetricField(), result.get("total"));
        return result;
    }

    private Map<String, Object> executeOnDataset(Long datasetId,
                                                 Long metricId,
                                                 String metricField,
                                                 List<DrillConditionDTO> inheritedConditions,
                                                 List<DrillRuleGroupDTO> ruleGroups,
                                                 List<DrillSortRuleDTO> sortRules,
                                                 Integer requestPageNum,
                                                 Integer requestPageSize) {
        Map<String, DatasetFieldVO> fieldMap = buildDatasetFieldMap(datasetId);
        if (StringUtils.isNotEmpty(metricField) && !fieldMap.containsKey(metricField.trim().toLowerCase())) {
            throw new ServiceException("metricField不在数据集字段范围内: " + metricField);
        }
        List<DrillConditionDTO> normalizedInherited = normalizeConditions(inheritedConditions, fieldMap, "已带入条件");
        List<DrillRuleGroupDTO> normalizedRuleGroups = normalizeRuleGroups(ruleGroups, fieldMap);

        // 用户规则优先: 如果规则中使用了同字段, 则剔除带入条件中的同字段条件
        Set<String> overrideFields = extractRuleFields(normalizedRuleGroups);
        if (!overrideFields.isEmpty()) {
            normalizedInherited = normalizedInherited.stream()
                .filter(c -> !overrideFields.contains(c.getField().toLowerCase()))
                .collect(Collectors.toList());
        }

        List<DrillConditionDTO> inheritedPushDown = normalizedInherited.stream()
            .filter(c -> PUSH_DOWN_OPERATORS.contains(c.getOperator()))
            .collect(Collectors.toList());
        List<DrillConditionDTO> inheritedMemory = normalizedInherited.stream()
            .filter(c -> !PUSH_DOWN_OPERATORS.contains(c.getOperator()))
            .collect(Collectors.toList());

        List<Filter> inheritedFilters = toExecutorFilters(inheritedPushDown);

        QueryResult queryResult = queryExecutor.executeQuery(
            datasetId,
            inheritedFilters,
            SecurityUtils.getLoginUser() != null ? SecurityUtils.getLoginUser().getUser() : null
        );
        if (!queryResult.isSuccess()) {
            throw new ServiceException(StringUtils.isNotEmpty(queryResult.getMessage()) ? queryResult.getMessage() : "明细查询失败");
        }

        List<Map<String, Object>> allRows = queryResult.getData() != null ? queryResult.getData() : Collections.emptyList();
        List<Map<String, Object>> filteredRows = applyConditions(allRows, inheritedMemory);
        filteredRows = applyRuleGroups(filteredRows, normalizedRuleGroups);
        filteredRows = applySortRules(filteredRows, normalizeSortRules(sortRules, fieldMap));

        int pageNum = requestPageNum != null && requestPageNum > 0 ? requestPageNum : 1;
        int pageSize = requestPageSize != null && requestPageSize > 0 ? requestPageSize : 20;
        int from = Math.max((pageNum - 1) * pageSize, 0);
        int to = Math.min(from + pageSize, filteredRows.size());
        List<Map<String, Object>> pageRows = from >= filteredRows.size() ? Collections.emptyList() : filteredRows.subList(from, to);

        Map<String, Object> result = new HashMap<>();
        result.put("metricId", metricId);
        result.put("metricField", metricField);
        result.put("datasetId", datasetId);
        result.put("total", filteredRows.size());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("rows", pageRows);
        result.put("fields", queryResult.getFields());
        return result;
    }

    private List<DrillSortRuleDTO> normalizeSortRules(List<DrillSortRuleDTO> sortRules,
                                                      Map<String, DatasetFieldVO> fieldMap) {
        if (sortRules == null || sortRules.isEmpty()) {
            return Collections.emptyList();
        }
        List<DrillSortRuleDTO> normalized = new ArrayList<>();
        for (DrillSortRuleDTO sortRule : sortRules) {
            if (sortRule == null) {
                continue;
            }
            String field = sortRule.getField();
            if (StringUtils.isEmpty(field)) {
                throw new ServiceException("排序字段不能为空");
            }
            DatasetFieldVO fieldVO = fieldMap.get(field.trim().toLowerCase());
            if (fieldVO == null) {
                throw new ServiceException("排序字段不在数据集字段范围内: " + field);
            }
            String order = StringUtils.isEmpty(sortRule.getOrder()) ? "ASC" : sortRule.getOrder().trim().toUpperCase();
            if (!"ASC".equals(order) && !"DESC".equals(order)) {
                throw new ServiceException("排序方向仅支持 ASC/DESC");
            }
            DrillSortRuleDTO normalizedRule = new DrillSortRuleDTO();
            normalizedRule.setField(fieldVO.getFieldName());
            normalizedRule.setOrder(order);
            normalized.add(normalizedRule);
        }
        return normalized;
    }

    private List<Map<String, Object>> applySortRules(List<Map<String, Object>> rows, List<DrillSortRuleDTO> sortRules) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        if (sortRules == null || sortRules.isEmpty()) {
            return rows;
        }
        List<Map<String, Object>> sorted = new ArrayList<>(rows);
        Comparator<Map<String, Object>> comparator = null;
        for (DrillSortRuleDTO sortRule : sortRules) {
            Comparator<Map<String, Object>> current = (left, right) -> {
                Object leftVal = getFieldValue(left, sortRule.getField());
                Object rightVal = getFieldValue(right, sortRule.getField());
                int cmp = compare(leftVal, rightVal);
                return "DESC".equals(sortRule.getOrder()) ? -cmp : cmp;
            };
            comparator = comparator == null ? current : comparator.thenComparing(current);
        }
        if (comparator != null) {
            sorted.sort(comparator);
        }
        return sorted;
    }

    private Map<String, DatasetFieldVO> buildDatasetFieldMap(Long datasetId) {
        List<Map<String, Object>> fields = datasetService.getDatasetFields(datasetId);
        if (fields == null || fields.isEmpty()) {
            throw new ServiceException("数据集字段信息为空，无法执行穿透查询");
        }

        Map<String, DatasetFieldVO> fieldMap = new HashMap<>();
        for (Map<String, Object> field : fields) {
            if (field == null || field.isEmpty()) {
                continue;
            }
            String fieldName = extractFieldName(field);
            if (StringUtils.isEmpty(fieldName)) {
                continue;
            }
            DatasetFieldVO vo = new DatasetFieldVO();
            vo.setFieldName(fieldName);
            vo.setFieldComment(stringValue(field.get("fieldComment")));
            vo.setFieldType(stringValue(field.get("fieldType")));
            fieldMap.put(fieldName.toLowerCase(), vo);
        }
        return fieldMap;
    }

    private String extractFieldName(Map<String, Object> field) {
        String[] keys = new String[] {"fieldName", "name", "field", "dbFieldName"};
        for (String key : keys) {
            String value = stringValue(field.get(key));
            if (StringUtils.isNotEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private List<DrillConditionDTO> normalizeConditions(List<DrillConditionDTO> conditions,
                                                        Map<String, DatasetFieldVO> fieldMap,
                                                        String sceneName) {
        if (conditions == null || conditions.isEmpty()) {
            return Collections.emptyList();
        }
        List<DrillConditionDTO> normalized = new ArrayList<>();
        for (DrillConditionDTO item : conditions) {
            DrillConditionDTO condition = normalizeSingleCondition(item, fieldMap, sceneName, false);
            if (condition != null) {
                normalized.add(condition);
            }
        }
        return normalized;
    }

    private List<DrillRuleGroupDTO> normalizeRuleGroups(List<DrillRuleGroupDTO> groups,
                                                        Map<String, DatasetFieldVO> fieldMap) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }

        List<DrillRuleGroupDTO> normalizedGroups = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            DrillRuleGroupDTO group = groups.get(i);
            if (group == null) {
                continue;
            }
            List<DrillConditionDTO> normalizedRules = new ArrayList<>();
            if (group.getRules() != null) {
                for (DrillConditionDTO rule : group.getRules()) {
                    DrillConditionDTO normalizedRule = normalizeSingleCondition(rule, fieldMap, "规则组条件", true);
                    if (normalizedRule != null) {
                        normalizedRules.add(normalizedRule);
                    }
                }
            }
            if (normalizedRules.isEmpty()) {
                continue;
            }
            DrillRuleGroupDTO newGroup = new DrillRuleGroupDTO();
            newGroup.setRules(normalizedRules);
            if (i == 0) {
                newGroup.setRelationWithPrev("AND");
            } else {
                String rel = StringUtils.isEmpty(group.getRelationWithPrev()) ? "AND" : group.getRelationWithPrev().trim().toUpperCase();
                newGroup.setRelationWithPrev("OR".equals(rel) ? "OR" : "AND");
            }
            normalizedGroups.add(newGroup);
        }
        return normalizedGroups;
    }

    private DrillConditionDTO normalizeSingleCondition(DrillConditionDTO source,
                                                       Map<String, DatasetFieldVO> fieldMap,
                                                       String sceneName,
                                                       boolean skipEmptyField) {
        if (source == null) {
            return null;
        }
        if (StringUtils.isEmpty(source.getField())) {
            if (skipEmptyField) {
                return null;
            }
            throw new ServiceException(sceneName + "字段不能为空");
        }

        String field = source.getField().trim();
        DatasetFieldVO fieldVO = fieldMap.get(field.toLowerCase());
        if (fieldVO == null) {
            throw new ServiceException(sceneName + "存在非法字段: " + source.getField());
        }

        String normalizedOp = normalizeOperator(source.getOperator());
        if (!ALLOWED_OPERATORS.contains(normalizedOp)) {
            throw new ServiceException(sceneName + "存在不支持的操作符: " + source.getOperator());
        }

        DrillConditionDTO target = new DrillConditionDTO();
        target.setField(fieldVO.getFieldName());
        target.setOperator(normalizedOp);

        if ("IS_NULL".equals(normalizedOp) || "IS_NOT_NULL".equals(normalizedOp)) {
            target.setValue(null);
            target.setValues(null);
            return target;
        }

        if ("IN".equals(normalizedOp) || "NOT_IN".equals(normalizedOp) || "BETWEEN".equals(normalizedOp)) {
            List<Object> normalizedValues = normalizeMultiValues(source);
            if ("BETWEEN".equals(normalizedOp) && normalizedValues.size() != 2) {
                throw new ServiceException(sceneName + "的BETWEEN操作符必须提供两个值");
            }
            if (("IN".equals(normalizedOp) || "NOT_IN".equals(normalizedOp)) && normalizedValues.isEmpty()) {
                throw new ServiceException(sceneName + "的IN/NOT IN操作符至少需要一个值");
            }
            target.setValues(normalizedValues);
            target.setValue(null);
            return target;
        }

        if (source.getValue() == null || StringUtils.isEmpty(String.valueOf(source.getValue()).trim())) {
            throw new ServiceException(sceneName + "字段[" + target.getField() + "]的条件值不能为空");
        }
        target.setValue(source.getValue());
        target.setValues(null);
        return target;
    }

    private String normalizeOperator(String operator) {
        if (StringUtils.isEmpty(operator)) {
            return "EQ";
        }
        String op = operator.trim().toUpperCase();
        switch (op) {
            case "=":
            case "==":
            case "EQ":
            case "等于":
                return "EQ";
            case "!=":
            case "<>":
            case "NE":
            case "不等于":
                return "NE";
            case ">":
            case "GT":
            case "大于":
                return "GT";
            case ">=":
            case "GTE":
            case "大于等于":
                return "GTE";
            case "<":
            case "LT":
            case "小于":
                return "LT";
            case "<=":
            case "LTE":
            case "小于等于":
                return "LTE";
            case "LIKE":
            case "包含":
                return "LIKE";
            case "IN":
                return "IN";
            case "NOT IN":
            case "NOT_IN":
                return "NOT_IN";
            case "BETWEEN":
            case "介于":
                return "BETWEEN";
            case "IS NULL":
            case "IS_NULL":
            case "为空":
                return "IS_NULL";
            case "IS NOT NULL":
            case "IS_NOT_NULL":
            case "非空":
                return "IS_NOT_NULL";
            default:
                return op;
        }
    }

    private List<Object> normalizeMultiValues(DrillConditionDTO source) {
        List<Object> result = new ArrayList<>();
        if (source.getValues() != null && !source.getValues().isEmpty()) {
            for (Object value : source.getValues()) {
                if (value != null && StringUtils.isNotEmpty(String.valueOf(value).trim())) {
                    result.add(value);
                }
            }
            return result;
        }

        if (source.getValue() == null) {
            return result;
        }

        Object value = source.getValue();
        if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            for (Object item : coll) {
                if (item != null && StringUtils.isNotEmpty(String.valueOf(item).trim())) {
                    result.add(item);
                }
            }
            return result;
        }

        String text = String.valueOf(value).trim();
        if (StringUtils.isEmpty(text)) {
            return result;
        }

        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1);
        }
        String[] parts = text.split(",");
        for (String part : parts) {
            String val = part != null ? part.trim() : "";
            if (StringUtils.isNotEmpty(val)) {
                result.add(val);
            }
        }
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
            f.setOperator(toExecutorOperator(item.getOperator()));

            if (item.getValues() != null && !item.getValues().isEmpty()) {
                f.setValues(item.getValues());
            } else {
                f.setValue(item.getValue());
            }
            filters.add(f);
        }
        return filters;
    }

    private String toExecutorOperator(String normalizedOp) {
        if (StringUtils.isEmpty(normalizedOp)) {
            return "eq";
        }
        switch (normalizedOp) {
            case "EQ":
                return "eq";
            case "NE":
                return "ne";
            case "GT":
                return "gt";
            case "GTE":
                return "gte";
            case "LT":
                return "lt";
            case "LTE":
                return "lte";
            case "LIKE":
                return "like";
            case "IN":
                return "in";
            case "BETWEEN":
                return "between";
            default:
                return normalizedOp.toLowerCase();
        }
    }

    private List<Map<String, Object>> applyConditions(List<Map<String, Object>> rows, List<DrillConditionDTO> conditions) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        if (conditions == null || conditions.isEmpty()) {
            return rows;
        }
        return rows.stream()
            .filter(row -> {
                for (DrillConditionDTO condition : conditions) {
                    if (!evaluateRule(row, condition)) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());
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
        String op = normalizeOperator(rule.getOperator());
        Object right = rule.getValue();
        List<Object> rights = rule.getValues();

        switch (op) {
            case "EQ":
                return compare(left, right) == 0;
            case "NE":
                return compare(left, right) != 0;
            case "GT":
                return compare(left, right) > 0;
            case "GTE":
                return compare(left, right) >= 0;
            case "LT":
                return compare(left, right) < 0;
            case "LTE":
                return compare(left, right) <= 0;
            case "LIKE":
                return left != null && right != null && String.valueOf(left).contains(String.valueOf(right));
            case "IN":
                return rights != null && rights.stream().anyMatch(v -> compare(left, v) == 0);
            case "NOT_IN":
                return rights == null || rights.stream().noneMatch(v -> compare(left, v) == 0);
            case "BETWEEN":
                if (rights != null && rights.size() == 2) {
                    return compare(left, rights.get(0)) >= 0 && compare(left, rights.get(1)) <= 0;
                }
                return true;
            case "IS_NULL":
                return left == null || String.valueOf(left).trim().isEmpty();
            case "IS_NOT_NULL":
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
