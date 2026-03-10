# Remove Dataset Row Permission Integration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Revert dataset row-level permission UI and backend integration to current branch `HEAD` baseline without touching unrelated dirty changes.

**Architecture:** Use git-based surgical rollback only on a strict file whitelist. First identify exact impacted files, then restore tracked files and delete untracked row-permission files. Validate by grepping for removed entry points and checking focused diffs.

**Tech Stack:** Git, PowerShell, Vue 2 (Element UI), Spring Boot + MyBatis

---

### Task 1: Build Exact Rollback File List

**Files:**
- Inspect: `ui/src/views/bi/dataset/index.vue`
- Inspect: `ui/src/api/bi/dataset.js`
- Inspect: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QuerySqlParamBuilder.java`
- Inspect: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/DatasetPermissionConfigController.java`
- Inspect: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/DatasetPermissionConfig.java`
- Inspect: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/mapper/DatasetPermissionConfigMapper.java`
- Inspect: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/IDatasetPermissionConfigService.java`
- Inspect: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DatasetPermissionConfigServiceImpl.java`
- Inspect: `iras-bi/src/main/resources/mapper/platform/DatasetPermissionConfigMapper.xml`
- Inspect: `sql/2026-03-10-bi-dataset-permission-config.sql`

**Step 1: Capture current status for only relevant files**

```bash
git status --short -- ui/src/views/bi/dataset/index.vue ui/src/api/bi/dataset.js iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QuerySqlParamBuilder.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/DatasetPermissionConfigController.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/DatasetPermissionConfig.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/mapper/DatasetPermissionConfigMapper.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/IDatasetPermissionConfigService.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DatasetPermissionConfigServiceImpl.java iras-bi/src/main/resources/mapper/platform/DatasetPermissionConfigMapper.xml sql/2026-03-10-bi-dataset-permission-config.sql
```

**Step 2: Verify all row-permission entry points exist before rollback**

```bash
rg -n "getDatasetPermissionConfig|saveDatasetPermissionConfig|DatasetPermissionConfig|rowPermission|buildRowPermissionCondition|/bi/dataset/permission" ui/src iras-bi/src sql
```

**Step 3: Freeze whitelist in notes**
- Keep only files that are explicitly row-permission related.
- Do not include unrelated dirty files.

### Task 2: Revert Tracked Files to HEAD

**Files:**
- Modify (restore): `ui/src/views/bi/dataset/index.vue`
- Modify (restore): `ui/src/api/bi/dataset.js`
- Modify (restore): `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QuerySqlParamBuilder.java`

**Step 1: Restore tracked files**

```bash
git restore -- ui/src/views/bi/dataset/index.vue ui/src/api/bi/dataset.js iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QuerySqlParamBuilder.java
```

**Step 2: Verify these files are clean vs HEAD**

```bash
git diff -- ui/src/views/bi/dataset/index.vue ui/src/api/bi/dataset.js iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QuerySqlParamBuilder.java
```

Expected: no diff output for these files.

### Task 3: Remove Untracked Row-Permission Files

**Files:**
- Delete: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/DatasetPermissionConfigController.java`
- Delete: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/DatasetPermissionConfig.java`
- Delete: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/mapper/DatasetPermissionConfigMapper.java`
- Delete: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/IDatasetPermissionConfigService.java`
- Delete: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DatasetPermissionConfigServiceImpl.java`
- Delete: `iras-bi/src/main/resources/mapper/platform/DatasetPermissionConfigMapper.xml`
- Delete: `sql/2026-03-10-bi-dataset-permission-config.sql`

**Step 1: Remove only the listed untracked files**

```bash
rm iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/DatasetPermissionConfigController.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/DatasetPermissionConfig.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/mapper/DatasetPermissionConfigMapper.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/IDatasetPermissionConfigService.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DatasetPermissionConfigServiceImpl.java iras-bi/src/main/resources/mapper/platform/DatasetPermissionConfigMapper.xml sql/2026-03-10-bi-dataset-permission-config.sql
```

**Step 2: Confirm no untracked row-permission files remain**

```bash
git status --short | rg "DatasetPermissionConfig|dataset-permission-config|/bi/dataset/permission"
```

Expected: no matches.

### Task 4: Focused Regression Verification

**Files:**
- Verify: `ui/src/views/bi/dataset/index.vue`
- Verify: `ui/src/api/bi/dataset.js`
- Verify: `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QuerySqlParamBuilder.java`

**Step 1: Verify frontend no longer contains permission UI/API usage**

```bash
rg -n "数据集行级权限配置|getDatasetPermissionConfig|saveDatasetPermissionConfig|permissionConfig" ui/src/views/bi/dataset/index.vue ui/src/api/bi/dataset.js
```

Expected: no matches.

**Step 2: Verify backend no longer contains permission config references**

```bash
rg -n "DatasetPermissionConfig|datasetPermissionConfigMapper|buildRowPermissionCondition|/bi/dataset/permission" iras-bi/src/main/java iras-bi/src/main/resources
```

Expected: no matches for newly introduced row-permission integration.

**Step 3: Check final focused diff**

```bash
git diff -- ui/src/views/bi/dataset/index.vue ui/src/api/bi/dataset.js iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QuerySqlParamBuilder.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/controller/DatasetPermissionConfigController.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/domain/DatasetPermissionConfig.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/mapper/DatasetPermissionConfigMapper.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/IDatasetPermissionConfigService.java iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DatasetPermissionConfigServiceImpl.java iras-bi/src/main/resources/mapper/platform/DatasetPermissionConfigMapper.xml sql/2026-03-10-bi-dataset-permission-config.sql
```

Expected: no diff.

### Task 5: Commit (Optional, only if requested)

**Files:**
- Stage exactly the rollback file set above.

**Step 1: Stage rollback files only**

```bash
git add ui/src/views/bi/dataset/index.vue ui/src/api/bi/dataset.js iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QuerySqlParamBuilder.java
```

**Step 2: Stage deletions if they were tracked (or skip if untracked)**

```bash
git add -u iras-bi/src/main/java/com/zjrcu/iras/bi/platform iras-bi/src/main/resources/mapper/platform sql
```

**Step 3: Commit**

```bash
git commit -m "revert: remove dataset row-level permission integration"
```