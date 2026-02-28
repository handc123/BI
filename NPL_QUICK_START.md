# 不良贷款率指标血缘 - 快速开始

## 📊 已创建的指标体系

### 指标血缘图

```
                    [各项贷款余额]
                          |
                          v
[次级] ----+          [不良贷款率] ----> [不良贷款率变化]
           |               |
[可疑] ----+----> [不良贷款总额]
           |               |
[损失] ----+               +----> [拨备覆盖率]
                           |
                   [拨备余额]
```

### 指标清单

| 指标编码 | 指标名称 | 类型 | 说明 |
|---------|---------|------|------|
| **NPL_BALANCE_SUBCLASS** | 不良贷款余额_次级类 | 基础 | 次级类贷款余额 |
| **NPL_BALANCE_DOUBTFUL** | 不良贷款余额_可疑类 | 基础 | 可疑类贷款余额 |
| **NPL_BALANCE_LOSS** | 不良贷款余额_损失类 | 基础 | 损失类贷款余额 |
| **TOTAL_LOAN_BALANCE** | 各项贷款余额 | 基础 | 所有贷款余额总和 |
| **NPL_BALANCE_TOTAL** | 不良贷款余额_总计 | 聚合 | 次级+可疑+损失 |
| **NPL_RATIO** | **不良贷款率** | **衍生** | **核心监管指标** |
| **PROVISION_BALANCE** | 贷款拨备余额 | 基础 | 拨备金额 |
| **PROVISION_COVERAGE_RATIO** | 拨备覆盖率 | 衍生 | 监管指标 |
| **NPL_RATIO_CHANGE** | 不良贷款率变化 | 衍生 | 趋势指标 |

---

## 🚀 快速开始（3步完成测试）

### 第一步：导入测试数据

```bash
# 在项目根目录执行
mysql -u root -p iras < INSERT_NPL_METRIC_DATA.sql
```

执行后会看到：
- ✅ 创建 9 个指标元数据
- ✅ 创建 8 条血缘关系
- ✅ 完整的不良贷款指标血缘体系

### 第二步：查看指标ID

```sql
-- 查看所有不良贷款相关指标
SELECT
    id,
    metric_code,
    metric_name,
    business_definition
FROM bi_metric_metadata
WHERE metric_code LIKE 'NPL_%' OR metric_code IN ('TOTAL_LOAN_BALANCE', 'PROVISION_BALANCE')
ORDER BY metric_code;
```

**重要：记住 `NPL_RATIO`（不良贷款率）的 ID，后面要用！**

### 第三步：为图表配置指标ID

**方式1：通过数据库直接配置（最快）**

```sql
-- 1. 查看所有图表组件
SELECT id, component_name, data_config
FROM bi_dashboard_component;

-- 2. 假设要为 id=123 的图表关联不良贷款率指标（假设 NPL_RATIO 的 id=10）
UPDATE bi_dashboard_component
SET data_config = JSON_SET(
    COALESCE(data_config, '{}'),
    '$.metricId',
    10  -- ← 替换为实际的 NPL_RATIO 的 id
)
WHERE id = 123;  -- ← 替换为实际的图表组件 id
```

**方式2：通过界面配置**

1. 进入看板设计器：`/bi/dashboard/designer/:id`
2. 点击某个图表组件
3. 在"数据配置"中找到"指标ID"字段
4. 输入 `NPL_RATIO` 的 ID（从第二步查询得到）
5. 保存配置

---

## 🎯 测试完整功能

### 1. 查看指标规范

- 打开配置了指标ID的图表所在看板
- 点击图表
- 查看"指标规范"标签页
- **应该看到**：不良贷款率的业务定义和技术公式

### 2. 查看血缘图谱

- 切换到"血缘图谱"标签页
- **应该看到**：
  - 6个节点（3个基础指标+2个聚合指标+1个衍生指标）
  - 8条关系线
  - 完整的数据流向图

### 3. 查看上游血缘

- 点击"上游血缘"按钮
- **应该看到**：
  - 各项贷款余额 → 不良贷款率
  - 次级/可疑/损失 → 不良贷款总额 → 不良贷款率

### 4. 查看下游血缘

- 点击"下游血缘"按钮
- **应该看到**：
  - 不良贷款率 → 不良贷款率变化
  - 不良贷款总额 → 拨备覆盖率

### 5. 查看数据查询

- 切换到"数据查询"标签页
- 设置查询条件
- 点击"查询"按钮
- **应该看到**：non_performing_loan_data 表的数据

---

## 📋 业务场景示例

### 场景1：新员工理解指标口径

**问题**："不良贷款率是怎么算的？"

**操作**：
1. 点击不良贷款率图表
2. 查看"指标规范"
3. 看到技术公式：`(NPL_BALANCE_TOTAL / TOTAL_LOAN_BALANCE) * 100`

**结果**：一目了然，不良贷款率 = 不良贷款总额 / 各项贷款余额

---

### 场景2：监管检查时追溯数据来源

**问题**："不良贷款总额的数据从哪来？"

**操作**：
1. 点击不良贷款率图表
2. 切换到"血缘图谱"
3. 点击"上游血缘"
4. 找到"不良贷款总额"节点

**结果**：清晰看到不良贷款总额由次级、可疑、损失三类贷款汇总而成

---

### 场景3：分析拨备覆盖率异常

**问题**："拨备覆盖率为什么下降了？"

**操作**：
1. 点击拨备覆盖率图表（如果已配置）
2. 查看血缘关系
3. 发现受两个因素影响：拨备余额、不良贷款余额
4. 逐个分析上游指标

**结果**：快速定位是哪个上游指标变化导致的

---

## 🔧 完整血缘关系查询

```sql
-- 查询不良贷款率指标的完整血缘链路
WITH RECURSIVE lineage_tree AS (
    -- 起始节点：不良贷款率
    SELECT
        mm.id,
        mm.metric_code,
        mm.metric_name,
        mm.business_definition,
        0 as level,
        CAST(mm.metric_name AS CHAR(500)) as path
    FROM bi_metric_metadata mm
    WHERE mm.metric_code = 'NPL_RATIO'

    UNION ALL

    -- 递归查询上游
    SELECT
        upstream.id,
        upstream.metric_code,
        upstream.metric_name,
        upstream.business_definition,
        lt.level + 1,
        CONCAT(upstream.metric_name, ' → ', lt.path)
    FROM bi_metric_lineage ml
    JOIN bi_metric_metadata upstream ON ml.upstream_metric_id = upstream.id
    JOIN lineage_tree lt ON ml.downstream_metric_id = lt.id
    WHERE lt.level < 5  -- 最多追溯5层
)
SELECT * FROM lineage_tree ORDER BY level, metric_code;
```

**预期输出**：
```
level | metric_code        | metric_name         | path
------+-------------------+---------------------+------------------
0     | NPL_RATIO         | 不良贷款率          | 不良贷款率
1     | NPL_BALANCE_TOTAL | 不良贷款余额_总计   | 不良贷款余额_总计 → 不良贷款率
1     | TOTAL_LOAN_BALANCE| 各项贷款余额        | 各项贷款余额 → 不良贷款率
2     | NPL_BALANCE_SUBCLASS | 不良贷款余额_次级 | 不良贷款余额_次级 → 不良贷款余额_总计 → 不良贷款率
2     | NPL_BALANCE_DOUBTFUL | 不良贷款余额_可疑 | 不良贷款余额_可疑 → 不良贷款余额_总计 → 不良贷款率
2     | NPL_BALANCE_LOSS  | 不良贷款余额_损失   | 不良贷款余额_损失 → 不良贷款余额_总计 → 不良贷款率
```

---

## 💡 使用技巧

### 技巧1：颜色识别节点类型

- 🔵 **蓝色**：指标节点
- 🟢 **绿色**：数据集节点
- 🟠 **橙色**：数据源节点

### 技巧2：点击节点查看详情

在血缘图谱中点击任意节点，右侧会显示：
- 节点ID、名称
- 节点类型
- 业务定义
- 技术公式

### 技巧3：导出血缘图谱

点击"导出图片"按钮，将血缘关系保存为PNG格式，可用于：
- 文档编写
- 培训材料
- 监管报送

### 技巧4：追踪数据变化

在"数据查询"标签页：
- 设置时间范围（如最近30天）
- 查看历史数据趋势
- 导出数据进行深入分析

---

## 🐛 常见问题

### Q1: 执行SQL后没有数据？

**检查**：
```sql
-- 验证指标是否创建成功
SELECT COUNT(*) FROM bi_metric_metadata WHERE metric_code LIKE 'NPL_%';
-- 应该返回 9

-- 验证血缘关系是否创建成功
SELECT COUNT(*) FROM bi_metric_lineage;
-- 应该大于等于 8
```

### Q2: 血缘图谱显示为空？

**原因**：图表没有配置 `metricId`

**解决**：
```sql
-- 查询 NPL_RATIO 的 id
SELECT id, metric_code, metric_name
FROM bi_metric_metadata
WHERE metric_code = 'NPL_RATIO';

-- 更新图表组件（替换为实际ID）
UPDATE bi_dashboard_component
SET data_config = JSON_SET(data_config, '$.metricId', 10)  -- 替换10为实际ID
WHERE id = 123;  -- 替换123为实际组件ID
```

### Q3: 数据查询报错？

**检查**：
1. 数据集ID（dataset_id）是否正确
2. non_performing_loan_data 表是否有数据
3. 数据源连接是否正常

---

## 📞 下一步

完成测试后，可以：

1. **扩展指标体系**
   - 添加更多银行业指标
   - 建立跨部门指标血缘
   - 创建指标对比分析

2. **优化血缘展示**
   - 调整图谱布局
   - 添加节点样式
   - 自定义关系标签

3. **数据质量监控**
   - 监控指标数据异常
   - 设置告警规则
   - 自动生成质量报告

---

**完成测试后，您就拥有了一个完整的指标血缘管理系统！** 🎉
