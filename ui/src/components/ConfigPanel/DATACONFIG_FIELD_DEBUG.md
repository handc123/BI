# DataConfig 字段显示问题调试指南

## 问题描述
选择数据源和数据集后，字段配置下的"指标"拖拽区域没有显示字段。

## 问题原因
1. `metricFields` 计算属性使用的字段类型判断逻辑不正确
2. 后端返回的 `fieldType` 是分类后的值（'metric' 或 'dimension'），而不是原始数据库类型
3. 前端代码只检查了原始类型名称，没有检查后端的分类结果

## 修复方案

### 1. 后端字段返回格式
`/bi/dataset/{id}/fields` 接口返回的字段格式：
```json
{
  "code": 200,
  "data": [
    {
      "fieldName": "amount",           // 字段名（用于SQL）
      "comment": "金额",                // 显示名称
      "fieldType": "metric",           // 后端分类: 'metric' 或 'dimension'
      "dataType": "DECIMAL",           // 原始数据库类型
      "dbFieldName": "amount"          // 数据库字段名
    }
  ]
}
```

### 2. 前端修复
修改 `metricFields` 计算属性，优先使用后端的 `fieldType` 分类：

```javascript
metricFields() {
  return this.availableFields.filter(field => {
    // 优先使用后端分类的fieldType
    if (field.fieldType === 'metric') {
      return true
    }
    // 兼容处理：检查dataType（原始数据库类型）
    const dataType = (field.dataType || field.type || '').toLowerCase()
    return ['int', 'bigint', 'decimal', 'double', 'float', 'numeric', 'smallint', 'tinyint', 'real'].some(t => dataType.includes(t))
  })
}
```

### 3. 调试步骤

#### 步骤1: 检查接口响应
打开浏览器开发者工具 -> Network 标签页，查找 `/bi/dataset/{id}/fields` 请求：
- 检查响应状态码是否为 200
- 检查 `response.data` 是否包含字段数据
- 检查每个字段是否包含 `fieldType` 和 `dataType` 属性

#### 步骤2: 检查控制台日志
打开浏览器控制台，查找以下日志：
```
[DataConfig] 开始加载数据集字段, datasetId: xxx
[DataConfig] 字段接口响应: {...}
[DataConfig] 加载字段成功, 共 X 个字段
[DataConfig] 字段详情: [...]
[DataConfig] 指标字段数量: X 个
[DataConfig] 指标字段列表: [...]
[DataConfig] 计算metricFields, availableFields: [...]
```

#### 步骤3: 验证字段分类
在控制台中手动检查：
```javascript
// 在浏览器控制台执行
console.log('所有字段:', this.$refs.dataConfig.availableFields)
console.log('指标字段:', this.$refs.dataConfig.metricFields)
console.log('维度字段:', this.$refs.dataConfig.dimensionFields)
```

### 4. 常见问题

#### 问题1: 字段列表为空
**原因**: 
- 数据集配置错误
- 数据源连接失败
- SQL查询失败

**解决方法**:
1. 检查数据集配置是否正确
2. 测试数据源连接
3. 查看后端日志中的错误信息

#### 问题2: 字段有数据但不显示在指标区域
**原因**:
- `fieldType` 分类错误
- 前端过滤逻辑问题

**解决方法**:
1. 检查后端 `classifyFieldType` 方法的分类逻辑
2. 检查前端 `metricFields` 计算属性
3. 添加调试日志查看字段详情

#### 问题3: 权限问题
**原因**: 用户没有 `bi:dataset:query` 权限

**解决方法**:
1. 检查用户角色权限配置
2. 在 `sys_menu` 表中添加相应权限
3. 重新登录刷新权限

### 5. 测试验证

#### 测试用例1: 数值型字段
```sql
-- 创建测试数据集
CREATE TABLE test_metrics (
  id INT PRIMARY KEY,
  amount DECIMAL(10,2),
  count BIGINT,
  rate DOUBLE
);
```
预期结果: amount, count, rate 应该显示在"指标"区域

#### 测试用例2: 混合字段
```sql
CREATE TABLE test_mixed (
  id INT PRIMARY KEY,
  name VARCHAR(100),
  amount DECIMAL(10,2),
  create_time DATETIME
);
```
预期结果: 
- amount 显示在"指标"区域
- name, create_time 显示在"维度"区域

## 修复文件清单
- `ui/src/components/ConfigPanel/DataConfig.vue` - 修复字段过滤逻辑
- `ui/src/api/bi/metadata.js` - 新增指标元数据API
- `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DatasetServiceImpl.java` - 字段分类逻辑（已存在）

## 相关文档
- [Enhanced Data Config Design](../../.kiro/specs/enhanced-data-config/design.md)
- [Field Display Name Fix](../../../FIELD_DISPLAY_NAME_FIX.md)
