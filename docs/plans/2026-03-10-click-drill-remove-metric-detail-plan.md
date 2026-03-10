# 图表单击穿透并下线指标详情 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将仪表板图表点击行为统一为“单击跳穿透”，并全项目移除指标详情弹窗能力。

**Architecture:** 以 `ChartWidget -> DashboardView -> drill route` 作为唯一点击链路。删除 `MetricDetailDialog` 组件及其页面引用，保留与指标元数据有关但非详情弹窗的能力。修复 `listMetricMetadata` 导入源，避免运行时 `is not a function`。

**Tech Stack:** Vue2, Element UI, Vue Router, Axios API 模块

---

### Task 1: 建立影响面基线与失败复现

**Files:**
- Inspect: `ui/src/components/ChartWidget/index.vue`
- Inspect: `ui/src/views/bi/dashboard/view.vue`
- Inspect: `ui/src/components/MetricDetailDialog/*`
- Inspect: `ui/src/api/bi/metric.js`
- Inspect: `ui/src/api/bi/metadata.js`

**Step 1: Write the failing test**
- 先定义手工失败点：点击图表时控制台报错 `listMetricMetadata is not a function`，并可能仍出现指标详情弹窗。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 操作：进入仪表板查看页，点击图表。
- Expected: 复现上述报错/弹窗行为。

**Step 3: Write minimal implementation**
- 暂不改代码，先记录现状截图与控制台日志用于回归对照。

**Step 4: Run test to verify baseline captured**
- 再次点击图表，确认复现稳定。

**Step 5: Commit**
```bash
git add docs/plans/2026-03-10-click-drill-remove-metric-detail-design.md
git commit -m "docs: capture baseline for click-to-drill migration"
```

### Task 2: 修复 ChartWidget 元数据导入并收敛点击语义

**Files:**
- Modify: `ui/src/components/ChartWidget/index.vue`
- Modify (if needed): `ui/src/api/bi/metric.js`
- Inspect: `ui/src/api/bi/metadata.js`

**Step 1: Write the failing test**
- 手工断言：点击图表不应再出现 `listMetricMetadata is not a function`。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 操作：点击未预配置 `metricId` 的图表，触发自动匹配分支。
- Expected: 当前分支会报错。

**Step 3: Write minimal implementation**
- 在 `ChartWidget` 将 `listMetricMetadata` 导入改为 `@/api/bi/metadata`。
- `handleChartClick` 保留并强化“只为穿透服务”的事件触发。
- 无可穿透指标时仅提示，不发详情相关事件。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 控制台不再报 `is not a function`，点击行为可继续进入穿透链路或给出提示。

**Step 5: Commit**
```bash
git add ui/src/components/ChartWidget/index.vue
git commit -m "fix: correct metric metadata api import in chart click flow"
```

### Task 3: DashboardView 移除指标详情弹窗并只保留穿透跳转

**Files:**
- Modify: `ui/src/views/bi/dashboard/view.vue`

**Step 1: Write the failing test**
- 手工断言：模板和脚本中仍包含 `MetricDetailDialog` 与其状态字段。

**Step 2: Run test to verify it fails**
- Run: `rg -n "MetricDetailDialog|metricDialogVisible|selectedMetricId|selectedMetricIds|selectedMetricList" ui/src/views/bi/dashboard/view.vue`
- Expected: 有匹配结果。

**Step 3: Write minimal implementation**
- 删除 `MetricDetailDialog` 导入、注册、模板节点。
- 删除弹窗相关状态字段。
- `handleMetricClick/handleMultiMetricClick` 仅调用 `navigateToDrill`。

**Step 4: Run test to verify it passes**
- Run: `rg -n "MetricDetailDialog|metricDialogVisible|selectedMetricId|selectedMetricIds|selectedMetricList" ui/src/views/bi/dashboard/view.vue`
- Expected: 无匹配。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/view.vue
git commit -m "refactor: remove metric detail dialog from dashboard view"
```

### Task 4: 全项目下线 MetricDetailDialog 组件及入口

**Files:**
- Delete: `ui/src/components/MetricDetailDialog/index.vue`
- Delete: `ui/src/components/MetricDetailDialog/DataQueryTab.vue`
- Delete: `ui/src/components/MetricDetailDialog/LineageTab.vue`
- Delete: `ui/src/components/MetricDetailDialog/SpecificationTab.vue`
- Modify (if needed): any residual imports found by search

**Step 1: Write the failing test**
- 手工断言：项目中仍存在 `MetricDetailDialog` 引用。

**Step 2: Run test to verify it fails**
- Run: `rg -n "MetricDetailDialog|components/MetricDetailDialog" ui/src`
- Expected: 有匹配结果。

**Step 3: Write minimal implementation**
- 删除组件目录文件。
- 修复删除后所有编译错误的引用点。

**Step 4: Run test to verify it passes**
- Run: `rg -n "MetricDetailDialog|components/MetricDetailDialog" ui/src`
- Expected: 无匹配。

**Step 5: Commit**
```bash
git add ui/src
git commit -m "refactor: remove metric detail dialog component and references"
```

### Task 5: 点击链路回归验证（单指标/多指标/缺失指标）

**Files:**
- Verify runtime behavior on `ui/src/views/bi/dashboard/view.vue`
- Verify runtime behavior on `ui/src/components/ChartWidget/index.vue`

**Step 1: Write the failing test**
- 回归清单：
  1. 单指标点击必须跳穿透
  2. 多指标点击必须带正确 `metricId`
  3. 无指标只提示

**Step 2: Run test to verify it fails (if regression exists)**
- Run: `npm run dev`
- 操作：按三类场景逐项点击验证。
- Expected: 若有回归，记录具体场景。

**Step 3: Write minimal implementation**
- 仅修复失败场景，不引入新交互。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 三类场景全部符合预期。

**Step 5: Commit**
```bash
git add ui/src/components/ChartWidget/index.vue ui/src/views/bi/dashboard/view.vue
git commit -m "fix: enforce click-to-drill behavior across metric scenarios"
```

### Task 6: 构建验证与交付文档更新

**Files:**
- Modify: `docs/plans/2026-03-09-drillthrough-detail-acceptance.md`
- Create (optional): `docs/plans/2026-03-10-click-drill-remove-metric-detail-acceptance.md`

**Step 1: Write the failing test**
- 交付文档未反映“指标详情已下线”。

**Step 2: Run test to verify it fails**
- 人工检查验收文档。

**Step 3: Write minimal implementation**
- 更新验收清单、已知限制、回滚说明。

**Step 4: Run test to verify it passes**
- Run: `npm run build:prod`
- Expected: 构建通过且无 `MetricDetailDialog` 相关报错。

**Step 5: Commit**
```bash
git add docs/plans ui/src
git commit -m "docs: update acceptance after metric detail removal"
```
