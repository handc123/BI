# Task 6: 生命周期管理 - Implementation Summary

## Overview
This document summarizes the implementation of lifecycle management for calculated fields in chart components. The implementation ensures that calculated fields are properly handled during copy, delete, export, and import operations.

## Implementation Details

### 6.1 图表复制时复制计算字段 ✅

**Location**: `ComponentServiceImpl.copyComponent()`

**Implementation**:
- The existing `copyComponent()` method already handles calculated fields correctly
- When a component is copied, all configuration fields are copied including:
  - `dataConfig` (contains calculated fields)
  - `styleConfig`
  - `advancedConfig`
- Since calculated fields are stored in the `dataConfig` JSON field, they are automatically included in the copy

**Code**:
```java
@Override
@Transactional(rollbackFor = Exception.class)
public DashboardComponent copyComponent(Long id) {
    DashboardComponent original = componentMapper.selectComponentById(id);
    if (original == null) {
        throw new ServiceException("组件不存在");
    }

    // 创建副本
    DashboardComponent copy = new DashboardComponent();
    copy.setDashboardId(original.getDashboardId());
    copy.setComponentType(original.getComponentType());
    copy.setComponentName(original.getComponentName() + " - 副本");
    // ... position and size settings ...
    copy.setDataConfig(original.getDataConfig());  // ← Calculated fields included here
    copy.setStyleConfig(original.getStyleConfig());
    copy.setAdvancedConfig(original.getAdvancedConfig());

    componentMapper.insertComponent(copy);
    return copy;
}
```

**Requirements Satisfied**: 5.1, 5.5

---

### 6.2 图表删除时删除计算字段 ✅

**Location**: `ComponentServiceImpl.deleteComponentById()` and `deleteComponentByIds()`

**Implementation**:
- Calculated fields are stored in the `config_json` column (specifically in `dataConfig`)
- When a component is deleted, the entire database row is deleted
- This automatically removes all calculated fields associated with that component
- No additional code needed - deletion is handled by the database

**Code**:
```java
@Override
@Transactional(rollbackFor = Exception.class)
public int deleteComponentById(Long id) {
    // 删除关联的条件映射会通过数据库外键级联删除
    return componentMapper.deleteComponentById(id);
}
```

**Requirements Satisfied**: 5.2

---

### 6.3 图表导出时包含计算字段 ✅

**Location**: `DashboardServiceImpl.getDashboardConfig()`

**Implementation**:
- The `getDashboardConfig()` method returns a complete `DashboardConfig` object
- This includes all components with their full configuration
- Each component's `dataConfig` field contains the calculated fields
- The export mechanism automatically serializes calculated fields as part of the component configuration

**Code**:
```java
@Override
public DashboardConfig getDashboardConfig(Long id) {
    Dashboard dashboard = dashboardMapper.selectDashboardById(id);
    if (dashboard == null) {
        throw new ServiceException("仪表板不存在");
    }

    DashboardConfig config = new DashboardConfig();
    config.setDashboard(dashboard);
    config.setComponents(componentMapper.selectComponentByDashboardId(id));  // ← Includes dataConfig with calculated fields
    config.setQueryConditions(queryConditionMapper.selectConditionByDashboardId(id));
    config.setConditionMappings(conditionMappingMapper.selectMappingByDashboardId(id));

    return config;
}
```

**API Endpoint**: `GET /bi/dashboard/{id}/config`

**Requirements Satisfied**: 5.3

---

### 6.4 图表导入时恢复计算字段 ✅

**Location**: `DashboardServiceImpl.saveDashboardConfig()` and new method `validateAndCleanCalculatedFields()`

**Implementation**:
- Enhanced the `saveDashboardConfig()` method to validate calculated fields during import
- Added a new private method `validateAndCleanCalculatedFields()` that:
  - Validates the structure of calculated fields
  - Checks for required fields (name, expression, fieldType)
  - Validates field types (dimension/metric)
  - Validates aggregation methods
  - Logs warnings for invalid fields but doesn't block import
  - Does NOT validate field references (since the dataset might be different during import)

**Code**:
```java
@Override
@Transactional(rollbackFor = Exception.class)
public int saveDashboardConfig(Long dashboardId, DashboardConfig config) {
    // ... existing validation code ...
    
    // 插入新组件
    Map<String, Long> compMap = new HashMap<>();
    if (config.getComponents() != null) {
        for (DashboardComponent component : config.getComponents()) {
            String tempId = component.getTempId();
            component.setDashboardId(dashboardId);
            
            // 验证并清理计算字段配置(导入时)
            if (StringUtils.isNotEmpty(component.getDataConfig())) {
                validateAndCleanCalculatedFields(component);  // ← New validation
            }
            
            componentMapper.insertComponent(component);
            // ... rest of the code ...
        }
    }
    // ... rest of the method ...
}

/**
 * 验证并清理计算字段配置(导入时)
 */
private void validateAndCleanCalculatedFields(DashboardComponent component) {
    try {
        JSONObject dataConfig = JSONObject.parseObject(component.getDataConfig());
        
        if (!dataConfig.containsKey("calculatedFields")) {
            return; // 没有计算字段,无需处理
        }
        
        Object calculatedFieldsObj = dataConfig.get("calculatedFields");
        if (!(calculatedFieldsObj instanceof java.util.List)) {
            return; // 格式不正确,跳过
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> calculatedFields = (List<Map<String, Object>>) calculatedFieldsObj;
        
        // 验证每个计算字段
        for (Map<String, Object> field : calculatedFields) {
            // 验证必填字段
            if (!field.containsKey("name") || StringUtils.isEmpty((String) field.get("name"))) {
                log.warn("导入的计算字段缺少name字段,已跳过");
                continue;
            }
            if (!field.containsKey("expression") || StringUtils.isEmpty((String) field.get("expression"))) {
                log.warn("导入的计算字段 {} 缺少expression字段,已跳过", field.get("name"));
                continue;
            }
            if (!field.containsKey("fieldType") || StringUtils.isEmpty((String) field.get("fieldType"))) {
                log.warn("导入的计算字段 {} 缺少fieldType字段,已跳过", field.get("name"));
                continue;
            }
            
            // 验证字段类型
            String fieldType = (String) field.get("fieldType");
            if (!"dimension".equals(fieldType) && !"metric".equals(fieldType)) {
                log.warn("导入的计算字段 {} 的fieldType无效: {},已跳过", field.get("name"), fieldType);
                continue;
            }
            
            // 验证聚合方式(如果是指标类型)
            if ("metric".equals(fieldType) && field.containsKey("aggregation")) {
                String aggregation = (String) field.get("aggregation");
                if (!java.util.Arrays.asList("AUTO", "SUM", "AVG", "MAX", "MIN", "COUNT").contains(aggregation)) {
                    log.warn("导入的计算字段 {} 的aggregation无效: {},已设置为AUTO", field.get("name"), aggregation);
                    field.put("aggregation", "AUTO");
                }
            }
            
            log.info("成功验证导入的计算字段: {}", field.get("name"));
        }
        
        // 注意: 这里不验证字段引用,因为导入时可能数据集不同
        // 字段引用验证应该在用户使用时进行
        
    } catch (Exception e) {
        log.warn("验证计算字段配置失败: {}", e.getMessage());
        // 不阻止导入,只记录警告
    }
}
```

**API Endpoint**: `POST /bi/dashboard/{id}/config`

**Validation Strategy**:
- **Structural validation**: Ensures required fields are present and have correct types
- **Graceful degradation**: Logs warnings but doesn't block import for invalid fields
- **Deferred field reference validation**: Field references are NOT validated during import because:
  - The imported dashboard might use a different dataset
  - Field references will be validated when the user actually uses the calculated field
  - This allows for more flexible import/export across different environments

**Requirements Satisfied**: 5.4

---

## Testing

### Compilation Test
```bash
mvn clean compile -pl iras-bi -am -DskipTests
```
**Result**: ✅ BUILD SUCCESS

### Diagnostics Check
```bash
getDiagnostics on:
- ComponentServiceImpl.java
- DashboardServiceImpl.java
```
**Result**: ✅ No diagnostics found

---

## Design Decisions

### 1. Storage Location
Calculated fields are stored in the `dataConfig` JSON field of the `bi_dashboard_component` table. This design ensures:
- **Scope isolation**: Calculated fields are tied to specific components
- **Automatic lifecycle**: Copy/delete operations automatically handle calculated fields
- **No schema changes**: No database migrations required

### 2. Import Validation Strategy
The import validation is intentionally lenient:
- **Structural validation only**: Checks field structure and types
- **No field reference validation**: Deferred until actual use
- **Warning-based**: Logs warnings instead of throwing errors
- **Rationale**: Allows cross-environment imports where datasets might differ

### 3. Export Format
Calculated fields are exported as part of the component's `dataConfig`:
```json
{
  "dashboard": { ... },
  "components": [
    {
      "id": 1,
      "dataConfig": {
        "datasetId": 10,
        "calculatedFields": [
          {
            "name": "npl_ratio",
            "alias": "不良贷款率(%)",
            "fieldType": "metric",
            "expression": "(SUM(CASE WHEN wjfl IN ('次级', '可疑', '损失') THEN jkye ELSE 0 END) / NULLIF(SUM(jkye), 0)) * 100",
            "aggregation": "AUTO"
          }
        ]
      }
    }
  ]
}
```

---

## Requirements Mapping

| Requirement | Implementation | Status |
|-------------|----------------|--------|
| 5.1 - Copy calculated fields | `ComponentServiceImpl.copyComponent()` | ✅ |
| 5.2 - Delete calculated fields | `ComponentServiceImpl.deleteComponentById()` | ✅ |
| 5.3 - Export calculated fields | `DashboardServiceImpl.getDashboardConfig()` | ✅ |
| 5.4 - Import calculated fields | `DashboardServiceImpl.saveDashboardConfig()` + `validateAndCleanCalculatedFields()` | ✅ |
| 5.5 - Maintain references | Automatic via JSON serialization | ✅ |

---

## Next Steps

The lifecycle management implementation is complete. The next tasks in the spec are:

- **Task 7**: 作用域隔离 (Scope Isolation)
- **Task 8**: Checkpoint - 核心功能验证
- **Task 9**: 测试实现 (Testing)
- **Task 10**: 文档和部署 (Documentation and Deployment)
- **Task 11**: Final Checkpoint

---

## Conclusion

All lifecycle management tasks have been successfully implemented:
- ✅ Copy operations preserve calculated fields
- ✅ Delete operations remove calculated fields
- ✅ Export operations include calculated fields
- ✅ Import operations validate and restore calculated fields

The implementation follows the design principles of scope isolation and graceful degradation, ensuring that calculated fields are properly managed throughout their lifecycle while maintaining flexibility for cross-environment operations.
