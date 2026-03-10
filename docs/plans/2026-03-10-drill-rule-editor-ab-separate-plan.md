# 穿透明细规则编辑重构（A/B分离 + 排序 + 日期动态控件） Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将穿透明细页区块A升级为可编辑带入条件（字段只读），保留区块B规则组能力，并新增多条排序与日期动态控件。

**Architecture:** 前端在 `drill.vue` 增加 `inheritedEditableRules + sortRules` 状态，查询时生成 `inheritedConditions/ruleGroups/sortRules` 三段协议。后端 `DrillServiceImpl` 新增 `sortRules` 校验与排序执行，兼容 metricId 与 field 两条查询链路。

**Tech Stack:** Vue2 + Element UI, Spring Boot + MyBatis

---

### Task 1: 区块A UI改造为“只读字段 + 可编辑操作符/值”

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`

**Step 1: Write the failing test**
- 手工断言：区块A仍为只读标签，不可编辑。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 打开穿透明细页，查看区块A。

**Step 3: Write minimal implementation**
- 引入 `inheritedEditableRules` 状态。
- 区块A每行改为：`*标签 + operator下拉 + value控件`。
- 字段只读，不提供字段编辑与删除。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 区块A可编辑 operator/value，字段不可改。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue
git commit -m "feat: make drill section A editable with readonly fields"
```

### Task 2: 日期字段动态控件（单日/区间）

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`

**Step 1: Write the failing test**
- 手工断言：日期字段无论操作符都使用同一值输入。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 在区块A选日期字段并切换操作符。

**Step 3: Write minimal implementation**
- `=`/`>`/`>=`/`<`/`<=` 使用单日日期选择器。
- `BETWEEN` 使用日期区间选择器。
- `IS NULL/IS NOT NULL` 隐藏或禁用值输入。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 控件按操作符动态切换。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue
git commit -m "feat: support dynamic date control by operator in drill section A"
```

### Task 3: 新增排序规则UI（多条sortRules）

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`

**Step 1: Write the failing test**
- 手工断言：页面无排序规则配置。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- Expected: 区块A没有排序行。

**Step 3: Write minimal implementation**
- 新增 `sortRules` 状态。
- 排序行支持：字段下拉 + 升序/降序 + 删除。
- “+”按钮可新增排序行。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: 可新增多条排序条件。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue
git commit -m "feat: add multi-sort rules in drill section A"
```

### Task 4: 前端查询协议接入 sortRules 与A区覆盖逻辑

**Files:**
- Modify: `ui/src/views/bi/dashboard/drill.vue`
- Modify: `ui/src/api/bi/drill.js`（如类型定义/请求封装需调整）

**Step 1: Write the failing test**
- 手工断言：A区编辑内容不覆盖带入条件，排序不随请求发送。

**Step 2: Run test to verify it fails**
- Run: `npm run dev`
- 观察请求 payload。

**Step 3: Write minimal implementation**
- 以 `inheritedEditableRules` 作为 `inheritedConditions` 来源。
- 合并区块B `ruleGroups`。
- 将 `sortRules` 传给 `/bi/drill/query` 与 `/bi/drill/queryByField`。

**Step 4: Run test to verify it passes**
- Run: `npm run dev`
- Expected: payload 包含覆盖后的 `inheritedConditions + ruleGroups + sortRules`。

**Step 5: Commit**
```bash
git add ui/src/views/bi/dashboard/drill.vue ui/src/api/bi/drill.js
git commit -m "feat: send inherited overrides and sort rules in drill query payload"
```

### Task 5: 后端DTO扩展 sortRules

**Files:**
- Create: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/DrillSortRuleDTO.java`
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/DrillQueryRequestDTO.java`
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto/DrillFieldQueryRequestDTO.java`

**Step 1: Write the failing test**
- 编译/测试断言：请求DTO无法接收 `sortRules`。

**Step 2: Run test to verify it fails**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`

**Step 3: Write minimal implementation**
- 增加 `sortRules` 字段到两种请求DTO。
- 定义 `field/order` 结构。

**Step 4: Run test to verify it passes**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`

**Step 5: Commit**
```bash
git add iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/dto
git commit -m "feat: add sortRules to drill query dto contracts"
```

### Task 6: 后端执行排序（白名单校验 + 多级排序）

**Files:**
- Modify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DrillServiceImpl.java`

**Step 1: Write the failing test**
- 用例：传入 `sortRules` 后结果顺序未变化。

**Step 2: Run test to verify it fails**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`

**Step 3: Write minimal implementation**
- 校验排序字段必须在数据集字段白名单。
- 校验排序方向仅 `ASC/DESC`。
- 在分页前执行多级排序（稳定排序）。

**Step 4: Run test to verify it passes**
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`

**Step 5: Commit**
```bash
git add iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DrillServiceImpl.java
git commit -m "feat: apply validated multi-sort rules in drill service"
```

### Task 7: 文档与回归清单更新

**Files:**
- Create: `docs/plans/2026-03-10-drill-rule-editor-ab-separate-acceptance.md`
- Modify: `docs/plans/2026-03-09-drillthrough-detail-acceptance.md`

**Step 1: Write the failing test**
- 当前文档未覆盖 A区可编辑、排序、日期动态控件。

**Step 2: Run test to verify it fails**
- 人工检查文档。

**Step 3: Write minimal implementation**
- 增补回归矩阵与已知限制。

**Step 4: Run test to verify it passes**
- Run: `npm run build:prod`
- Run: `mvn test -pl iras-bi -Dtest=*Drill*Test`
- 记录环境阻塞项（若有）。

**Step 5: Commit**
```bash
git add docs/plans ui/src iras-bi/src
git commit -m "docs: add acceptance for drill A/B editor and sorting"
```
