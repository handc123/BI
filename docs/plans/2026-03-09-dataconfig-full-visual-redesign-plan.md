# DataConfig Full Visual Redesign Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Deliver a full visual redesign for DataConfig and FieldManagementPanel with enterprise blue-gray style and unchanged business behavior.

**Architecture:** Keep API/data/event logic untouched. Apply semantic template wrappers only where needed for clearer layout targeting, then rebuild scoped styles into a shared visual rhythm (toolbar, card system, drag/drop states, list rows, filter rows). Validate via focused regression checks and production build.

**Tech Stack:** Vue 2 (Options API), Element UI, scoped CSS/SCSS, Vue CLI (`npm run build:prod`).

---

### Task 1: Baseline Snapshot and Selector Mapping

**Files:**
- Modify: none
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`, `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

Capture current structure and key selectors.

```bash
rg -n "<template>|<style scoped|field-config-layout|field-management-area|drop-zone|selected-field|filter-item" ui/src/components/ConfigPanel/DataConfig.vue
rg -n "<template>|<style scoped|field-management-panel|panel-header|field-section|field-item|add-field-btn" ui/src/components/FieldManagementPanel/index.vue
```

**Step 2: Run test to verify it fails**

Run commands above.  
Expected: Current structure does not yet match redesigned hierarchy.

**Step 3: Write minimal implementation**

No code change. Confirm baseline only.

**Step 4: Run test to verify it passes**

Re-run commands and save output snippets in task notes.

**Step 5: Commit**

No commit for baseline task.

### Task 2: Rebuild DataConfig Layout Skeleton

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

Define expected semantic wrappers.

```bash
rg -n "dc-toolbar|dc-main|dc-cards|dc-card|dc-card__header|dc-card__body" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Run command above.  
Expected: selectors/classes absent.

**Step 3: Write minimal implementation**

Add semantic container classes in template only (no logic change):
- top toolbar wrapper for datasource/dataset + actions
- main two-column wrapper (left cards, right panel)
- card header/body wrappers for dimension/metric/filter modules

**Step 4: Run test to verify it passes**

```bash
rg -n "dc-toolbar|dc-main|dc-cards|dc-card|dc-card__header|dc-card__body" ui/src/components/ConfigPanel/DataConfig.vue
git diff -- ui/src/components/ConfigPanel/DataConfig.vue
```

Expected: wrappers present; no JS logic changes.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "refactor: add semantic layout skeleton for DataConfig redesign"
```

### Task 3: Implement DataConfig Visual System

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "--bg-page|--bg-card|--bg-subtle|--border|--text-main|--text-secondary|--primary|--primary-soft|dc-card|drop-zone|selected-field|filter-item" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Run command above.  
Expected: tokenized system incomplete/absent.

**Step 3: Write minimal implementation**

Rework scoped styles:
- define local CSS tokens for blue-gray system
- style toolbar, card headers, card bodies with consistent 8px card system
- redesign drop zones and selected rows for restrained default + clear drag feedback
- redesign filter rows with aligned controls and stable action placement

**Step 4: Run test to verify it passes**

```bash
git diff -- ui/src/components/ConfigPanel/DataConfig.vue
```

Expected: style + class-targeting changes only.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: redesign DataConfig with enterprise blue-gray visual system"
```

### Task 4: Rebuild FieldManagementPanel Structure and Styles

**Files:**
- Modify: `ui/src/components/FieldManagementPanel/index.vue`
- Test: `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

```bash
rg -n "panel-header|field-sections|section-header|field-list|field-item|calculated-field|add-field-btn" ui/src/components/FieldManagementPanel/index.vue
```

**Step 2: Run test to verify it fails**

Run command above.  
Expected: old style rhythm still present.

**Step 3: Write minimal implementation**

Update template/style minimally:
- normalize header and section structure
- align section icons/count positions
- rebuild field rows and calculated-field affordances for hover/drag clarity
- keep all emits/events unchanged

**Step 4: Run test to verify it passes**

```bash
git diff -- ui/src/components/FieldManagementPanel/index.vue
```

Expected: no business logic change; style/structure updates only.

**Step 5: Commit**

```bash
git add ui/src/components/FieldManagementPanel/index.vue
git commit -m "style: redesign FieldManagementPanel to match DataConfig visual system"
```

### Task 5: Cross-Component Consistency and Interaction Polish

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`
- Modify: `ui/src/components/FieldManagementPanel/index.vue`
- Test: both files above

**Step 1: Write the failing test**

Create interaction checklist:
1. hover does not shift layout
2. drag-over has highest visual priority
3. empty states are readable but low-noise
4. action hierarchy is clear

**Step 2: Run test to verify it fails**

Manual check in designer view: identify mismatched states or inconsistent spacing.

**Step 3: Write minimal implementation**

Apply small style adjustments only for consistency across both panels.

**Step 4: Run test to verify it passes**

Manual verification:
- datasource/dataset selection works
- drag/drop to dimension/metric works
- filter add/remove works
- calculated field add/edit/delete works
- preview/refresh works

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue ui/src/components/FieldManagementPanel/index.vue
git commit -m "style: align interaction states across DataConfig and FieldManagementPanel"
```

### Task 6: Build Verification and Final Regression

**Files:**
- Validate: `ui/src/components/ConfigPanel/DataConfig.vue`
- Validate: `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

Define final acceptance checklist from design doc:
- hierarchy clarity
- visual consistency
- no functional regression
- successful production build

**Step 2: Run test to verify it fails**

Before final polish, checklist should expose any residual issues.

**Step 3: Write minimal implementation**

Fix only remaining visual defects; no feature changes.

**Step 4: Run test to verify it passes**

```bash
cd ui
npm run build:prod
```

Expected:
- Build succeeds.
- Existing unrelated warnings may remain; no new errors caused by this redesign.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue ui/src/components/FieldManagementPanel/index.vue
git commit -m "test: verify DataConfig full visual redesign regression and build"
```
