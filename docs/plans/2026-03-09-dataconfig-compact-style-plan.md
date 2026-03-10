# DataConfig Compact Style Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refine `DataConfig.vue` and `FieldManagementPanel` into a medium-compact, cleaner style without changing existing behavior.

**Architecture:** Apply scoped CSS refinements only, with minimal template/class adjustments if needed for selector targeting. Keep all business logic, data flow, and events unchanged. Validate with static checks, manual interaction testing, and production build.

**Tech Stack:** Vue 2 (Options API), Element UI, scoped SCSS/CSS in single-file components, Vue CLI build.

---

### Task 1: Baseline Verification and Safety Snapshot

**Files:**
- Modify: none
- Test: `ui/src/components/ConfigPanel/DataConfig.vue`, `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

Capture current baseline selectors and spacing targets.

```bash
rg -n "<style|\.data-config|\.field-config-layout|\.drop-zone|\.selected-field|\.filter-item" ui/src/components/ConfigPanel/DataConfig.vue
rg -n "<style|panel|field-item|drag|search|header" ui/src/components/FieldManagementPanel/index.vue
```

**Step 2: Run test to verify it fails**

Run commands above.
Expected: existing style structure is present and still non-compact.

**Step 3: Write minimal implementation**

No code change; establish baseline references for later diff review.

**Step 4: Run test to verify it passes**

Re-run same commands; output should be stable.

**Step 5: Commit**

No commit for this setup-only task.

### Task 2: Compact Rhythm in DataConfig Container and Layout

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

Define expected compact adjustments by selector presence.

```bash
rg -n "\.data-config|\.el-form-item|\.el-divider|\.field-config-layout|\.field-management-area" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Run command above.
Expected: selectors exist with current larger spacing values.

**Step 3: Write minimal implementation**

Update scoped styles in `DataConfig.vue`:
- Reduce `.data-config` padding.
- Reduce `.el-form-item` margin-bottom.
- Reduce `.el-divider` vertical margin.
- Reduce `.field-config-layout` gap.
- Slightly tighten `.field-management-area` width/visual weight.

Keep behavior unchanged.

**Step 4: Run test to verify it passes**

```bash
git diff -- ui/src/components/ConfigPanel/DataConfig.vue
```

Expected: only style/layout-related changes in targeted selectors.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: tighten DataConfig container and layout spacing"
```

### Task 3: Compact Drop Zone and Selected Field Visuals

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "\.drop-zone|\.drop-zone\.is-over|\.drop-placeholder|\.selected-field" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Run command above.
Expected: current values still less compact.

**Step 3: Write minimal implementation**

Update scoped styles:
- Reduce `.drop-zone` min-height/padding.
- Keep drag-over feedback but lighter visual emphasis.
- Reduce `.drop-placeholder` icon/text spacing and font size slightly.
- Reduce `.selected-field` row height/padding with preserved click area for remove icon.

**Step 4: Run test to verify it passes**

Manual visual spot-check in designer:
- Drag area more compact.
- Tags still readable/clickable.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: compact DataConfig drop zones and selected field chips"
```

### Task 4: Compact Filter Row and Action Area

**Files:**
- Modify: `ui/src/components/ConfigPanel/DataConfig.vue`

**Step 1: Write the failing test**

```bash
rg -n "\.filter-list|\.filter-item|\.filter-item > \*|\.filter-item \.el-input" ui/src/components/ConfigPanel/DataConfig.vue
```

**Step 2: Run test to verify it fails**

Run command above.
Expected: current spacing values remain baseline.

**Step 3: Write minimal implementation**

Refine filter/action spacing:
- Tighten `.filter-item` gap and row spacing.
- Ensure input/select alignment remains clean.
- Keep action buttons visually separated but compact.

**Step 4: Run test to verify it passes**

Manual check:
- Add/remove filter still clean and readable.
- No control overlap at common viewport widths.

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue
git commit -m "style: compact DataConfig filters and action row"
```

### Task 5: Compact Style Alignment in FieldManagementPanel

**Files:**
- Modify: `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

```bash
rg -n "<style|header|search|field-item|drag|calculated|section" ui/src/components/FieldManagementPanel/index.vue
```

**Step 2: Run test to verify it fails**

Run command above.
Expected: panel still using looser spacing.

**Step 3: Write minimal implementation**

Adjust scoped styles in `FieldManagementPanel`:
- Reduce header/search/list spacing.
- Compact field row item height and padding.
- Harmonize icon-text alignment with DataConfig rhythm.
- Keep drag/hover visual cues but reduce noise.

**Step 4: Run test to verify it passes**

Manual check in designer:
- Left and right panels now visually consistent.
- Drag behavior unaffected.

**Step 5: Commit**

```bash
git add ui/src/components/FieldManagementPanel/index.vue
git commit -m "style: compact FieldManagementPanel spacing and item density"
```

### Task 6: Regression Check and Build Verification

**Files:**
- Validate:
  - `ui/src/components/ConfigPanel/DataConfig.vue`
  - `ui/src/components/FieldManagementPanel/index.vue`

**Step 1: Write the failing test**

Regression checklist (manual):
1. Open BI dashboard designer and select a chart component.
2. Open DataConfig panel.
3. Verify compact style appears in both panels.
4. Verify all key interactions still work.

**Step 2: Run test to verify it fails**

Before implementation, style mismatch and loose spacing should be visible.

**Step 3: Write minimal implementation**

No additional code unless regression found.

**Step 4: Run test to verify it passes**

```bash
cd ui
npm run build:prod
```

Expected:
- Build succeeds.
- Existing warnings may remain, but no new compile errors from these style changes.

Manual verification:
- datasource/dataset selection works
- drag-drop works
- filter add/remove works
- calculated field dialog works
- preview/refresh buttons work

**Step 5: Commit**

```bash
git add ui/src/components/ConfigPanel/DataConfig.vue ui/src/components/FieldManagementPanel/index.vue
git commit -m "test: verify compact style regression checks for DataConfig flow"
```
