# DataConfig Drag-and-Drop Layout Fix Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Restore drag usability by reintroducing a responsive two-column layout where field source and drop targets stay close in normal widths without causing clipping in narrow widths.

**Architecture:** Apply CSS/layout refinements only. Keep DataConfig business logic, drag/drop events, and API flow unchanged. Use fixed right-panel width in normal view and threshold-based fallback to vertical layout for constrained widths.

**Tech Stack:** Vue 2 (Options API), Element UI, scoped CSS/SCSS, Vue CLI build.

---

### Task 1: Baseline Snapshot

**Files:**
- Modify: none
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`, `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

```bash
rg -n "dc-main|dc-cards|field-management-area|dc-toolbar|filter-item|@media" ui/src/components/ConfigPanel/DataConfig.vue
rg -n "field-sections|overflow-y|max-height" ui/src/components/FieldManagementPanel/index.vue
```

**Step 2: Run test to verify it fails**

Expected: current `dc-main` is vertical; right panel is full-width.

**Step 3: Write minimal implementation**

No code change.

**Step 4: Run test to verify it passes**

Re-run commands; capture baseline.

**Step 5: Commit**

No commit for baseline.

### Task 2: Reintroduce Two-Column Main Layout

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "dc-main|dc-cards|field-management-area" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Expected: `dc-main` vertical-only behavior still present.

**Step 3: Write minimal implementation**

Update style rules:
- `dc-main` -> horizontal flex in default view
- `dc-cards` -> `flex: 1; min-width: 0`
- `field-management-area` -> fixed width `300px`
- keep spacing `12px`

**Step 4: Run test to verify it passes**

```bash
git diff -- ui/src/components/ConfigPanel/DataConfig.vue
```

Expected: only layout-related style changes.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: restore two-column DataConfig layout for drag usability"
```

### Task 3: Responsive Threshold Fallback

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "@media|1200px|dc-main|field-management-area" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Expected: no dedicated threshold behavior for this layout goal.

**Step 3: Write minimal implementation**

Add responsive block:
- at/below threshold (`max-width: 1200px`) switch `dc-main` to column
- right panel width to `100%`
- preserve toolbar non-clipping behavior

**Step 4: Run test to verify it passes**

```bash
git diff -- ui/src/components/ConfigPanel/DataConfig.vue
```

Expected: clear default + fallback responsive rules.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: add responsive threshold fallback for DataConfig main layout"
```

### Task 4: Right Panel Scroll Height Guard

**Files:**
- Modify: `ui/src/components/FieldManagementPanel/index.vue`
- Test: `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

```bash
rg -n "field-sections|overflow-y|max-height|height" ui/src/components/FieldManagementPanel/index.vue
```

**Step 2: Run test to verify it fails**

Expected: no viewport-guarded height constraint for long field lists.

**Step 3: Write minimal implementation**

Apply bounded scroll area behavior:
- keep independent right-panel scrolling
- ensure panel does not push whole page excessively when field list is long

**Step 4: Run test to verify it passes**

Manual check in designer:
- long list still scrolls in right panel
- left drop zones remain quickly reachable

**Step 5: Commit**

```bash
git add ui/src/components/FieldManagementPanel/index.vue
git commit -m "style: constrain field panel scroll region for long lists"
```

### Task 5: Regression and Build Verification

**Files:**
- Validate: `ui/src/components/ConfigPanel/DataConfig.vue`
- Validate: `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

Checklist:
1. Normal width: two-column layout and no clipping.
2. Narrow width: auto-switch vertical and no clipping.
3. Long field list: right panel scrolls; drag remains practical.

**Step 2: Run test to verify it fails**

Validate before final polish.

**Step 3: Write minimal implementation**

Fix only residual layout defects.

**Step 4: Run test to verify it passes**

```bash
cd ui
npm run build:prod
```

Expected: build succeeds; existing unrelated warnings may remain.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue ui/src/components/FieldManagementPanel/index.vue
git commit -m "test: verify DataConfig drag-and-drop layout fix regression and build"
```
