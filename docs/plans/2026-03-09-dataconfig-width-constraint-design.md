# DataConfig Width Constraint Design

**Date:** 2026-03-09  
**Scope:** `ui/src/components/ConfigPanel/DataConfig.vue`, `ui/src/components/FieldManagementPanel/index.vue`, `ui/src/components/ConfigPanel/index.vue`  
**Goal:** Keep Data tab content fully inside panel bounds with no clipping while preserving left-right field configuration workflow.

## 1. Layout Constraint Strategy

- Keep top datasource block full-width.
- Keep left-right layout for field area with fixed widths:
  - Left field config: `228px`
  - Right field management: `148px`
  - Gap: `8px`
- Effective total width: `384px`, leaving safe space for panel padding/border inside the 400px config panel.
- Add hard anti-overflow constraints:
  - `overflow-x: hidden` on DataConfig container level
  - child blocks use `max-width: 100%`, `min-width: 0`, `box-sizing: border-box`

## 2. Narrow-Column Usability Rules

- Right field management (`148px`):
  - field name single-line ellipsis
  - no horizontal scrolling
  - vertical scroll only
  - action button appears on hover to save width
- Left drop area (`228px`):
  - selected field rows remain single-line ellipsis
  - text region keeps `min-width: 0`
  - remove icon fixed to right edge
- Filter area:
  - compact wrap layout
  - controls auto-wrap to next line when needed
  - no horizontal overflow

## 3. Acceptance Criteria

- No clipping for:
  - datasource block
  - field config block
  - field management block
  - filter block
- No horizontal scrollbar in Data tab content.
- Drag-and-drop remains usable from right to left.
- Functional regression checks:
  - datasource/dataset switch
  - dimension/metric drag add/remove
  - filter add/edit/remove
  - calculated field add/edit/delete
- Build check:
  - `cd ui && npm run build:prod` succeeds (existing unrelated warnings allowed).
