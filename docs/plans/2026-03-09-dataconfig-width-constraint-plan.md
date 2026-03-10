# DataConfig Width Constraint Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Prevent any clipping in Data tab while preserving left-right field workflow by enforcing strict width constraints that always fit the 400px config panel.

**Architecture:** Keep business logic unchanged; apply deterministic layout constraints at panel/container/card levels. Use fixed field-area widths and anti-overflow rules with compact wrapping for filter controls.

**Tech Stack:** Vue 2 (Options API), Element UI, scoped CSS/SCSS, Vue CLI build.

---

### Task 1: Baseline Width Audit

**Files:**
- Modify: none
- Test: `ui/src/components/ConfigPanel/index.vue`, `ui/src/components/ConfigPanel/DataConfig.vue`, `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

```bash
rg -n "config-panel|el-tab-pane > div|width|min-width|max-width|overflow-x|dc-main|field-management-area|gap" ui/src/components/ConfigPanel/index.vue ui/src/components/ConfigPanel/DataConfig.vue ui/src/components/FieldManagementPanel/index.vue
```

**Step 2: Run test to verify it fails**

Expected: mixed rules still allow potential overflow at current widths.

**Step 3: Write minimal implementation**

No code change.

**Step 4: Run test to verify it passes**

Re-run command and record baseline.

**Step 5: Commit**

No commit.

### Task 2: Hard Constraint at ConfigPanel Layer

**Files:**
- Modify: `ui/src/components/ConfigPanel/index.vue`
- Test: `ui/src/components/ConfigPanel/index.vue`

**Step 1: Write the failing test**

```bash
rg -n "config-panel|el-tab-pane > div|width: 400px|overflow" ui/src/components/ConfigPanel/index.vue
```

**Step 2: Run test to verify it fails**

Expected: tab child width is hard-coded and may conflict with inner padding rules.

**Step 3: Write minimal implementation**

Refine width rules:
- keep panel width 400
- tab-pane direct child use `width: 100%` instead of hard 400
- ensure box-sizing and overflow boundaries remain strict

**Step 4: Run test to verify it passes**

```bash
git diff -- ui/src/components/ConfigPanel/index.vue
```

Expected: width boundary rules are deterministic and non-overflowing.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/index.vue
git commit -m "style: enforce safe tab content width in ConfigPanel"
```

### Task 3: Field Area Fixed Split in DataConfig

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "dc-main|field-config-area|field-management-area|gap|overflow-x|228px|148px|8px" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Expected: current split does not match target 228/148/8 deterministic layout.

**Step 3: Write minimal implementation**

Apply fixed split:
- left area 228px
- right area 148px
- gap 8px
- enforce `max-width: 100%`, `min-width: 0`, `overflow-x: hidden`

**Step 4: Run test to verify it passes**

```bash
git diff -- ui/src/components/ConfigPanel/DataConfig.vue
```

Expected: only width/overflow/layout style updates.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: apply fixed left-right width split for DataConfig field area"
```

### Task 4: Right Column Compactness and Scroll Safety

**Files:**
- Modify: `ui/src/components/FieldManagementPanel/index.vue`
- Test: `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

```bash
rg -n "field-name|text-overflow|white-space|overflow-y|field-sections|delete-btn" ui/src/components/FieldManagementPanel/index.vue
```

**Step 2: Run test to verify it fails**

Expected: right column may still use widths that are too generous for 148px.

**Step 3: Write minimal implementation**

Tighten compact behavior:
- preserve single-line ellipsis for field names
- keep vertical scrolling only
- ensure controls/icons do not force overflow

**Step 4: Run test to verify it passes**

Manual check in Data tab:
- long field names are ellipsized
- no horizontal clipping/scroll

**Step 5: Commit**

```bash
git add ui/src/components/FieldManagementPanel/index.vue
git commit -m "style: compact field management panel for constrained width"
```

### Task 5: Filter Area Overflow-Proofing

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "filter-list|filter-item|flex-wrap|el-select|el-input|min-width|overflow" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Expected: filter row may still overflow in some combinations.

**Step 3: Write minimal implementation**

Refine wrapping:
- compact row padding/gaps
- ensure controls wrap safely
- ensure no horizontal overflow

**Step 4: Run test to verify it passes**

Manual check:
- add multiple filters
- edit/select values
- no clipping across rows

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: harden filter layout against overflow in DataConfig"
```

### Task 6: Regression + Build Verification

**Files:**
- Validate: `ui/src/components/ConfigPanel/index.vue`
- Validate: `ui/src/components/ConfigPanel/DataConfig.vue`
- Validate: `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

Checklist:
1. datasource block no clipping
2. field config + field management no clipping
3. filters no clipping
4. no horizontal scrollbar in Data tab

**Step 2: Run test to verify it fails**

Validate before final polish.

**Step 3: Write minimal implementation**

Fix only residual overflow defects.

**Step 4: Run test to verify it passes**

```bash
cd ui
npm run build:prod
```

Expected: build succeeds; existing unrelated warnings may remain.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/index.vue ui/src/components/ConfigPanel/DataConfig.vue ui/src/components/FieldManagementPanel/index.vue
git commit -m "test: verify DataConfig width-constraint regression and build"
```
