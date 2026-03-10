# 机构级指标解析与计算字段元数据化穿透 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在“计算字段必须元数据化”的前提下，实现按当前机构解析 `metricId` 的穿透流程，并保持现有 `metricId` 驱动穿透接口不变。

**Architecture:** 新增机构级指标解析接口 `resolve`，前端点击图表先解析机构内 `metricId` 再跳转穿透。设计态保存增加强校验，确保图表指标（含计算字段）均能映射到本机构指标元数据。后端继续复用现有 `/bi/drill/config/{metricId}` 与 `/bi/drill/query`。

**Tech Stack:** Vue2 + Element UI + Axios, Spring Boot + MyBatis, MySQL

---

### Task 1: 梳理并固化机构域内指标唯一约束

**Files:**
- Inspect/Modify: `sql/` 下指标元数据相关DDL脚本
- Inspect/Modify: `iras-bi/src/main/resources/mapper/metric/*`（按实际路径）
- Inspect: 指标元数据实体与Mapper（`iras-bi/src/main/java/com/zjrcu/iras/bi/metric/*`）

**Step 1: Write the failing test**
- 定义数据库约束期望：同机构 `metric_code` 不可重复，不同机构可重复。

**Step 2: Run test to verify it fails**
- 在测试库执行插入样例，验证当前约束是否不满足该规则。

**Step 3: Write minimal implementation**
- 增加/调整唯一索引为 `(org_id, metric_code)`。
- 更新Mapper查询条件，确保机构域过滤。

**Step 4: Run test to verify it passes**
- 复测同机构重复失败、跨机构重复成功。

**Step 5: Commit**
```bash
git add sql iras-bi/src/main/resources/mapper iras-bi/src/main/java/com/zjrcu/iras/bi/metric
git commit -m "feat: enforce org-scoped uniqueness for metric code"
```

### Task 2: 新增机构级指标解析接口 `/bi/metadata/resolve`

**Files:**
- Create: `iras-bi/src/main/java/com/zjrcu/iras/bi/metric/domain/dto/MetricResolveRequestDTO.java`
- Create: `iras-bi/src/main/java/com/zjrcu/iras/bi/metric/domain/dto/MetricResolveResponseDTO.java`
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/metric/controller/MetricMetadataController.java`（或现有元数据控制器）
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/metric/service/*`
- Modify: `iras-bi/src/main/resources/mapper/metric/*Mapper.xml`

**Step 1: Write the failing test**
- Controller/Service测试：
  - 请求 `metricCode`，按当前登录机构返回唯一 `metricId`。
  - 未命中返回明确错误。

**Step 2: Run test to verify it fails**
- Run: `mvn test -pl iras-bi -Dtest=*Metric*Resolve*Test`
- Expected: 接口不存在或断言失败。

**Step 3: Write minimal implementation**
- 新增 `POST /bi/metadata/resolve`。
- 从登录态获取机构ID，禁止前端传机构ID覆盖。
- 查询优先 `metricCode`，可选回退 `metricName`（仅机构内）。
- 返回唯一结果；多命中报“标识不唯一”。

**Step 4: Run test to verify it passes**
- Run: `mvn test -pl iras-bi -Dtest=*Metric*Resolve*Test`
- Expected: 测试通过。

**Step 5: Commit**
```bash
git add iras-bi/src/main/java/com/zjrcu/iras/bi/metric iras-bi/src/main/resources/mapper/metric
git commit -m "feat: add org-scoped metric resolve api"
```

### Task 3: 前端点击链路改为“先resolve再跳穿透”

**Files:**
- Modify: `ui/src/components/ChartWidget/index.vue`
- Modify: `ui/src/views/bi/dashboard/view.vue`
- Create/Modify: `ui/src/api/bi/metadata.js`

**Step 1: Write the failing test**
- 手工断言：计算字段只有名称无 `metricId` 时，当前点击仍直接失败。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 点击“未绑定 metricId 的计算字段图表”。
- Expected: 无法进入穿透。

**Step 3: Write minimal implementation**
- 新增前端API：`resolveMetricMetadata(payload)` 调 `/bi/metadata/resolve`。
- `ChartWidget` 点击时优先收敛出 `metricCode`（或标准化名称），触发事件把候选标识传给 `view.vue`。
- `view.vue` 在 `navigateToDrill` 前调用 resolve，拿到 `metricId` 后再路由跳转。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 机构内已注册指标可跳穿透，未注册给出明确提示。

**Step 5: Commit**
```bash
git add ui/src/components/ChartWidget/index.vue ui/src/views/bi/dashboard/view.vue ui/src/api/bi/metadata.js
git commit -m "feat: resolve org-scoped metric id before drill navigation"
```

### Task 4: 设计态强校验（图表保存时拦截未注册计算字段）

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`（按实际配置保存入口）
- Modify: `ui/src/components/DataConfigPanel/index.vue`
- Modify: 相关后端验证接口（如已有 `/bi/condition/validate` 逻辑可复用）

**Step 1: Write the failing test**
- 手工断言：当前可保存“仅表达式、无元数据映射”的计算字段图表。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 设计器中新增计算字段后直接保存。
- Expected: 当前保存未阻断。

**Step 3: Write minimal implementation**
- 保存前逐个校验指标（含计算字段）在当前机构是否可 resolve。
- 失败阻断保存并提示：`计算字段未注册为本机构指标元数据`。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 未注册时保存失败；注册后可保存。

**Step 5: Commit**
```bash
git add ui/src/components/ConfigPanel/DataConfig.vue ui/src/components/DataConfigPanel/index.vue
git commit -m "feat: enforce metric metadata registration for drill-enabled calculated fields"
```

### Task 5: 穿透页兼容与提示优化

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`

**Step 1: Write the failing test**
- 手工断言：缺少 `metricId` 时页面提示不明确。

**Step 2: Run test to verify it fails**
- 直接访问缺少 `metricId` 的 drill URL。
- Expected: 页面行为不友好或静默失败。

**Step 3: Write minimal implementation**
- 对无 `metricId` 场景增加明确错误提示与返回引导。
- 保持现有 A/B/C 逻辑不扩展新能力。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 缺参时提示清晰，正常场景不受影响。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue
git commit -m "fix: improve drill page feedback when metric id is unresolved"
```

### Task 6: 文档、迁移清单与回归

**Files:**
- Create: `docs/plans/2026-03-10-org-scoped-metric-resolve-drill-acceptance.md`
- Modify: `docs/plans/2026-03-09-drillthrough-detail-acceptance.md`
- Create (optional): SQL/脚本清单用于扫描“未注册计算字段图表”

**Step 1: Write the failing test**
- 文档缺少迁移指引与机构级约束说明。

**Step 2: Run test to verify it fails**
- 人工检查文档覆盖项。

**Step 3: Write minimal implementation**
- 补齐验收用例：跨机构同码、未注册阻断、注册后可穿透。
- 提供存量图表排查清单与回滚步骤。

**Step 4: Run test to verify it passes**
- Run: `npm run build:prod`
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test,*Metric*Resolve*Test`
- Expected: 构建/测试通过或明确环境阻塞项。

**Step 5: Commit**
```bash
git add docs/plans sql ui/src iras-bi/src
git commit -m "docs: add acceptance and migration guide for org-scoped metric drill"
```
