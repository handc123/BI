# 不良贷款率指标血缘 - 测试完成指南

## ✅ 数据创建成功

### 数据库：ry-vue

**已创建的数据：**
- ✅ 5张指标血缘表（bi_metric_metadata, bi_metric_lineage等）
- ✅ 9个指标元数据
- ✅ 8条血缘关系
- ✅ 14个菜单权限

**核心指标ID：**
- **NPL_RATIO（不良贷款率）ID = 6**

**已配置的图表组件：**
- 图表组件ID = 3（仪表盘）
- 已关联 metricId = 6

---

## 🎯 立即测试

### 第一步：启动前端

```bash
cd ui
npm run dev
```

### 第二步：访问看板

访问以下URL之一（根据已配置的图表）：
- `http://localhost:80/views/bi/dashboard/view/3`  （仪表盘ID=3）
- `http://localhost:80/views/bi/dashboard/view/4`
- `http://localhost:80/views/bi/dashboard/view/5`

### 第三步：点击图表

1. 找到"仪表盘"图表（或其他已配置的图表）
2. 鼠标悬停会显示蓝色边框（可点击提示）
3. 点击图表
4. 查看指标详情对话框

---

## 📊 数据验证

### 查询所有指标

```sql
USE ry-vue;

SELECT
    id,
    metric_code,
    metric_name,
    business_definition,
    technical_formula,
    owner_dept
FROM bi_metric_metadata
WHERE metric_code LIKE 'NPL_%' OR metric_code IN ('TOTAL_LOAN_BALANCE', 'PROVISION_BALANCE')
ORDER BY metric_code;
```

### 查询血缘关系

```sql
SELECT
    ml.id,
    upstream.metric_code AS upstream_code,
    upstream.metric_name AS upstream_name,
    downstream.metric_code AS downstream_code,
    downstream.metric_name AS downstream_name,
    ml.transformation_type,
    ml.dependency_strength
FROM bi_metric_lineage ml
JOIN bi_metric_metadata upstream ON ml.upstream_metric_id = upstream.id
JOIN bi_metric_metadata downstream ON ml.downstream_metric_id = downstream.id
ORDER BY downstream.metric_code;
```

### 查询已配置的图表

```sql
SELECT
    c.id,
    c.component_name,
    c.dashboard_id,
    JSON_EXTRACT(c.data_config, '$.metricId') AS metric_id,
    m.metric_code,
    m.metric_name
FROM bi_dashboard_component c
LEFT JOIN bi_metric_metadata m ON JSON_EXTRACT(c.data_config, '$.metricId') = m.id
WHERE JSON_EXTRACT(c.data_config, '$.metricId') IS NOT NULL;
```

---

## 🎬 功能测试清单

### ✅ 指标规范标签页

- [ ] 查看指标编码、名称、责任部门
- [ ] 查看业务定义
- [ ] 查看技术公式
- [ ] 查看计算逻辑
- [ ] 查看数据新鲜度、更新频率
- [ ] 点击"编辑"按钮修改业务定义

**预期结果：**
- 指标编码：`NPL_RATIO`
- 指标名称：`不良贷款率`
- 业务定义：`不良贷款余额 / 各项贷款余额 × 100%`
- 技术公式：`(NPL_BALANCE_TOTAL / TOTAL_LOAN_BALANCE) * 100`

---

### ✅ 血缘图谱标签页

- [ ] 查看完整血缘图谱
- [ ] 切换到"上游血缘"模式
- [ ] 切换到"下游血缘"模式
- [ ] 点击节点查看详情
- [ ] 放大/缩小图谱
- [ ] 导出图谱为PNG图片

**预期结果：**
- 完整图谱：显示6个节点、8条关系
- 上游血缘：看到各项贷款余额和3个次级指标
- 下游血缘：看到不良贷款率变化
- 节点颜色：蓝色（指标）、绿色（数据集）

---

### ✅ 数据查询标签页

- [ ] 查看数据概览（记录数、新鲜度）
- [ ] 设置时间范围筛选
- [ ] 设置排序字段和方式
- [ ] 点击"查询"按钮
- [ ] 查看数据表格
- [ ] 点击"导出"按钮（需权限）

---

## 🔍 血缘图谱预览

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

**指标清单：**
1. NPL_BALANCE_SUBCLASS - 不良贷款余额_次级类
2. NPL_BALANCE_DOUBTFUL - 不良贷款余额_可疑类
3. NPL_BALANCE_LOSS - 不良贷款余额_损失类
4. TOTAL_LOAN_BALANCE - 各项贷款余额
5. NPL_BALANCE_TOTAL - 不良贷款余额_总计
6. **NPL_RATIO - 不良贷款率** ⭐
7. PROVISION_BALANCE - 贷款拨备余额
8. PROVISION_COVERAGE_RATIO - 拨备覆盖率
9. NPL_RATIO_CHANGE - 不良贷款率变化

---

## 🛠️ 故障排查

### 问题1：点击图表没有反应

**检查：**
```sql
-- 验证图表是否配置了metricId
SELECT id, component_name, data_config
FROM bi_dashboard_component
WHERE id = 3;
```

**解决：**
```sql
-- 重新配置
UPDATE bi_dashboard_component
SET data_config = JSON_SET(COALESCE(data_config, '{}'), '$.metricId', 6)
WHERE id = 3;
```

### 问题2：血缘图谱显示为空

**检查：**
```sql
-- 验证血缘关系
SELECT COUNT(*) FROM bi_metric_lineage;
-- 应该返回 8
```

### 问题3：API报错404

**检查：**
- 后端服务是否启动
- 端口8080是否正常
- Swagger是否可访问：`http://localhost:8080/swagger-ui.html`

---

## 📞 下一步

### 为更多图表配置指标

```sql
-- 为所有"折线图"类型的图表配置不良贷款率指标
UPDATE bi_dashboard_component
SET data_config = JSON_SET(COALESCE(data_config, '{}'), '$.metricId', 6)
WHERE component_type = 'chart'
  AND component_name = '折线图';
```

### 创建更多指标

参考 `INSERT_NPL_METRIC_DATA.sql`，创建：
- 不良贷款率分地区
- 不良贷款率分行业
- 不良贷款率趋势预测
- 拨备覆盖率分地区

---

## 🎉 测试成功标志

当您看到以下内容时，说明测试成功：

✅ 点击图表后弹出对话框
✅ 三个标签页都能正常显示
✅ 血缘图谱显示6个节点、8条关系
✅ 可以点击节点查看详情
✅ 数据查询能正常执行
✅ 所有功能按钮响应正常

---

**测试完成时间：** 2025-02-26
**数据库：** ry-vue
**核心指标：** NPL_RATIO（ID=6）
**状态：** ✅ 就绪
