# 图表穿透功能 - 正确架构设计

## 一、核心概念澄清

### 穿透是**指标属性**，不是组件属性！

| 概念 | 说明 | 示例 |
|------|------|------|
| **指标** | 业务度量单位 | 不良贷款率、贷款余额、逾期金额 |
| **穿透规则** | 指标下钻到明细数据的路径 | 不良贷款率 → 贷款明细表 |
| **组件** | 可视化展示载体 | 柱状图、饼图、折线图 |
| **图表** | 指标的可视化实例 | 某个具体的柱状图组件 |

### 关键理解

```
指标：不良贷款率
├── 定义：不良贷款 / 总贷款
├── 数据源：贷款表
├── 穿透规则：点击后查看贷款明细 ✅ 这是指标的属性！
└── 可视化：
    ├── 柱状图A  ← 引用指标，自动获得穿透能力
    ├── 饼图B    ← 引用指标，自动获得穿透能力
    └── 折线图C  ← 引用指标，自动获得穿透能力
```

---

## 二、正确的数据模型

### 2.1 扩展指标元数据表

在 `bi_metric_metadata` 表中添加穿透配置字段：

```sql
ALTER TABLE bi_metric_metadata
ADD COLUMN `drill_through_config` TEXT COMMENT '穿透配置JSON' AFTER `technical_formula`;

-- 添加索引
CREATE INDEX idx_drill_through ON bi_metric_metadata(drill_through_config(100));
```

### 2.2 穿透配置JSON结构

```json
{
  "enabled": true,
  "datasourceId": 1,
  "targetTable": "npl_loan_detail",
  "fieldMappings": [
    {
      "sourceField": "dimensionValue",
      "sourceType": "click",
      "targetField": "region_name",
      "operator": "eq"
    }
  ],
  "additionalFilters": [
    {
      "field": "loan_status",
      "operator": "eq",
      "value": "1"
    }
  ],
  "displayFields": [
    {"field": "loan_id", "label": "贷款编号", "width": 120},
    {"field": "customer_name", "label": "客户名称", "width": 100}
  ],
  "pageSize": 50,
  "maxRows": 10000
}
```

### 2.3 组件与指标的关系（保持不变）

```javascript
// 组件配置
{
  "id": 123,
  "name": "地区-不良贷款率-柱状图",
  "dataConfig": {
    "metricIds": [1, 2, 3],  // ← 引用指标ID
    "dimensions": [...],
    "metrics": [...]
  }
}
```

---

## 三、穿透流程

### 3.1 用户交互流程

```
用户双击图表数据点
    ↓
ChartWidget 捕获双击事件
    ↓
提取点击上下文（地区="北京", 数值=5.2%）
    ↓
从组件配置获取指标ID
    ↓
从指标元数据获取穿透配置 ✅ 关键！
    ↓
组装查询参数
    ↓
执行穿透查询
    ↓
显示明细数据
```

### 3.2 数据流转

```
1. 组件配置存储在：bi_dashboard_component
   ├── id: 123
   └── data_config: {"metricIds": [1]}

2. 指标元数据存储在：bi_metric_metadata
   ├── id: 1
   ├── metric_name: 不良贷款率
   └── drill_through_config: "{...}"

3. 穿透查询流程：
   component.metricIds[0] = 1
   → 查询 bi_metric_metadata WHERE id = 1
   → 读取 drill_through_config
   → 执行穿透查询
```

---

## 四、与现有系统的集成

### 4.1 复用现有指标功能

系统已有：
- ✅ `bi_metric_metadata` 指标元数据表
- ✅ 指标详情对话框（`MetricDetailDialog`）
- ✅ 指标血缘关系
- ✅ 多指标支持（`metricIds`）

只需：
- ➕ 在 `bi_metric_metadata` 添加 `drill_through_config` 字段
- ➕ 修改穿透查询逻辑，从指标读取配置
- ❌ 删除组件级别的穿透配置（不需要了）

### 4.2 配置界面设计

#### 方案A：在指标管理界面配置（推荐）
```
指标管理页面
├── 指标列表
│   ├── 不良贷款率 [编辑]
│   ├── 贷款余额 [编辑]
│   └── ...
└── 编辑指标对话框
    ├── 基本信息
    ├── 业务定义
    ├── 计算公式
    └── 穿透配置 ✅ 新增
        ├── 启用穿透 [开关]
        ├── 目标表 [输入框]
        ├── 字段映射 [JSON编辑器]
        └── 展示字段 [JSON编辑器]
```

#### 方案B：在组件高级配置中选择（简化）
```
组件高级配置
├── 数据配置
│   ├── 指标选择
│   │   ├── 不良贷款率 (已配置穿透) ✅
│   │   ├── 贷款余额 (已配置穿透) ✅
│   │   └── 新增指标 (未配置) ⚠️
│   └── ...
└── 高级配置
    └── (不再需要穿透配置，自动从指标继承)
```

---

## 五、代码修改要点

### 5.1 删除组件级穿透配置

**从以下文件中删除：**
- ❌ `AdvancedConfig.vue` 中的 `drillThrough` 配置
- ❌ 组件 `advancedConfig` 结构中的 `drillThrough` 字段

### 5.2 添加指标级穿透配置

**修改指标元数据实体：**
```java
// MetricMetadata.java
@Table(name = "bi_metric_metadata")
public class MetricMetadata {
    // ... 现有字段 ...

    /** 穿透配置JSON */
    private String drillThroughConfig;

    // getter/setter
}
```

**修改指标详情对话框：**
- 添加"穿透配置"标签页
- 提供穿透规则配置表单

### 5.3 修改穿透查询逻辑

**`DashboardServiceImpl.drillThroughDetail()` 修改：**

```java
@Override
public DrillThroughResult drillThroughDetail(DrillThroughRequest request) {
    // 1. 获取组件配置
    DashboardComponent component = componentMapper.selectDashboardComponentById(request.getComponentId());

    // 2. 从组件获取指标ID列表
    List<Long> metricIds = parseMetricIds(component.getDataConfig());

    if (metricIds.isEmpty()) {
        return DrillThroughResult.failure("组件未配置指标");
    }

    // 3. 从指标获取穿透配置（使用第一个指标的配置）
    Long metricId = metricIds.get(0);
    MetricMetadata metric = metricMetadataMapper.selectMetricMetadataById(metricId);

    if (metric == null) {
        return DrillThroughResult.failure("指标不存在");
    }

    // 4. 解析穿透配置
    DrillThroughConfig drillConfig = parseDrillThroughConfig(metric.getDrillThroughConfig());

    if (drillConfig == null || !drillConfig.isEnabled()) {
        return DrillThroughResult.failure("该指标未配置穿透功能");
    }

    // 5. 执行查询（使用指标的穿透配置）
    DrillThroughResult result = queryExecutor.executeDrillThroughQuery(
        drillConfig.getDatasourceId(),
        drillConfig.getTargetTable(),
        buildFilters(request, drillConfig),
        drillConfig.getDisplayFields(),
        request.getPage(),
        request.getSize(),
        drillConfig.getMaxRows()
    );

    return result;
}
```

---

## 六、SQL变更脚本

```sql
-- ========================================
-- 添加穿透配置字段到指标元数据表
-- ========================================

ALTER TABLE bi_metric_metadata
ADD COLUMN `drill_through_config` TEXT COMMENT '穿透配置JSON' AFTER `technical_formula`;

CREATE INDEX idx_drill_through ON bi_metric_metadata(drill_through_config(100));

-- ========================================
-- 示例：为指标添加穿透配置
-- ========================================

UPDATE bi_metric_metadata
SET drill_through_config = '{
  "enabled": true,
  "datasourceId": 1,
  "targetTable": "npl_loan_detail",
  "fieldMappings": [
    {
      "sourceField": "dimensionValue",
      "sourceType": "click",
      "targetField": "region_name",
      "operator": "eq"
    }
  ],
  "additionalFilters": [
    {
      "field": "loan_status",
      "operator": "eq",
      "value": "1"
    }
  ],
  "displayFields": [
    {"field": "loan_id", "label": "贷款编号", "width": 120},
    {"field": "customer_name", "label": "客户名称", "width": 100},
    {"field": "loan_amount", "label": "贷款金额", "width": 100, "type": "DECIMAL"},
    {"field": "region_name", "label": "地区", "width": 80},
    {"field": "loan_date", "label": "贷款日期", "width": 100, "type": "DATE"}
  ],
  "pageSize": 50,
  "maxRows": 10000
}'
WHERE metric_code = 'NPL_RATIO';  -- 不良贷款率

-- ========================================
-- 删除不再需要的表和代码
-- ========================================

-- 删除独立的穿透配置表（如果已创建）
DROP TABLE IF EXISTS bi_component_drill_mapping;
DROP TABLE IF EXISTS bi_drill_through_config;
```

---

## 七、架构对比

### 错误架构（组件级配置）
```
问题：同一指标的穿透规则要配置多次
┌─────────────┐     drillThrough     ┌──────────────┐
│ 组件A（柱状图）│ ←─────────────────→  穿透配置1     │
│ 不良贷款率    │                     │ 目标：贷款明细 │
└─────────────┘                     └──────────────┘

┌─────────────┐     drillThrough     ┌──────────────┐
│ 组件B（饼图）  │ ←─────────────────→  穿透配置2     │
│ 不良贷款率    │                     │ 目标：贷款明细 │
└─────────────┘                     └──────────────┘

┌─────────────┐     drillThrough     ┌──────────────┐
│ 组件C（折线图）│ ←─────────────────→  穿透配置3     │
│ 不良贷款率    │                     │ 目标：贷款明细 │
└─────────────┘                     └──────────────┘

配置重复3次！维护成本高！
```

### 正确架构（指标级配置）
```
解决方案：配置在指标上，组件引用指标
┌─────────────┐     metricIds        ┌──────────────┐
│ 组件A（柱状图）│ ──────────────────→│ 不良贷款率    │
│              │                     │              │
└─────────────┘                     │ 穿透配置 ✅   │
                                     │ 目标：贷款明细 │
┌─────────────┐     metricIds        │              │
│ 组件B（饼图）  │ ──────────────────→│              │
│              │                     │              │
└─────────────┘                     └──────────────┘
                                     ┌──────────────┐
┌─────────────┐     metricIds        │ 贷款余额      │
│ 组件C（折线图）│ ──────────────────→│ (无穿透配置)  │
│              │                     └──────────────┘
└─────────────┘

配置一次，多处复用！✅
```

---

## 八、实施步骤

### 步骤1：扩展指标表
```sql
ALTER TABLE bi_metric_metadata ADD COLUMN drill_through_config TEXT;
```

### 步骤2：删除组件级穿透配置
- 删除 `bi_drill_through_config` 表相关代码
- 删除组件 `advancedConfig.drillThrough` 配置

### 步骤3：修改穿透查询逻辑
- 从指标ID获取指标元数据
- 从指标元数据读取 `drill_through_config`
- 使用指标的配置执行查询

### 步骤4：添加指标穿透配置界面
- 在指标管理/详情对话框添加穿透配置表单
- 支持配置的保存和验证

### 步骤5：测试
- 为指标配置穿透规则
- 在多个图表中引用该指标
- 双击测试穿透功能

---

## 九、优势总结

### 与错误架构对比

| 维度 | 错误架构（组件级） | 正确架构（指标级） |
|------|------------------|------------------|
| **配置次数** | 每个组件都要配置 | 指标配置一次 |
| **维护成本** | 高（修改需改多处） | 低（只改指标） |
| **一致性** | 难保证（容易配错） | 易保证（配置集中） |
| **复用性** | 无 | 高（一处配置，处处使用） |
| **业务语义** | 不清晰（组件不是业务实体） | 清晰（指标是业务实体） |

### 业务价值

1. **符合业务理解**：用户说"查看不良贷款率的明细"，而不是"查看柱状图的明细"
2. **配置复用**：一次配置，所有使用该指标的图表都获得穿透能力
3. **易于管理**：指标管理人员集中管理穿透规则
4. **扩展性强**：未来可以添加更多指标级别的属性（如数据权限、计算公式等）

---

## 十、总结

### 核心洞察
> **穿透是指标的行为特性，不是图表的展示特性**

### 设计原则
- **业务实体**：指标是业务实体，应该拥有业务属性
- **配置复用**：一次配置，多处使用
- **职责分离**：组件负责展示，指标负责业务逻辑

### 实施建议
✅ **立即采用正确架构**
- 删除组件级穿透配置
- 在指标元数据表中添加穿透配置
- 修改查询逻辑从指标读取配置
