# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IRAS (智能监管分析系统) is an enterprise-grade full-stack Business Intelligence and data quality monitoring platform built on RuoYi framework. It provides real-time supervision analytics with AI-powered insights for financial and regulatory monitoring.

- **Backend**: Spring Boot 3.5.4, Java 17, Maven multi-module architecture
- **Frontend**: Vue.js 2.6.12 SPA with Element UI, ECharts 5.4.0
- **Database**: MySQL 8.2.0 with Druid connection pool, Redis caching
- **AI Integration**: Tongyi Qwen API for analytics

## Build and Development Commands

### Backend (Java/Maven)
```bash
# From project root
mvn clean compile              # Compile all modules
mvn clean package              # Package for deployment
mvn clean package -DskipTests  # Build without running tests
mvn clean install              # Install to local Maven repo
mvn test -pl iras-system       # Run tests for specific module
mvn test -Dtest=ClassName#methodName  # Run single test method

# Run the application (default port: 8080)
java -jar iras-admin/target/iras-admin.jar
# Or run via Maven: mvn spring-boot:run -pl iras-admin
```

### Frontend (Vue.js - in ui/ directory)
```bash
cd ui
npm install                   # Install dependencies (use npm, not cnpm)
npm install --registry=https://registry.npmmirror.com  # If download is slow
npm run dev                   # Development server on port 80
npm run build:prod            # Production build
npm run build:stage           # Staging environment build
npm run preview               # Preview production build locally
```

## High-Level Architecture

### Backend Module Structure
Multi-module Maven project with clear separation of concerns:

```
iras-admin/          # Web layer - REST controllers, application.yml, main entry point
iras-framework/      # Cross-cutting concerns - security, config, interceptors, filters
iras-system/         # System management - users, roles, depts, menus, dictionaries
iras-common/         # Shared utilities - base entities, annotations, constants, utils
iras-bi/             # Business Intelligence modules
  ├── platform/      # BI platform core - datasources, datasets, dashboards, visualizations
  ├── conversionquality/    # Data conversion quality monitoring
  ├── fieldquality/        # Field-level data quality checks
  ├── indicatorquality/    # Indicator quality metrics
  └── globaloverview/      # Global overview dashboards with AI integration
```

**Entry Point**: `iras-admin/src/main/java/com/zjrcu/iras/IrasApplication.java`

**Key Architectural Patterns**:
- **Multi-datasource architecture**: Dynamic datasource switching for MySQL, PostgreSQL, ClickHouse, Doris
- **Dataset modes**: Direct query (real-time) vs Extract mode (cached data)
- **Dashboard Designer**: Drag-and-drop visualization builder using Vue Grid Layout
- **Permission system**: Method-level `@PreAuthorize` with role-based access control
- **Audit logging**: Comprehensive operation tracking via `@Log` annotation

### Frontend Structure
```
ui/src/
├── api/                # Axios service layer - one file per backend controller
├── components/         # Reusable components
│   ├── BiDashboardDesigner/    # Drag-and-drop dashboard builder (vue-grid-layout)
│   ├── BiVisualizationConfig/  # Chart configuration dialogs
│   └── BiChartLibrary/         # ECharts wrapper components
├── views/              # Page-level components (organized by module)
├── router/             # Vue Router - dynamic routes based on user permissions
├── store/              # Vuex state management
└── utils/              # Request interceptor, auth, permissions
```

**State Management**: Vuex store at `ui/src/store/` with modules for user, permissions, settings

**Dynamic Routing**: Routes are loaded from backend based on user permissions - see `ui/src/router/index.js`

### Database Schema

**Core Tables**:
- `sys_user`, `sys_role`, `sys_menu`, `sys_dept` - User management (RuoYi standard)
- `bi_datasource` - Multi-type database connection configurations
- `bi_dataset` - Dataset definitions (SQL, API, file-based)
- `bi_dashboard` - Dashboard layouts and component configurations
- `bi_visualization` - Chart type, dimensions, metrics, styles
- `bi_extract_data` - Cached dataset snapshots (extract mode)
- `bi_audit_log` - Complete audit trail for all BI operations

**Multi-Database Support**: System supports connecting to external databases via `bi_datasource` configuration

### Key Configuration Files

- `iras-admin/src/main/resources/application.yml` - Main config (server, logging, Redis, token)
- `iras-admin/src/main/resources/application-druid.yml` - Database connection pool config
- `ui/vue.config.js` - Frontend build configuration
- `ui/.env.development`, `.env.production` - Environment-specific variables

## BI-Specific Architecture

### Dashboard Designer System
The dashboard designer allows users to create interactive dashboards:

1. **Data Source Layer**: Connect to databases (MySQL, PostgreSQL, ClickHouse, Doris)
2. **Dataset Layer**: Define SQL queries or API endpoints, with query conditions
3. **Visualization Layer**: Configure ECharts (line, bar, pie, scatter, map, etc.)
4. **Dashboard Layer**: Drag-and-drop components using Vue Grid Layout
5. **Share Layer**: Generate public links with optional access controls

**Frontend Components**:
- `BiDashboardDesigner`: Main editor with grid layout, component palette, property panel
- `BiVisualizationConfig`: Chart configuration form (data binding, styling)
- `BiChartLibrary`: Wrapper components for each ECharts type

**Backend Controllers** (in `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/`):
- `DataSourceController`: CRUD for database connections
- `DatasetController`: Manage datasets, test queries, preview data
- `VisualizationController`: Create/update chart configurations
- `DashboardController`: Dashboard CRUD, publish/unpublish
- `ComponentController`: Link visualizations to dashboard positions
- `ShareLinkController`: Generate access tokens and permissions

### Data Quality Monitoring

**Modules in `iras-bi/src/main/java/com/zjrcu/iras/bi/`**:

1. **conversionquality/**: Monitors data transformation quality between systems
2. **fieldquality/**: Validates field-level data completeness, accuracy, consistency
3. **indicatorquality/**: Tracks business indicator metrics and thresholds
4. **globaloverview/**: Aggregated dashboards with AI-powered insights

Each module follows the standard pattern: `controller/` → `service/` → `mapper/` → `domain/`

### AI Integration

**Tongyi Qwen Integration** (`globaloverview/controller/AiController.java`):
- API key configured in `application.yml`: `ai.tongyi.api-key`
- Model: `qwen3-max`
- Used for generating insights from monitoring data

## API Documentation

**Swagger UI**: Available at `http://localhost:8080/swagger-ui.html` when running backend

**API Groups** (configured in `application.yml`):
- conversion - 转换质量模块
- field - 字段质量模块
- indicator - 指标质量模块
- overview - 全局模块 (includes AI endpoints)

## Development Workflow

### Adding a New Feature

**Backend**:
1. Create domain entity in appropriate module's `domain/` package (extends `BaseEntity`)
2. Create MyBatis mapper interface in `mapper/` with `@Mapper` annotation
3. Create mapper XML in `src/main/resources/mapper/`
4. Create service interface in `service/`
5. Create service implementation in `service/impl/` with `@Service`
6. Create controller in `controller/` with `@RestController`, `@RequestMapping`
7. Add permissions via `@PreAuthorize("@ss.hasPermi('module:function:action')")`
8. Add logging via `@Log(title = "操作名", businessType = BusinessType.INSERT)`

**Frontend**:
1. Create API service in `ui/src/api/module/` (uses axios request wrapper)
2. Create Vue component in `ui/src/views/module/`
3. Add route to dynamic routes in `ui/src/router/index.js` (or backend menu system)
4. Use Element UI components and global components (Pagination, RightToolbar, etc.)
5. Add permission checks with `v-hasPermi="['module:function:action']"`

### Database Changes

SQL scripts are typically located in project root or `sql/` directory. The system uses MyBatis with XML mappers located in `src/main/resources/mapper/`.

## Important Notes

- **Jakarta EE**: Uses `jakarta.*` namespace (not `javax.*`) - Spring Boot 3.x requirement
- **Chinese Language**: UI text and code comments are in Chinese
- **RuoYi Conventions**: Follow RuoYi framework patterns for consistency
- **Security**: JWT-based authentication, Spring Security for authorization
- **Caching**: Redis used for session storage and performance
- **File Upload**: Configured in `application.yml` (max 10MB per file, 20MB total)
- **CORS**: Configure in backend for cross-origin requests during development
- **Multi-tenancy**: Department-based data isolation via `sys_dept`
