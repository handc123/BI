# 双轨穿透（计算字段元数据化 + 原始字段直穿） Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现“计算字段走 metricId、原始字段直穿 dataset+field”的统一穿透能力，并在指标选择框中同时支持元数据指标与数据集字段。

**Architecture:** 前端点击统一采集指标上下文（isCalculated/metricField/metricCode），在 `view/drill` 分流到两条后端查询链路。后端新增 `queryByField` 保持与现有 `query` 相同返回结构。设计器在新增计算字段时自动做指标元数据落库/复用。

**Tech Stack:** Vue2 + Element UI + Axios, Spring Boot + MyBatis

---

### Task 1: 指标选择框双分组（元数据指标 + 数据集字段）

**Files:**
- Modify: `ui/src/components/DataConfigPanel/index.vue`
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`（如存在统一数据配置入口）
- Modify: `ui/src/api/bi/metadata.js`

**Step 1: Write the failing test**
- 手工断言：当前指标区无法同时清晰展示两类来源，或无来源标签。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 进入图表配置页查看指标选择区。

**Step 3: Write minimal implementation**
- 拉取当前机构+当前数据集可用元数据指标。
- 与数据集原始指标字段合并为双分组结构。
- 每项显示来源标签（`元数据`/`字段`）。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 指标选择区双分组正常显示，选择行为可区分来源。

**Step 5: Commit**
```bash
git add ui/src/components/DataConfigPanel/index.vue ui/src/components/ConfigPanel/DataConfig.vue ui/src/api/bi/metadata.js
git commit -m "feat: show metadata metrics and dataset fields in grouped metric selector"
```

### Task 2: 新增计算字段自动元数据化（同码复用）

**Files:**
- Modify: `ui/src/components/DataConfigPanel/index.vue`
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`
- Modify: `ui/src/api/bi/metadata.js`

**Step 1: Write the failing test**
- 手工断言：新增计算字段后不会自动落到元数据，或重复创建同机构同码。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 在设计器新增计算字段并保存。

**Step 3: Write minimal implementation**
- 保存计算字段时先调用 `resolve`。
- 未命中则自动创建元数据；命中则复用已有 `metricId`。
- 图表配置持久化 `metricId + metricCode + expression`。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 首次创建成功，重复同码复用不重复新增。

**Step 5: Commit**
```bash
git add ui/src/components/DataConfigPanel/index.vue ui/src/components/ConfigPanel/DataConfig.vue ui/src/api/bi/metadata.js
git commit -m "feat: auto register calculated metric metadata with org-scoped reuse"
```

### Task 3: 前端点击链路补全双轨上下文

**Files:**
- Modify: `ui/src/components/ChartWidget/index.vue`
- Modify: `ui/src/views/bi/dashboard/view.vue`

**Step 1: Write the failing test**
- 手工断言：点击时无法区分计算字段和原始字段，或只能走单链路。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 点击计算字段与原始字段图表。

**Step 3: Write minimal implementation**
- `ChartWidget` 事件中附带：`isCalculated`、`metricField`、`metricCode`、`datasetId`。
- `view.vue` 根据 `isCalculated` 选择：
  - 计算字段：resolve `metricId` -> drill
  - 原始字段：直接带 `datasetId + metricField` -> drill

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 两类指标都能进入同一穿透页，路由参数正确。

**Step 5: Commit**
```bash
git add ui/src/components/ChartWidget/index.vue ui/src/views/bi/dashboard/view.vue
git commit -m "feat: route drill by metric type with dual-path context"
```

### Task 4: 后端新增原始字段直穿接口 `queryByField`

**Files:**
- Create: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/DrillFieldQueryRequestDTO.java`
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/DrillController.java`
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/IDrillService.java`
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DrillServiceImpl.java`

**Step 1: Write the failing test**
- 新增Service测试：`datasetId + metricField` 查询当前不存在接口实现。

**Step 2: Run test to verify it fails**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`

**Step 3: Write minimal implementation**
- 新增 `POST /bi/drill/queryByField`。
- 校验 `metricField` 必须在数据集字段白名单内。
- 复用现有条件合并、规则组过滤、分页返回。
- 返回结构与 `/bi/drill/query` 一致。

**Step 4: Run test to verify it passes**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`

**Step 5: Commit**
```bash
git add iras-bi/src/main/java/com/zjrcu/iras/bi/platform
git commit -m "feat: add field-based drill query api for raw metrics"
```

### Task 5: 穿透页统一接入双链路

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`
- Modify: `ui/src/api/bi/drill.js`

**Step 1: Write the failing test**
- 手工断言：当前 `drill.vue` 只支持 `metricId` 链路。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 用原始字段路由参数打开穿透页。

**Step 3: Write minimal implementation**
- `drill.vue` 初始化时判断：
  - 有 `metricId` -> 调 `getDrillConfig/queryDrillDetail`
  - 有 `datasetId+metricField` -> 调 `queryByField`
- 区块A保留并展示来源链路信息（只读）。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 两链路都可查询并分页。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue ui/src/api/bi/drill.js
git commit -m "feat: support dual-path drill query in drill page"
```

### Task 6: 验收文档与回归

**Files:**
- Create: `docs/plans/2026-03-10-dual-path-drill-acceptance.md`
- Modify: `docs/plans/2026-03-09-drillthrough-detail-acceptance.md`

**Step 1: Write the failing test**
- 现有验收文档未覆盖双轨链路。

**Step 2: Run test to verify it fails**
- 人工检查文档清单。

**Step 3: Write minimal implementation**
- 补齐双轨测试矩阵：
  - 计算字段已注册/未注册
  - 原始字段直穿
  - 同名指标来源区分

**Step 4: Run test to verify it passes**
- Run: `npm run build:prod`
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test,*Metric*Resolve*Test`
- 记录环境阻塞项（若有）

**Step 5: Commit**
```bash
git add docs/plans ui/src iras-bi/src
git commit -m "docs: add dual-path drill acceptance and regression checklist"
```
