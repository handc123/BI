# DataConfig Compact Style Design

- Date: 2026-03-09
- Scope:
  - `ui/src/components/ConfigPanel/DataConfig.vue`
  - `ui/src/components/FieldManagementPanel/index.vue`
- Goal: Improve visual quality with a medium-compact, information-dense layout while keeping existing business behavior unchanged.

## Background
Current DataConfig UI works functionally but appears visually loose and not refined enough. The user requested a compact style update for both DataConfig and FieldManagementPanel.

## Decisions (Approved)
1. Visual direction: Minimal and compact.
2. Scope: Update both `DataConfig.vue` and `FieldManagementPanel`.
3. Density level: Medium compact (more content per screen without harming readability).
4. Approach: Minimal-intrusion styling (CSS-focused), no business logic changes.

## Options Considered
1. Recommended (selected): minimal-intrusion style refinement
- Adjust spacing, border, typography rhythm, and interactive states.
- Keep DOM and behavior stable.

2. Semi-refactor layout
- Rebuild section structure for stronger redesign.
- Higher visual gain but larger regression risk.

3. Global theme override
- Fast broad changes, but high coupling and side effects.

## Design
### 1) Style Strategy
- Keep component structure and events unchanged.
- Standardize compact visual rhythm with shared spacing tokens inside each component.
- Reduce decorative noise, increase alignment clarity.

### 2) DataConfig.vue Updates
- Container rhythm:
  - Reduce `.data-config` padding.
  - Reduce `.el-form-item` margin-bottom.
  - Reduce `.el-divider` vertical spacing.
  - Reduce `.field-config-layout` gap.
- Field area:
  - Slightly tighten `.field-management-area` width and padding feel.
  - Reduce `.drop-zone` min-height and padding.
  - Simplify hover/over state with border emphasis instead of heavy background.
  - Reduce `.drop-placeholder` icon/text size and spacing.
  - Tighten `.selected-field` row padding and line-height while keeping clickability.
- Filter/action area:
  - Tighten `.filter-item` spacing and control alignment.
  - Keep button usability and visual separation.

### 3) FieldManagementPanel Updates
- Tighten header/search/list spacing.
- Reduce draggable field item height/padding and unify text/icon spacing.
- Reduce section separation between normal/calculated fields.
- Preserve hover and drag feedback with lower visual noise.

## Non-Goals
- No logic/API/event changes.
- No text/content rewrite.
- No cross-page global theme migration.

## Validation
1. DataConfig and FieldManagementPanel show a consistent compact rhythm.
2. One-screen information density improves visibly.
3. All current interactions remain intact:
- datasource/dataset selection
- field drag-drop
- filter add/remove
- calculated field dialog save
- preview/refresh actions
4. Responsive behavior remains usable on narrower widths.
5. Frontend build succeeds.

## Risks & Mitigation
- Risk: Over-compression hurts readability.
  - Mitigation: Medium compact only, keep default small control size.
- Risk: Style changes accidentally impact nested ElementUI parts.
  - Mitigation: Keep scoped selectors local and avoid broad global overrides.
