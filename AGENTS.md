# AGENTS.md

This file provides guidance to Qoder (qoder.com) when working with code in this repository.

## Project Overview
IRAS (智能监管分析系统) is an enterprise-grade BI and data quality monitoring platform built on the RuoYi framework.
- **Backend**: Spring Boot 3.5.4, Java 17, Maven multi-module, MyBatis, Spring Security, Jakarta EE.
- **Frontend**: Vue.js 2.6.12 (Options API), Element UI, ECharts 5.4.0.
- **AI Integration**: Tongyi Qwen (qwen3-max) for data insights.

## Build & Test Commands

### Backend (Java/Maven)
```bash
mvn clean compile              # Compile all modules
mvn clean package -DskipTests  # Build without running tests
mvn test -pl <module-name>     # Run tests for a specific module (e.g., iras-system)
mvn test -Dtest=ClassName#methodName # Run a single test method
```

### Frontend (Vue.js - in `ui/` directory)
```bash
cd ui
npm install --registry=https://registry.npmmirror.com # Recommended
npm run dev        # Development server (port 80)
npm run build:prod # Production build
```

## High-Level Architecture

### Module Structure
- `iras-admin`: Web layer, REST controllers, `application.yml`, and the `IrasApplication` entry point.
- `iras-framework`: Security (JWT), configurations, Druid pool, and core filters/interceptors.
- `iras-system`: System management (Users, Roles, Depts, Menus, Dictionaries).
- `iras-common`: Base entities, annotations, constants, and global utilities.
- `iras-bi`: Core business modules:
    - `platform/`: BI core (Datasources, Datasets, Dashboards, Visualizations).
    - `conversionquality/`, `fieldquality/`, `indicatorquality/`: Data quality monitoring logic.
    - `globaloverview/`: Aggregated dashboards and AI endpoints (`AiController.java`).

### BI Core Flow
1. **Datasource**: Multi-database support (MySQL, PostgreSQL, ClickHouse, Doris).
2. **Dataset**: SQL or API based. Supports "Direct Query" and "Extract Mode" (cached snapshot).
3. **Visualization**: ECharts configurations bound to datasets.
4. **Dashboard**: Drag-and-drop layout using `vue-grid-layout`.

## Development Patterns

### Backend (Jakarta EE & RuoYi)
- **Jakarta Namespace**: Use `jakarta.*` instead of `javax.*`.
- **Controller Pattern**:
    - `@PreAuthorize("@ss.hasPermi('module:function:action')")` for permissions.
    - `@Log(title = "...", businessType = BusinessType.INSERT)` for audit trails.
    - Return `AjaxResult` for standard responses or `TableDataInfo` for paginated lists.
- **Data Access**: MyBatis XML mappers are in `src/main/resources/mapper/`.
- **Entity**: Most domain entities should extend `BaseEntity`.

### Frontend
- **API Services**: Defined in `ui/src/api/` matching the backend controller structure.
- **Permissions**: Use `v-hasPermi="['permission:code']"` directive.
- **Dictionaries**: Load via `dicts: ['dict_type']` in component options.
- **Components**: Reusable components like `Pagination`, `RightToolbar`, and `DictTag` are global.

## Development Workflow: Adding Features
1. **Backend**: Entity (`domain`) -> Mapper Interface & XML -> Service Interface & `impl` -> Controller.
2. **Frontend**: API service -> Vue view component -> Add to RuoYi menu system (DB/UI).
3. **Permissions**: Add permission string to `sys_menu` table and use in `@PreAuthorize`/`v-hasPermi`.
