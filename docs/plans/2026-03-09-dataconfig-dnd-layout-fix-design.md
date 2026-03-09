# DataConfig Drag-and-Drop Layout Fix Design

**Date:** 2026-03-09  
**Scope:** `ui/src/components/ConfigPanel/DataConfig.vue`, `ui/src/components/FieldManagementPanel/index.vue`  
**Problem:** In current top-bottom layout, when the field list is long, drag source and drop target are far apart, causing poor drag usability.

## 1. Layout Strategy

- Restore two-column layout in normal width:
  - Left: field configuration cards (`dc-cards`)
  - Right: field management panel (`field-management-area`)
- Fix right panel width to `300px`.
- Keep proper gap between columns (`12px`) for readability.
- Add responsive threshold:
  - If container width is below threshold (recommended `1200px`), switch to vertical layout automatically.
  - In vertical mode, right panel width becomes `100%`.

## 2. Scroll and Interaction Usability

- Keep right panel independently scrollable (`field-sections` remains `overflow-y: auto`).
- Constrain right panel visual height with viewport-based max-height so it does not push the whole page.
- Keep left drop zones stable and reachable during drag.
- Do not change drag/drop event flow or business logic.

## 3. Acceptance Criteria

- Normal width:
  - Two-column layout active.
  - No right-side content clipping.
  - Drag path from right field list to left drop zones is short and stable.
- Narrow width:
  - Layout auto-switches to vertical.
  - No clipping in field config or filter area.
- Functional regression:
  - datasource/dataset switch
  - dimension/metric drag-add/remove
  - calculated field add/edit/delete
  - filter add/edit/remove
- Build regression:
  - `cd ui && npm run build:prod` passes (existing unrelated warnings allowed).
