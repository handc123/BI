# Remove DataConfig Metric Tabs Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove the "基础指标" and "计算指标" tab-based management from `DataConfig.vue` while preserving field drag-and-drop metric configuration and existing preview/refresh behavior.

**Architecture:** Keep the datasource-tab flow as the single source of truth. Eliminate metric-tab state/methods/templates and stop emitting `metricConfig`. Preserve compatibility by still reading legacy configs safely and mirroring `metrics` to `measures`.

**Tech Stack:** Vue 2 (Options API), Element UI, vuedraggable, RuoYi Vue frontend build (`vue-cli-service`).

---

### Task 1: Remove Metric-Tab UI and Imports

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

Use static checks to confirm deprecated UI/usage exists before changes.

```bash
rg -n "name=\"baseMetrics\"|name=\"computedMetrics\"|metric-config-dialog|handleAddMetric\('base'\)|handleAddMetric\('computed'\)" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Run command above.
Expected: matches found (deprecated UI still present).

**Step 3: Write minimal implementation**

- Remove `<el-tab-pane name="baseMetrics">...</el-tab-pane>`.
- Remove `<el-tab-pane name="computedMetrics">...</el-tab-pane>`.
- Remove `<metric-config-dialog ... />` block.
- Remove `import MetricConfigDialog from '@/components/MetricConfigDialog'` and component registration.

**Step 4: Run test to verify it passes**

```bash
rg -n "name=\"baseMetrics\"|name=\"computedMetrics\"|metric-config-dialog|handleAddMetric\('base'\)|handleAddMetric\('computed'\)" ui/src/components/ConfigPanel/DataConfig.vue
```

Expected: no matches.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "refactor: remove metric tabs from DataConfig"
```

### Task 2: Remove Metric-Management State/Methods and Emission

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "baseMetrics|computedMetrics|metricDialogVisible|currentEditingMetric|currentEditingIndex|currentEditingType|metricConfig:" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Run command above.
Expected: matches found.

**Step 3: Write minimal implementation**

- Remove metric-tab-only state fields from `data()`.
- Remove metric methods:
  - `handleAddMetric`, `handleEditMetric`, `handleDeleteMetric`, `performDeleteMetric`
  - `checkMetricDependencies`, `handleMetricSubmit`, `handleMetricReorder`
  - `syncMetricConfig`, `getMetricSqlPreview`, `getComputeTypeLabel`
- In `initDataConfig()`, stop reading `component.dataConfig.metricConfig`.
- In `emitChange()`, remove emitted `metricConfig` payload.
- Keep `syncMetricsToConfig()` mirroring `metrics` to `measures`.

**Step 4: Run test to verify it passes**

```bash
rg -n "metricDialogVisible|currentEditingMetric|currentEditingIndex|currentEditingType|metricConfig:" ui/src/components/ConfigPanel/DataConfig.vue
```

Expected: no matches.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "refactor: drop legacy metricConfig flow in DataConfig"
```

### Task 3: Remove Dead Styles and Validate Build

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "metrics-panel|metrics-header|metrics-list|metric-item|metric-drag|metric-info|metric-actions|sql-preview" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Run command above.
Expected: matches found.

**Step 3: Write minimal implementation**

- Delete style blocks only used by removed metric tabs.
- Keep styles used by datasource tab and calculated-field panel.

**Step 4: Run test to verify it passes**

```bash
rg -n "metrics-panel|metrics-header|metrics-list|metric-item|metric-drag|metric-info|metric-actions|sql-preview" ui/src/components/ConfigPanel/DataConfig.vue
cd ui
npm run build:prod
```

Expected:
- first command returns no matches
- build succeeds without compile errors

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: remove unused DataConfig metric-tab styles"
```

### Task 4: Regression Verification for Designer Integration

**Files:**
- Validate behavior in:
  - `ui/src/components/ConfigPanel/DataConfig.vue`
  - `ui/src/views/bi/dashboard/designer.vue`

**Step 1: Write the failing test**

Manual test checklist (before fix expected to still show removed tabs):
1. Open designer and select a chart component.
2. Open DataConfig panel.
3. Verify only datasource tab exists (this currently fails before implementation).

**Step 2: Run test to verify it fails**

Run local UI:

```bash
cd ui
npm run dev
```

Expected before implementation: old tabs visible.

**Step 3: Write minimal implementation**

Apply previous tasks only; do not modify unrelated files.

**Step 4: Run test to verify it passes**

Manual checks:
1. Only datasource tab is visible.
2. Drag dimension/metric fields still updates chart config.
3. Preview data works.
4. Saving dashboard succeeds.
5. Re-open saved dashboard: no runtime error.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "test: verify DataConfig single-tab behavior"
```
