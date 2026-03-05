# Field Display Name Fix - 字段显示名称修复

## 问题描述 (Problem Description)

在数据配置面板中选择数据源和数据集后，字段名称显示为英文字段名（fieldName）而不是数据库中的中文注释（comment）。

When selecting datasource and dataset in the data configuration panel, field names were displaying in English (fieldName) instead of Chinese comments from the database.

## 根本原因 (Root Cause)

前端的 `DataConfig.vue` 组件使用了错误的 API 端点来加载字段信息：
- **之前**: 使用 `getDatasetData` API，该接口返回数据行，字段元数据中不包含数据库注释
- **现在**: 使用 `getDatasetFields` API，该接口专门返回字段元数据，包含中文注释

The frontend `DataConfig.vue` component was using the wrong API endpoint to load field information:
- **Before**: Used `getDatasetData` API which returns data rows without database column comments
- **Now**: Uses `getDatasetFields` API which specifically returns field metadata with comments

## 修复内容 (Changes Made)

### 1. 前端修复 (Frontend Fix)

**文件**: `ui/src/components/ConfigPanel/DataConfig.vue`

修改 `loadDatasetFields` 方法：

```javascript
// 之前 (Before)
async loadDatasetFields(datasetId) {
  const response = await getDatasetData(datasetId, { pageSize: 1 })
  if (response.data && response.data.fields) {
    this.availableFields = response.data.fields
  }
}

// 之后 (After)
async loadDatasetFields(datasetId) {
  const response = await getDatasetFields(datasetId)
  if (response.code === 200 && response.data) {
    this.availableFields = response.data.map(field => ({
      name: field.name || field.fieldName,
      fieldName: field.fieldName || field.name,
      comment: field.comment || field.fieldComment || '',
      fieldType: field.fieldType || field.type,
      type: field.type || field.fieldType
    }))
  }
}
```

### 2. 后端增强 (Backend Enhancement)

**文件**: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DatasetServiceImpl.java`

增强 `getDatasetFields` 方法，从数据集的 `fieldConfig` 中读取字段别名（中文注释）：

```java
// 关键改进：读取数据集的 fieldConfig 配置
Map<String, Object> fieldConfigMap = dataset.getFieldConfigMap();
Map<String, String> fieldAliasMap = new java.util.HashMap<>();

if (fieldConfigMap != null && fieldConfigMap.containsKey("fields")) {
    List<Map<String, Object>> configFields = (List<Map<String, Object>>) fieldConfigMap.get("fields");
    if (configFields != null) {
        for (Map<String, Object> configField : configFields) {
            String name = (String) configField.get("name");
            String alias = (String) configField.get("alias");
            if (name != null && alias != null) {
                fieldAliasMap.put(name, alias);
            }
        }
    }
}

// 使用 fieldConfig 中的 alias 作为 comment
String comment = fieldAliasMap.getOrDefault(fieldName, 
    field.getAlias() != null && !field.getAlias().isEmpty() ? field.getAlias() : fieldName);
fieldMap.put("comment", comment);
```

## 数据流程 (Data Flow)

1. **数据集创建时** (Dataset Creation):
   - 从数据库表结构获取字段信息，包括 `columnComment`
   - 将 `columnComment` 存储为 `fieldConfig.fields[].alias`
   - 存储在数据集的 `field_config` JSON 字段中

2. **字段加载时** (Field Loading):
   - 前端调用 `/bi/dataset/{id}/fields` API
   - 后端从数据集的 `fieldConfig` 读取字段别名
   - 返回包含 `comment` 字段的字段元数据
   - 前端显示 `comment` 作为字段名称

## 显示优先级 (Display Priority)

字段显示名称的优先级顺序：
1. `fieldConfig` 中存储的 `alias`（数据集创建时保存的中文注释）
2. 查询结果中的 `alias`（如果有）
3. 原始字段名 `fieldName`（英文）

Display name priority:
1. `alias` from `fieldConfig` (Chinese comment saved during dataset creation)
2. `alias` from query result (if available)
3. Original `fieldName` (English)

## 测试验证 (Testing)

### 测试步骤 (Test Steps):

1. 打开仪表板设计器 `/dashboard/designer`
2. 添加一个图表组件
3. 在配置面板中选择"数据源配置"选项卡
4. 选择一个数据源
5. 选择一个数据集
6. 验证字段列表显示中文注释而不是英文字段名

### 预期结果 (Expected Result):

- 维度字段列表显示中文名称（如"客户名称"而不是"customer_name"）
- 指标字段列表显示中文名称（如"交易金额"而不是"transaction_amount"）
- 拖拽字段到配置区域后，显示的也是中文名称

## 影响范围 (Impact Scope)

### 受益功能 (Benefited Features):

1. ✅ 数据源配置面板 - 字段选择
2. ✅ 维度字段拖拽配置
3. ✅ 指标字段拖拽配置
4. ✅ 过滤条件字段选择
5. ✅ 指标配置对话框 - 字段选择

### 不影响 (Not Affected):

- 数据查询逻辑（仍使用英文字段名）
- SQL 生成（仍使用英文字段名）
- 数据集管理页面

## 相关文件 (Related Files)

- `ui/src/components/ConfigPanel/DataConfig.vue` - 前端数据配置组件
- `ui/src/api/bi/dataset.js` - 数据集 API 服务
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DatasetServiceImpl.java` - 数据集服务实现
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/DatasetController.java` - 数据集控制器

## 注意事项 (Notes)

1. 此修复依赖于数据集创建时正确保存了字段的 `alias` 配置
2. 如果数据集是通过 SQL 创建的（而不是基于表），可能没有中文注释，会显示字段名
3. 前端模板已经使用了正确的显示优先级：`{{ field.comment || field.fieldName || field.name }}`

## 修复日期 (Fix Date)

2026-03-05
