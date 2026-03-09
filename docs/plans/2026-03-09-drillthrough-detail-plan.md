# BI穿透明细 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现图表点击一键跳转明细页，自动带入图表查询条件与指标口径条件，并支持多条件组(AND/OR)动态查询。

**Architecture:** 复用现有图表点击事件(`metric-click`/`multi-metric-click`)，新增独立明细路由页承载查询。后端新增指标级明细配置读取与明细执行接口，按“带入条件 + 用户规则”合并生成最终查询条件，冲突字段由用户规则覆盖。

**Tech Stack:** Vue2 + Element UI + Vue Router + Vuex, Spring Boot + MyBatis, MySQL

---

### Task 1: 明细路由与页面骨架

**Files:**
- Modify: `ui/src/router/index.js`
- Create: `ui/src/views/bi/dashboard/drill.vue`

**Step 1: Write the failing test**
- 在现有前端无自动化测试前提下，先写手工验收点到页面注释：访问 `/bi/dashboard/drill/23` 应渲染基础页面标题。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- Expected: 访问路由 404 或空白（尚未注册路由）。

**Step 3: Write minimal implementation**
- 注册新路由 `/bi/dashboard/drill/:dashboardId`。
- 新建 `drill.vue`，仅渲染页面容器、标题、占位区域(A/B/C区块)。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 路由可访问并看到基础页面结构。

**Step 5: Commit**
```bash
git add ui/src/router/index.js ui/src/views/bi/dashboard/drill.vue
git commit -m "feat: add drill detail route and page scaffold"
```

### Task 2: 图表点击跳转与参数透传

**Files:**
- Modify: `ui/src/views/bi/dashboard/view.vue`
- Modify: `ui/src/components/ChartWidget/index.vue` (仅补齐事件参数，如需)

**Step 1: Write the failing test**
- 手工验收点：点击折线图某条线后未跳转，或跳转后缺少 `metricId`。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- Expected: 当前行为仅弹指标详情，不进入独立明细页。

**Step 3: Write minimal implementation**
- 在 `view.vue` 的 `handleMetricClick/handleMultiMetricClick` 中构造路由参数并 `push` 到 `drill` 页：
  - `metricId`
  - `componentId`
  - 当前查询条件快照(`queryParams`)
  - 关键点位信息（日期/维度值，若可取）
- 多指标场景必须传被点击线的 `metricId`。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 点击不同线，跳转URL中 `metricId` 不同。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/view.vue ui/src/components/ChartWidget/index.vue
git commit -m "feat: route chart clicks to drill detail with metric context"
```

### Task 3: 区块A“已带入条件”展示与口径解析降级

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`
- Create: `ui/src/utils/metricExpressionParser.js`

**Step 1: Write the failing test**
- 新增解析工具单测（如项目允许前端单测）；若无测试基建，写最小 Node 断言脚本验证：
  - 输入表达式 `SUM(CASE WHEN wjfl in ('次级','可疑') THEN jkye ELSE 0 END) / SUM(jkye)`
  - 输出包含 `wjfl IN (次级, 可疑)`。

**Step 2: Run test to verify it fails**
- Expected: 尚无解析器时失败。

**Step 3: Write minimal implementation**
- `metricExpressionParser` 提供：
  - `parseMetricConditions(expression)` -> 结构化条件数组
  - 解析失败返回 `{ ok:false, message }`
- `drill.vue` 区块A展示：
  - 图表查询条件（机构/日期/指标）
  - 指标口径条件（只读标签）
  - 失败时提示“口径条件解析失败，可手动补充筛选规则”
  - 每条口径条件按钮“转为可编辑条件”

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 区块A可显示两类带入条件；解析失败有提示且不阻断页面。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue ui/src/utils/metricExpressionParser.js
git commit -m "feat: show inherited conditions and parse metric expression rules"
```

### Task 4: 区块B多条件组规则编辑器

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`
- (可选) Create: `ui/src/components/DrillRuleBuilder/index.vue`

**Step 1: Write the failing test**
- 手工验收点：无法新增第二个条件组或无法设置组间 AND/OR。

**Step 2: Run test to verify it fails**
- Expected: 当前仅占位，无规则编辑能力。

**Step 3: Write minimal implementation**
- 实现规则数据结构：
  - `groups: [{ id, relationWithPrev, rules:[{field, operator, value}] }]`
- 实现UI：
  - 新增/删除组、组间关系AND/OR
  - 组内新增/删除条件
  - 字段中文展示（value传英文编码）
  - 操作符中文展示
- 实现“转为可编辑条件”：将区块A口径条件复制进当前组。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 可完成多组规则配置与编辑。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue ui/src/components/DrillRuleBuilder/index.vue
git commit -m "feat: add multi-group drill filter rule builder"
```

### Task 5: 后端指标级明细配置接口

**Files:**
- Create: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/MetricDrillConfigDTO.java`
- Create/Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/*`
- Create/Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/*`
- Create/Modify: `iras-bi/src/main/resources/mapper/platform/*Mapper.xml`

**Step 1: Write the failing test**
- 新增 service/controller 测试：按 `metricId` 获取不到明细配置返回404/错误。

**Step 2: Run test to verify it fails**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`
- Expected: 接口不存在或断言失败。

**Step 3: Write minimal implementation**
- 新增接口：`GET /bi/drill/config/{metricId}`
- 返回：数据源、字段字典、参数映射、默认规则（如有）
- 做空值校验与权限校验。

**Step 4: Run test to verify it passes**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`
- Expected: 测试通过。

**Step 5: Commit**
```bash
git add iras-bi/src/main/java/com/zjrcu/iras/bi/platform iras-bi/src/main/resources/mapper/platform
git commit -m "feat: add metric-level drill config query api"
```

### Task 6: 后端明细执行接口（带入条件 + 用户规则合并）

**Files:**
- Create: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/DrillQueryRequest.java`
- Create/Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/*`
- Create/Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/*`
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java` (如需)

**Step 1: Write the failing test**
- 测试用例：同字段冲突时，用户规则覆盖带入条件；组间AND/OR生效。

**Step 2: Run test to verify it fails**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`
- Expected: 合并规则未实现，断言失败。

**Step 3: Write minimal implementation**
- 新增接口：`POST /bi/drill/query`
- 处理流程：
  1. 加载 `metricId` 对应明细配置
  2. 校验字段/操作符白名单
  3. 合并条件：先带入，再覆盖（用户规则优先）
  4. 参数化查询并分页返回
- 失败场景返回明确错误码与信息。

**Step 4: Run test to verify it passes**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`
- Expected: 规则合并/分页测试通过。

**Step 5: Commit**
```bash
git add iras-bi/src/main/java/com/zjrcu/iras/bi/platform
git commit -m "feat: add drill detail query api with rule-group merge"
```

### Task 7: 前后端联调与回归

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`
- Modify: `ui/src/api/bi/*.js`
- (必要时) Modify: `ui/src/components/ChartWidget/index.vue`

**Step 1: Write the failing test**
- 手工回归清单：
  - 单指标卡点击进入明细
  - 多指标折线点击不同线进入不同 `metricId`
  - 区块A口径条件可转为区块B
  - 冲突字段用户规则覆盖成功

**Step 2: Run test to verify it fails**
- 预期至少1项失败（接口未接通/参数不匹配）。

**Step 3: Write minimal implementation**
- 接通 `drill config` 与 `drill query` API。
- 完成查询按钮、分页、重置。
- 增加解析失败提示与空状态提示。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Run: `mvn test -pl iras-bi`
- Expected: 核心链路通过，页面行为符合需求。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue ui/src/api/bi iras-bi/src/main/java/com/zjrcu/iras/bi/platform
git commit -m "feat: integrate drill detail page with backend apis"
```

### Task 8: 文档与验收记录

**Files:**
- Create: `docs/plans/2026-03-09-drillthrough-detail-acceptance.md`
- Modify: 相关 README（如 `ui/src/components/ChartWidget/README.md`）

**Step 1: Write the failing test**
- 验收条目初稿未覆盖“解析失败降级”“多指标点击映射”。

**Step 2: Run test to verify it fails**
- 人工检查清单不完整。

**Step 3: Write minimal implementation**
- 补齐验收清单、已知限制、回滚方案、排查指引。

**Step 4: Run test to verify it passes**
- 人工评审通过。

**Step 5: Commit**
```bash
git add docs/plans/2026-03-09-drillthrough-detail-acceptance.md ui/src/components/ChartWidget/README.md
git commit -m "docs: add drill detail acceptance and usage guide"
```
