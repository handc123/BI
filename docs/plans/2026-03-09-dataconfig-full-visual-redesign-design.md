# DataConfig Full Visual Redesign Design

**Date:** 2026-03-09  
**Scope:** `ui/src/components/ConfigPanel/DataConfig.vue`, `ui/src/components/FieldManagementPanel/index.vue`  
**Style Direction:** Enterprise blue-gray, balanced density, restrained default with clear interaction feedback.

## 1. Information Architecture

- Convert DataConfig into a stable work path: select data -> drag fields -> set filters -> preview.
- Use a top toolbar layout:
  - Left: datasource and dataset selectors.
  - Right: primary actions (preview and refresh).
- Use a two-column main workspace:
  - Left (main): dimension, metric, and filter cards in operation order.
  - Right (side): field management with collapsible groups and quick navigation.
- Unify each module as `header + count/hint + body` to remove fragmented visual blocks.

## 2. Visual Language

- Establish a blue-gray token set with clear hierarchy:
  - Page background, card background, subtle section background, border, primary text, secondary text, primary accent.
- Keep card visuals consistent:
  - 8px radius, 1px border, minimal shadow (hover/active only).
- Typography and rhythm:
  - Body text 13px, assist text 12px, title weight 600.
  - Medium compact spacing, avoid both crowding and excessive whitespace.
- State design:
  - Default is quiet.
  - Hover is light.
  - Drag-over/active is clearly visible via primary border and soft primary fill.

## 3. Component Interaction Design

### DataConfig

- Add semantic layout classes for toolbar, main area, card wrapper, card header, and card body.
- Rebuild dimension/metric drop areas into one visual system.
- Rebuild selected field rows into compact, readable chips with clear remove affordance.
- Convert filter rows to aligned row cards; keep add/remove actions stable and predictable.

### FieldManagementPanel

- Rebuild header as title + summary area with lighter background.
- Unify group headers (height, icon alignment, spacing).
- Rebuild field rows as light tag-like lines with clean hover and drag cues.
- Keep calculated-field edit/delete affordances; show delete action on hover to reduce noise.

### Unified Interaction Rules

- No positional motion for hover; use border/background only.
- Drag-over state has highest priority and must not conflict with hover.
- Empty states stay explicit but low-noise.

## 4. Verification and Acceptance

- Functional regression:
  - datasource/dataset switch
  - drag-drop add/remove in dimension and metric
  - calculated field add/edit/delete
  - filter add/edit/remove
  - preview and refresh behavior unchanged
- Visual acceptance:
  - Clear hierarchy at first glance
  - Left/right panel style consistency
  - Distinguishable hover/active/drag states without harsh contrast
  - No layout break on common desktop widths
- Build verification:
  - `cd ui && npm run build:prod`
  - Existing unrelated warnings may be documented; no new errors from this redesign.
