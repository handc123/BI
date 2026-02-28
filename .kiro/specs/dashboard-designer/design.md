# 设计文档 - 仪表板设计器

## 概述

仪表板设计器是一个基于Vue 2.6和ECharts 5.4的可视化拖拽式仪表板构建系统。它采用"三段式+三列"布局,提供所见即所得的编辑体验,支持图表与查询条件解耦、仪表板级统一控制和低代码/零SQL的设计理念。

### 核心设计原则

1. **所见即所得(WYSIWYG)**: 编辑界面与最终展示效果一致
2. **图表与查询条件解耦**: 图表定义"计算什么、如何分组",查询组件定义"过滤条件"
3. **仪表板级统一控制**: 查询条件在仪表板级别统一管理,控制多个图表
4. **低代码/零SQL**: 通过可视化配置完成仪表板构建,无需编写SQL
5. **支持复杂业务分析**: 支持下钻、联动、级联等高级分析场景

### 技术栈

- **前端**: Vue 2.6.12, Element UI 2.15.14, ECharts 5.4.0, Vuex, Vue Router
- **后端**: Spring Boot 3.5.4, Java 17, MyBatis, Spring Security
- **拖拽**: 原生HTML5 Drag & Drop API + Vue自定义指令
- **布局**: CSS Grid + Flexbox,网格吸附算法
- **状态管理**: Vuex存储画布状态、组件配置、撤销/重做栈

## 架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      仪表板设计器前端                          │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  顶部工具栏   │  │  画布编辑区   │  │  右侧配置面板 │      │
│  │              │  │              │  │              │      │
│  │ - 返回/撤销  │  │ - 组件拖拽   │  │ - 数据配置   │      │
│  │ - 组件库     │  │ - 网格吸附   │  │ - 样式配置   │      │
│  │ - 预览/保存  │  │ - 选择/操作  │  │ - 高级配置   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Vuex 状态管理                            │  │
│  │  - dashboard (仪表板配置)                            │  │
│  │  - components (组件列表)                             │  │
│  │  - history (撤销/重做栈)                             │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↕ REST API
┌─────────────────────────────────────────────────────────────┐
│                      后端服务层                               │
├─────────────────────────────────────────────────────────────┤
│  DashboardController → DashboardService → DashboardMapper   │
│  ComponentController → ComponentService → ComponentMapper   │
│  QueryController → QueryService → QueryMapper               │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                      数据库层                                 │
│  - bi_dashboard (仪表板表)                                   │
│  - bi_dashboard_component (组件表)                          │
│  - bi_query_condition (查询条件表)                          │
│  - bi_condition_mapping (条件映射表)                        │
└─────────────────────────────────────────────────────────────┘
```

### 分层架构

#### 前端层次

1. **视图层 (Views)**
   - `DashboardDesigner.vue`: 主设计器页面
   - `DashboardPreview.vue`: 预览页面
   - `DashboardList.vue`: 仪表板列表页面

2. **组件层 (Components)**
   - `DesignerToolbar.vue`: 顶部工具栏
   - `DesignerCanvas.vue`: 画布编辑区
   - `ConfigPanel.vue`: 右侧配置面板
   - `ComponentLibrary.vue`: 组件库弹窗
   - `QueryConditionConfig.vue`: 查询条件配置弹窗
   - `ChartWidget.vue`: 图表组件包装器
   - `QueryWidget.vue`: 查询组件包装器

3. **状态管理层 (Vuex Store)**
   - `dashboard.js`: 仪表板状态模块
   - `components.js`: 组件状态模块
   - `history.js`: 历史记录模块

4. **服务层 (API Services)**
   - `dashboard.js`: 仪表板API
   - `component.js`: 组件API
   - `query.js`: 查询API

#### 后端层次

1. **控制器层 (Controllers)**
   - `DashboardController`: 仪表板CRUD操作
   - `ComponentController`: 组件CRUD操作
   - `QueryConditionController`: 查询条件管理

2. **服务层 (Services)**
   - `IDashboardService`: 仪表板业务逻辑
   - `IComponentService`: 组件业务逻辑
   - `IQueryConditionService`: 查询条件业务逻辑

3. **数据访问层 (Mappers)**
   - `DashboardMapper`: 仪表板数据访问
   - `ComponentMapper`: 组件数据访问
   - `QueryConditionMapper`: 查询条件数据访问

## 组件和接口

### 前端核心组件

#### 1. DashboardDesigner (主设计器)

**职责**: 协调工具栏、画布和配置面板的交互

**Props**: 
- `dashboardId`: String - 仪表板ID(编辑模式)

**Data**:
```javascript
{
  currentDashboard: Object,      // 当前仪表板配置
  selectedComponent: Object,     // 当前选中的组件
  isPreviewMode: Boolean,        // 是否预览模式
  hasUnsavedChanges: Boolean     // 是否有未保存的更改
}
```

**Methods**:
- `loadDashboard(id)`: 加载仪表板
- `saveDashboard()`: 保存仪表板
- `publishDashboard()`: 发布仪表板
- `enterPreviewMode()`: 进入预览模式
- `exitPreviewMode()`: 退出预览模式
- `handleComponentSelect(component)`: 处理组件选择

#### 2. DesignerCanvas (画布编辑区)

**职责**: 管理组件的拖拽、放置、调整大小和选择

**Props**:
- `components`: Array - 组件列表
- `gridSize`: Number - 网格大小(默认10px)
- `canvasConfig`: Object - 画布配置(背景、边距等)

**Data**:
```javascript
{
  draggedComponent: Object,      // 正在拖拽的组件
  resizingComponent: Object,     // 正在调整大小的组件
  selectionBox: Object,          // 多选框
  alignmentGuides: Array         // 对齐参考线
}
```

**Methods**:
- `handleDrop(event)`: 处理组件拖放
- `handleDragOver(event)`: 处理拖拽悬停
- `snapToGrid(x, y)`: 吸附到网格
- `handleComponentResize(component, newSize)`: 处理组件调整大小
- `showAlignmentGuides(component)`: 显示对齐参考线
- `selectComponent(component)`: 选择组件
- `deleteComponent(component)`: 删除组件
- `copyComponent(component)`: 复制组件

**Events**:
- `@component-select`: 组件被选中
- `@component-update`: 组件位置/大小更新
- `@component-delete`: 组件被删除

#### 3. ConfigPanel (配置面板)

**职责**: 显示和编辑选中组件或仪表板的配置

**Props**:
- `target`: Object - 配置目标(组件或仪表板)
- `targetType`: String - 'component' | 'dashboard'

**Data**:
```javascript
{
  activeTab: String,             // 当前激活的标签页
  dataConfig: Object,            // 数据配置
  styleConfig: Object,           // 样式配置
  advancedConfig: Object         // 高级配置
}
```

**Methods**:
- `updateDataConfig(config)`: 更新数据配置
- `updateStyleConfig(config)`: 更新样式配置
- `updateAdvancedConfig(config)`: 更新高级配置
- `validateConfig()`: 验证配置完整性

**Events**:
- `@config-change`: 配置发生变化

#### 4. QueryConditionConfig (查询条件配置)

**职责**: 配置查询条件及其与图表的映射关系

**Props**:
- `conditions`: Array - 查询条件列表
- `components`: Array - 图表组件列表

**Data**:
```javascript
{
  selectedCondition: Object,     // 当前选中的条件
  conditionMappings: Array,      // 条件映射关系
  cascadeRules: Array            // 级联规则
}
```

**Methods**:
- `addCondition()`: 添加查询条件
- `updateCondition(condition)`: 更新查询条件
- `deleteCondition(conditionId)`: 删除查询条件
- `mapConditionToChart(conditionId, chartId, fieldName)`: 映射条件到图表
- `updateCascadeRule(rule)`: 更新级联规则

#### 5. ChartWidget (图表组件包装器)

**职责**: 包装ECharts图表,处理数据绑定和刷新

**Props**:
- `config`: Object - 图表配置
- `queryParams`: Object - 查询参数(来自查询组件)
- `isEditMode`: Boolean - 是否编辑模式

**Data**:
```javascript
{
  chartInstance: Object,         // ECharts实例
  chartData: Array,              // 图表数据
  loading: Boolean,              // 加载状态
  error: String                  // 错误信息
}
```

**Methods**:
- `initChart()`: 初始化图表
- `fetchData(params)`: 获取数据
- `refreshChart()`: 刷新图表
- `updateChartOption(option)`: 更新图表配置
- `handleDrillDown(params)`: 处理下钻

**Computed**:
- `mergedQueryParams`: 合并图表级过滤器和仪表板级查询参数

### 后端核心接口

#### 1. DashboardController

**基础路径**: `/bi/dashboard`

**接口列表**:

```java
// 获取仪表板列表
@GetMapping("/list")
@PreAuthorize("@ss.hasPermi('bi:dashboard:list')")
public TableDataInfo list(Dashboard dashboard)

// 获取仪表板详情
@GetMapping("/{id}")
@PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
public AjaxResult getInfo(@PathVariable Long id)

// 创建仪表板
@PostMapping
@PreAuthorize("@ss.hasPermi('bi:dashboard:add')")
@Log(title = "仪表板", businessType = BusinessType.INSERT)
public AjaxResult add(@Validated @RequestBody Dashboard dashboard)

// 更新仪表板
@PutMapping
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
@Log(title = "仪表板", businessType = BusinessType.UPDATE)
public AjaxResult edit(@Validated @RequestBody Dashboard dashboard)

// 删除仪表板
@DeleteMapping("/{ids}")
@PreAuthorize("@ss.hasPermi('bi:dashboard:remove')")
@Log(title = "仪表板", businessType = BusinessType.DELETE)
public AjaxResult remove(@PathVariable Long[] ids)

// 发布仪表板
@PostMapping("/{id}/publish")
@PreAuthorize("@ss.hasPermi('bi:dashboard:publish')")
@Log(title = "仪表板发布", businessType = BusinessType.UPDATE)
public AjaxResult publish(@PathVariable Long id)

// 获取仪表板配置(包含所有组件)
@GetMapping("/{id}/config")
@PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
public AjaxResult getConfig(@PathVariable Long id)

// 保存仪表板配置
@PostMapping("/{id}/config")
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
@Log(title = "仪表板配置", businessType = BusinessType.UPDATE)
public AjaxResult saveConfig(@PathVariable Long id, @RequestBody DashboardConfig config)
```

#### 2. ComponentController

**基础路径**: `/bi/dashboard/component`

**接口列表**:

```java
// 获取组件列表
@GetMapping("/list")
@PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
public TableDataInfo list(DashboardComponent component)

// 获取组件详情
@GetMapping("/{id}")
@PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
public AjaxResult getInfo(@PathVariable Long id)

// 添加组件
@PostMapping
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
@Log(title = "仪表板组件", businessType = BusinessType.INSERT)
public AjaxResult add(@Validated @RequestBody DashboardComponent component)

// 更新组件
@PutMapping
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
@Log(title = "仪表板组件", businessType = BusinessType.UPDATE)
public AjaxResult edit(@Validated @RequestBody DashboardComponent component)

// 删除组件
@DeleteMapping("/{ids}")
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
@Log(title = "仪表板组件", businessType = BusinessType.DELETE)
public AjaxResult remove(@PathVariable Long[] ids)

// 批量更新组件位置
@PostMapping("/batch-position")
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
public AjaxResult batchUpdatePosition(@RequestBody List<ComponentPosition> positions)

// 复制组件
@PostMapping("/{id}/copy")
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
public AjaxResult copy(@PathVariable Long id)

// 保存为模板
@PostMapping("/{id}/template")
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
public AjaxResult saveAsTemplate(@PathVariable Long id, @RequestBody TemplateInfo info)
```

#### 3. QueryConditionController

**基础路径**: `/bi/dashboard/query`

**接口列表**:

```java
// 获取查询条件列表
@GetMapping("/list")
@PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
public TableDataInfo list(QueryCondition condition)

// 获取查询条件详情
@GetMapping("/{id}")
@PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
public AjaxResult getInfo(@PathVariable Long id)

// 添加查询条件
@PostMapping
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
public AjaxResult add(@Validated @RequestBody QueryCondition condition)

// 更新查询条件
@PutMapping
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
public AjaxResult edit(@Validated @RequestBody QueryCondition condition)

// 删除查询条件
@DeleteMapping("/{ids}")
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
public AjaxResult remove(@PathVariable Long[] ids)

// 获取条件映射
@GetMapping("/{dashboardId}/mappings")
@PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
public AjaxResult getMappings(@PathVariable Long dashboardId)

// 保存条件映射
@PostMapping("/{dashboardId}/mappings")
@PreAuthorize("@ss.hasPermi('bi:dashboard:edit')")
public AjaxResult saveMappings(@PathVariable Long dashboardId, 
                               @RequestBody List<ConditionMapping> mappings)

// 获取级联选项
@GetMapping("/cascade-options")
@PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
public AjaxResult getCascadeOptions(@RequestParam Long conditionId, 
                                   @RequestParam Map<String, Object> parentValues)
```

## 数据模型

### 数据库表设计

#### 1. bi_dashboard (仪表板表)

```sql
CREATE TABLE bi_dashboard (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仪表板ID',
  dashboard_name VARCHAR(100) NOT NULL COMMENT '仪表板名称',
  dashboard_desc VARCHAR(500) COMMENT '仪表板描述',
  theme VARCHAR(20) DEFAULT 'light' COMMENT '主题(light/dark)',
  canvas_config TEXT COMMENT '画布配置(JSON)',
  global_style TEXT COMMENT '全局样式配置(JSON)',
  status CHAR(1) DEFAULT '0' COMMENT '状态(0草稿 1已发布)',
  published_version INT DEFAULT 0 COMMENT '已发布版本号',
  create_by VARCHAR(64) COMMENT '创建者',
  create_time DATETIME COMMENT '创建时间',
  update_by VARCHAR(64) COMMENT '更新者',
  update_time DATETIME COMMENT '更新时间',
  remark VARCHAR(500) COMMENT '备注',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志(0正常 1删除)'
) COMMENT='仪表板表';
```

#### 2. bi_dashboard_component (组件表)

```sql
CREATE TABLE bi_dashboard_component (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '组件ID',
  dashboard_id BIGINT NOT NULL COMMENT '仪表板ID',
  component_type VARCHAR(50) NOT NULL COMMENT '组件类型(chart/query/text/media/tabs)',
  component_name VARCHAR(100) COMMENT '组件名称',
  position_x INT NOT NULL COMMENT 'X坐标',
  position_y INT NOT NULL COMMENT 'Y坐标',
  width INT NOT NULL COMMENT '宽度',
  height INT NOT NULL COMMENT '高度',
  z_index INT DEFAULT 0 COMMENT '层级',
  data_config TEXT COMMENT '数据配置(JSON)',
  style_config TEXT COMMENT '样式配置(JSON)',
  advanced_config TEXT COMMENT '高级配置(JSON)',
  create_time DATETIME COMMENT '创建时间',
  update_time DATETIME COMMENT '更新时间',
  FOREIGN KEY (dashboard_id) REFERENCES bi_dashboard(id) ON DELETE CASCADE
) COMMENT='仪表板组件表';
```

#### 3. bi_query_condition (查询条件表)

```sql
CREATE TABLE bi_query_condition (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '条件ID',
  dashboard_id BIGINT NOT NULL COMMENT '仪表板ID',
  condition_name VARCHAR(100) NOT NULL COMMENT '条件名称',
  condition_type VARCHAR(50) NOT NULL COMMENT '条件类型(time/dropdown/select/range)',
  display_order INT DEFAULT 0 COMMENT '显示顺序',
  is_required CHAR(1) DEFAULT '0' COMMENT '是否必填(0否 1是)',
  is_visible CHAR(1) DEFAULT '1' COMMENT '是否显示(0否 1是)',
  default_value VARCHAR(500) COMMENT '默认值',
  config TEXT COMMENT '条件配置(JSON:时间粒度/选项列表/范围限制等)',
  parent_condition_id BIGINT COMMENT '父条件ID(级联)',
  create_time DATETIME COMMENT '创建时间',
  update_time DATETIME COMMENT '更新时间',
  FOREIGN KEY (dashboard_id) REFERENCES bi_dashboard(id) ON DELETE CASCADE,
  FOREIGN KEY (parent_condition_id) REFERENCES bi_query_condition(id) ON DELETE SET NULL
) COMMENT='查询条件表';
```

#### 4. bi_condition_mapping (条件映射表)

```sql
CREATE TABLE bi_condition_mapping (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '映射ID',
  condition_id BIGINT NOT NULL COMMENT '条件ID',
  component_id BIGINT NOT NULL COMMENT '组件ID',
  field_name VARCHAR(100) NOT NULL COMMENT '映射字段名',
  mapping_type VARCHAR(20) DEFAULT 'auto' COMMENT '映射类型(auto/custom)',
  create_time DATETIME COMMENT '创建时间',
  FOREIGN KEY (condition_id) REFERENCES bi_query_condition(id) ON DELETE CASCADE,
  FOREIGN KEY (component_id) REFERENCES bi_dashboard_component(id) ON DELETE CASCADE,
  UNIQUE KEY uk_condition_component (condition_id, component_id, field_name)
) COMMENT='条件映射表';
```

#### 5. bi_component_template (组件模板表)

```sql
CREATE TABLE bi_component_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  template_desc VARCHAR(500) COMMENT '模板描述',
  component_type VARCHAR(50) NOT NULL COMMENT '组件类型',
  template_config TEXT NOT NULL COMMENT '模板配置(JSON)',
  create_by VARCHAR(64) COMMENT '创建者',
  create_time DATETIME COMMENT '创建时间',
  update_time DATETIME COMMENT '更新时间'
) COMMENT='组件模板表';
```

### 领域对象

#### 1. Dashboard (仪表板实体)

```java
package com.zjrcu.iras.bi.platform.domain;

import com.zjrcu.iras.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

public class Dashboard extends BaseEntity {
    private Long id;
    private String dashboardName;
    private String dashboardDesc;
    private String theme;
    private String canvasConfig;      // JSON字符串
    private String globalStyle;       // JSON字符串
    private String status;            // 0草稿 1已发布
    private Integer publishedVersion;
    private String delFlag;
    
    // Getters and Setters
}
```

#### 2. DashboardComponent (组件实体)

```java
package com.zjrcu.iras.bi.platform.domain;

import com.zjrcu.iras.common.core.domain.BaseEntity;

public class DashboardComponent extends BaseEntity {
    private Long id;
    private Long dashboardId;
    private String componentType;     // chart/query/text/media/tabs
    private String componentName;
    private Integer positionX;
    private Integer positionY;
    private Integer width;
    private Integer height;
    private Integer zIndex;
    private String dataConfig;        // JSON字符串
    private String styleConfig;       // JSON字符串
    private String advancedConfig;    // JSON字符串
    
    // Getters and Setters
}
```

#### 3. QueryCondition (查询条件实体)

```java
package com.zjrcu.iras.bi.platform.domain;

import com.zjrcu.iras.common.core.domain.BaseEntity;

public class QueryCondition extends BaseEntity {
    private Long id;
    private Long dashboardId;
    private String conditionName;
    private String conditionType;     // time/dropdown/select/range
    private Integer displayOrder;
    private String isRequired;        // 0否 1是
    private String isVisible;         // 0否 1是
    private String defaultValue;
    private String config;            // JSON字符串
    private Long parentConditionId;   // 级联父条件
    
    // Getters and Setters
}
```

#### 4. ConditionMapping (条件映射实体)

```java
package com.zjrcu.iras.bi.platform.domain;

import com.zjrcu.iras.common.core.domain.BaseEntity;

public class ConditionMapping extends BaseEntity {
    private Long id;
    private Long conditionId;
    private Long componentId;
    private String fieldName;
    private String mappingType;       // auto/custom
    
    // Getters and Setters
}
```

#### 5. ComponentTemplate (组件模板实体)

```java
package com.zjrcu.iras.bi.platform.domain;

import com.zjrcu.iras.common.core.domain.BaseEntity;

public class ComponentTemplate extends BaseEntity {
    private Long id;
    private String templateName;
    private String templateDesc;
    private String componentType;
    private String templateConfig;    // JSON字符串
    
    // Getters and Setters
}
```

### DTO对象

#### 1. DashboardConfig (仪表板配置DTO)

```java
package com.zjrcu.iras.bi.platform.domain.dto;

import java.util.List;

public class DashboardConfig {
    private Dashboard dashboard;
    private List<DashboardComponent> components;
    private List<QueryCondition> queryConditions;
    private List<ConditionMapping> conditionMappings;
    
    // Getters and Setters
}
```

#### 2. ComponentPosition (组件位置DTO)

```java
package com.zjrcu.iras.bi.platform.domain.dto;

public class ComponentPosition {
    private Long componentId;
    private Integer positionX;
    private Integer positionY;
    private Integer width;
    private Integer height;
    private Integer zIndex;
    
    // Getters and Setters
}
```

#### 3. TemplateInfo (模板信息DTO)

```java
package com.zjrcu.iras.bi.platform.domain.dto;

public class TemplateInfo {
    private String templateName;
    private String templateDesc;
    
    // Getters and Setters
}
```

### 前端数据模型

#### 1. Canvas State (画布状态)

```javascript
// Vuex store: dashboard.js
const state = {
  currentDashboard: {
    id: null,
    dashboardName: '',
    theme: 'light',
    canvasConfig: {
      width: 1920,
      height: 1080,
      gridSize: 10,
      margin: { top: 20, right: 20, bottom: 20, left: 20 },
      background: { type: 'color', value: '#ffffff', opacity: 1 }
    },
    globalStyle: {
      colorScheme: ['#5470c6', '#91cc75', '#fac858'],
      titleStyle: { fontSize: 16, fontWeight: 'bold', color: '#333' },
      fontFamily: 'Microsoft YaHei'
    }
  },
  components: [],
  queryConditions: [],
  conditionMappings: [],
  selectedComponentId: null,
  isDirty: false
}
```

#### 2. Component Model (组件模型)

```javascript
// 图表组件示例
const chartComponent = {
  id: 'comp_001',
  type: 'chart',
  name: '销售趋势图',
  position: { x: 100, y: 100 },
  size: { width: 600, height: 400 },
  zIndex: 1,
  dataConfig: {
    datasetId: 'ds_001',
    dimensions: [
      { field: 'date', axis: 'x', label: '日期' }
    ],
    metrics: [
      { field: 'sales', aggregation: 'sum', label: '销售额' }
    ],
    filters: [],
    limit: 100,
    refreshInterval: 0  // 0表示不自动刷新
  },
  styleConfig: {
    chartType: 'line',
    colors: ['#5470c6'],
    legend: { show: true, position: 'top' },
    grid: { top: 60, right: 20, bottom: 60, left: 60 }
  },
  advancedConfig: {
    drillDown: { enabled: false, dimensions: [] },
    tooltip: { show: true }
  }
}

// 查询组件示例
const queryComponent = {
  id: 'comp_002',
  type: 'query',
  name: '查询条件',
  position: { x: 100, y: 20 },
  size: { width: 1720, height: 60 },
  zIndex: 999,
  styleConfig: {
    layout: 'horizontal',
    spacing: 10
  }
}
```

#### 3. Query Condition Model (查询条件模型)

```javascript
// 时间条件示例
const timeCondition = {
  id: 'cond_001',
  name: '时间范围',
  type: 'time',
  displayOrder: 1,
  isRequired: true,
  isVisible: true,
  defaultValue: 'last7days',
  config: {
    granularity: 'day',  // day/week/month/quarter/year
    rangeType: 'relative',  // relative/absolute
    relativeOptions: ['today', 'yesterday', 'last7days', 'last30days'],
    format: 'YYYY-MM-DD'
  }
}

// 下拉选择条件示例
const dropdownCondition = {
  id: 'cond_002',
  name: '机构',
  type: 'dropdown',
  displayOrder: 2,
  isRequired: false,
  isVisible: true,
  defaultValue: null,
  config: {
    multiple: false,
    options: [
      { label: '总行', value: '001' },
      { label: '分行A', value: '002' },
      { label: '分行B', value: '003' }
    ],
    dataSource: {  // 或从接口获取
      type: 'api',
      url: '/api/organizations',
      labelField: 'name',
      valueField: 'code'
    }
  },
  parentConditionId: null
}

// 级联条件示例
const cascadeCondition = {
  id: 'cond_003',
  name: '支行',
  type: 'dropdown',
  displayOrder: 3,
  isRequired: false,
  isVisible: true,
  defaultValue: null,
  config: {
    multiple: false,
    dataSource: {
      type: 'api',
      url: '/api/branches',
      params: { parentId: '${cond_002}' },  // 依赖父条件
      labelField: 'name',
      valueField: 'code'
    }
  },
  parentConditionId: 'cond_002'
}
```

#### 4. Condition Mapping Model (条件映射模型)

```javascript
const conditionMapping = {
  id: 'map_001',
  conditionId: 'cond_001',  // 时间范围条件
  componentId: 'comp_001',  // 销售趋势图
  fieldName: 'date',        // 映射到数据集的date字段
  mappingType: 'auto'       // auto/custom
}
```

### 核心算法

#### 1. 网格吸附算法

```javascript
/**
 * 将坐标吸附到网格
 * @param {number} value - 原始坐标值
 * @param {number} gridSize - 网格大小
 * @returns {number} 吸附后的坐标值
 */
function snapToGrid(value, gridSize) {
  return Math.round(value / gridSize) * gridSize;
}

/**
 * 处理组件拖拽时的吸附
 * @param {Object} component - 组件对象
 * @param {number} deltaX - X轴移动距离
 * @param {number} deltaY - Y轴移动距离
 * @param {number} gridSize - 网格大小
 */
function handleDrag(component, deltaX, deltaY, gridSize) {
  const newX = component.position.x + deltaX;
  const newY = component.position.y + deltaY;
  
  component.position.x = snapToGrid(newX, gridSize);
  component.position.y = snapToGrid(newY, gridSize);
}
```

#### 2. 对齐参考线算法

```javascript
/**
 * 计算对齐参考线
 * @param {Object} movingComponent - 正在移动的组件
 * @param {Array} otherComponents - 其他组件列表
 * @param {number} threshold - 对齐阈值(像素)
 * @returns {Array} 参考线列表
 */
function calculateAlignmentGuides(movingComponent, otherComponents, threshold = 5) {
  const guides = [];
  const moving = {
    left: movingComponent.position.x,
    right: movingComponent.position.x + movingComponent.size.width,
    top: movingComponent.position.y,
    bottom: movingComponent.position.y + movingComponent.size.height,
    centerX: movingComponent.position.x + movingComponent.size.width / 2,
    centerY: movingComponent.position.y + movingComponent.size.height / 2
  };
  
  otherComponents.forEach(comp => {
    const other = {
      left: comp.position.x,
      right: comp.position.x + comp.size.width,
      top: comp.position.y,
      bottom: comp.position.y + comp.size.height,
      centerX: comp.position.x + comp.size.width / 2,
      centerY: comp.position.y + comp.size.height / 2
    };
    
    // 检查垂直对齐
    if (Math.abs(moving.left - other.left) < threshold) {
      guides.push({ type: 'vertical', position: other.left });
    }
    if (Math.abs(moving.right - other.right) < threshold) {
      guides.push({ type: 'vertical', position: other.right });
    }
    if (Math.abs(moving.centerX - other.centerX) < threshold) {
      guides.push({ type: 'vertical', position: other.centerX });
    }
    
    // 检查水平对齐
    if (Math.abs(moving.top - other.top) < threshold) {
      guides.push({ type: 'horizontal', position: other.top });
    }
    if (Math.abs(moving.bottom - other.bottom) < threshold) {
      guides.push({ type: 'horizontal', position: other.bottom });
    }
    if (Math.abs(moving.centerY - other.centerY) < threshold) {
      guides.push({ type: 'horizontal', position: other.centerY });
    }
  });
  
  return guides;
}
```

#### 3. 撤销/重做算法

```javascript
/**
 * 历史记录管理器
 */
class HistoryManager {
  constructor(maxSize = 50) {
    this.undoStack = [];
    this.redoStack = [];
    this.maxSize = maxSize;
  }
  
  /**
   * 记录状态快照
   * @param {Object} state - 当前状态
   */
  push(state) {
    // 深拷贝状态
    const snapshot = JSON.parse(JSON.stringify(state));
    
    this.undoStack.push(snapshot);
    if (this.undoStack.length > this.maxSize) {
      this.undoStack.shift();
    }
    
    // 清空重做栈
    this.redoStack = [];
  }
  
  /**
   * 撤销操作
   * @param {Object} currentState - 当前状态
   * @returns {Object|null} 上一个状态
   */
  undo(currentState) {
    if (this.undoStack.length === 0) return null;
    
    // 保存当前状态到重做栈
    this.redoStack.push(JSON.parse(JSON.stringify(currentState)));
    
    // 返回上一个状态
    return this.undoStack.pop();
  }
  
  /**
   * 重做操作
   * @param {Object} currentState - 当前状态
   * @returns {Object|null} 下一个状态
   */
  redo(currentState) {
    if (this.redoStack.length === 0) return null;
    
    // 保存当前状态到撤销栈
    this.undoStack.push(JSON.parse(JSON.stringify(currentState)));
    
    // 返回下一个状态
    return this.redoStack.pop();
  }
  
  canUndo() {
    return this.undoStack.length > 0;
  }
  
  canRedo() {
    return this.redoStack.length > 0;
  }
  
  clear() {
    this.undoStack = [];
    this.redoStack = [];
  }
}
```

#### 4. 查询参数注入算法

```javascript
/**
 * 将查询条件值注入到图表查询参数
 * @param {Object} component - 图表组件
 * @param {Array} conditionMappings - 条件映射列表
 * @param {Object} conditionValues - 查询条件值
 * @returns {Object} 合并后的查询参数
 */
function injectQueryParams(component, conditionMappings, conditionValues) {
  const params = {};
  
  // 获取该组件的所有映射
  const componentMappings = conditionMappings.filter(
    m => m.componentId === component.id
  );
  
  // 注入映射的条件值
  componentMappings.forEach(mapping => {
    const conditionValue = conditionValues[mapping.conditionId];
    if (conditionValue !== undefined && conditionValue !== null) {
      params[mapping.fieldName] = conditionValue;
    }
  });
  
  // 合并图表级过滤器
  if (component.dataConfig.filters) {
    component.dataConfig.filters.forEach(filter => {
      params[filter.field] = filter.value;
    });
  }
  
  return params;
}
```

#### 5. 级联条件加载算法

```javascript
/**
 * 加载级联条件选项
 * @param {Object} condition - 查询条件
 * @param {Object} parentValues - 父条件值
 * @returns {Promise<Array>} 选项列表
 */
async function loadCascadeOptions(condition, parentValues) {
  if (!condition.parentConditionId) {
    // 非级联条件,直接加载
    return loadOptions(condition);
  }
  
  // 检查父条件是否有值
  const parentValue = parentValues[condition.parentConditionId];
  if (!parentValue) {
    return [];  // 父条件未选择,返回空
  }
  
  // 替换URL中的父条件参数
  let url = condition.config.dataSource.url;
  const params = { ...condition.config.dataSource.params };
  
  Object.keys(params).forEach(key => {
    const value = params[key];
    if (typeof value === 'string' && value.startsWith('${')) {
      // 替换占位符
      const conditionId = value.slice(2, -1);
      params[key] = parentValues[conditionId];
    }
  });
  
  // 发起请求
  const response = await request({
    url: url,
    method: 'get',
    params: params
  });
  
  return response.data.map(item => ({
    label: item[condition.config.dataSource.labelField],
    value: item[condition.config.dataSource.valueField]
  }));
}
```

## 正确性属性

属性是一个特征或行为,应该在系统的所有有效执行中保持为真——本质上是关于系统应该做什么的形式化陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。

### 属性 1: 网格吸附一致性

*对于任意*组件和任意拖拽位置,拖拽后组件的位置坐标应该是网格大小的整数倍

**验证: 需求 1.2, 7.4, 9.1**

### 属性 2: 尺寸吸附一致性

*对于任意*组件和任意调整大小操作,调整后组件的宽度和高度应该是网格大小的整数倍

**验证: 需求 1.3, 9.2**

### 属性 3: 撤销重做往返

*对于任意*画布状态,执行任意操作后撤销再重做,应该返回到操作后的状态

**验证: 需求 1.6**

### 属性 4: 图层管理正确性

*对于任意*组件集合,当执行"置于顶层"操作时,该组件的z-index应该大于所有其他组件;当执行"置于底层"操作时,该组件的z-index应该小于所有其他组件

**验证: 需求 1.7, 7.5**

### 属性 5: 组件添加完整性

*对于任意*组件类型,添加组件后,组件列表的长度应该增加1,且新组件应该出现在列表中

**验证: 需求 2.3**

### 属性 6: 维度配置结构完整性

*对于任意*图表组件,配置维度后,dataConfig.dimensions数组应该包含所有配置的维度,且每个维度应该有field、axis和label字段

**验证: 需求 3.3**

### 属性 7: 指标配置结构完整性

*对于任意*图表组件,配置指标后,dataConfig.metrics数组应该包含所有配置的指标,且每个指标应该有field、aggregation和label字段

**验证: 需求 3.4**

### 属性 8: 配置验证完整性

*对于任意*图表组件,如果缺少必填字段(如datasetId或至少一个指标),验证函数应该返回错误消息

**验证: 需求 3.7**

### 属性 9: 查询条件管理正确性

*对于任意*查询条件,执行重命名、排序或显示/隐藏操作后,条件列表应该反映这些变化,且条件ID保持不变

**验证: 需求 4.2**

### 属性 10: 条件映射多对多正确性

*对于任意*查询条件,当映射到多个图表时,每个图表应该能够独立配置不同的字段映射,且一个条件的变化应该影响所有映射的图表

**验证: 需求 4.4**

### 属性 11: 级联条件依赖性

*对于任意*级联查询条件,当父条件值改变时,子条件的选项列表应该重新加载,且子条件的当前值应该被清空(如果不在新选项中)

**验证: 需求 4.6**

### 属性 12: 查询参数注入正确性

*对于任意*图表组件和任意查询条件值,当条件值改变时,所有映射到该条件的图表应该接收到包含新条件值的查询参数

**验证: 需求 4.7, 8.1**

### 属性 13: 图表刷新独立性

*对于任意*图表集合,当一个图表查询失败时,其他图表应该继续正常显示数据,不受影响

**验证: 需求 8.5**

### 属性 14: 并行刷新一致性

*对于任意*关联到同一条件的图表集合,当条件值改变时,所有图表应该同时开始刷新,且刷新完成的顺序不应影响最终显示结果

**验证: 需求 8.3**

### 属性 15: 自动刷新定时准确性

*对于任意*配置了刷新频率的图表,从上次刷新完成到下次刷新开始的时间间隔应该在配置的刷新频率的±10%范围内

**验证: 需求 8.4**

### 属性 16: 对齐参考线显示正确性

*对于任意*正在移动的组件和任意其他组件集合,当移动组件的边缘或中心与其他组件的边缘或中心在阈值范围内时,应该显示对应的对齐参考线

**验证: 需求 9.4**

### 属性 17: 网格配置应用一致性

*对于任意*网格大小配置,更改网格大小后,所有后续的拖拽和调整大小操作应该使用新的网格大小进行吸附

**验证: 需求 9.5**

### 属性 18: 仪表板持久化往返

*对于任意*仪表板配置(包括所有组件、位置、样式和查询条件),保存后重新加载应该得到等价的配置

**验证: 需求 6.2, 10.1, 10.2**

### 属性 19: 发布版本隔离性

*对于任意*仪表板,发布后修改草稿版本不应该影响已发布版本,且已发布版本应该保持不变直到下次发布

**验证: 需求 10.4**

### 属性 20: 编辑已发布仪表板创建草稿

*对于任意*已发布的仪表板,打开编辑时应该创建一个新的草稿版本,且草稿版本的初始状态应该与已发布版本相同

**验证: 需求 10.5**

### 属性 21: 预览模式数据完整性

*对于任意*仪表板,进入预览模式时,所有图表应该使用当前查询条件值执行查询,且查询参数应该与编辑模式下的参数一致

**验证: 需求 11.2**

### 属性 22: 组件复制正确性

*对于任意*组件,复制后应该创建一个新组件,其配置(dataConfig、styleConfig、advancedConfig)应该与原组件相同,但ID和位置应该不同

**验证: 需求 7.1**

### 属性 23: 组件删除完整性

*对于任意*组件,删除后,组件列表的长度应该减少1,且该组件不应该再出现在列表中,相关的条件映射也应该被删除

**验证: 需求 7.2**

### 属性 24: 组件模板往返

*对于任意*组件配置,保存为模板后使用该模板创建新组件,新组件的配置应该与原组件配置等价(除了ID和位置)

**验证: 需求 12.1, 12.2**

### 属性 25: 模板独立性

*对于任意*组件模板,更新模板后,之前使用该模板创建的组件实例应该保持不变

**验证: 需求 12.3**

### 属性 26: 模板删除完整性

*对于任意*组件模板,删除后,模板列表的长度应该减少1,且该模板不应该再出现在列表中

**验证: 需求 12.4**

### 属性 27: 仪表板名称更新一致性

*对于任意*仪表板名称,编辑名称后,工具栏显示的名称、配置中的名称和数据库中的名称应该保持一致

**验证: 需求 6.5**

## 错误处理

### 前端错误处理

#### 1. 网络错误

```javascript
// API调用错误处理
async function saveDashboard(dashboard) {
  try {
    const response = await request({
      url: '/bi/dashboard',
      method: 'post',
      data: dashboard
    });
    return response;
  } catch (error) {
    if (error.response) {
      // 服务器返回错误
      if (error.response.status === 401) {
        Message.error('会话已过期,请重新登录');
        router.push('/login');
      } else if (error.response.status === 403) {
        Message.error('没有权限执行此操作');
      } else if (error.response.status === 500) {
        Message.error('服务器错误,请稍后重试');
      } else {
        Message.error(error.response.data.msg || '操作失败');
      }
    } else if (error.request) {
      // 请求发送但没有响应
      Message.error('网络连接失败,请检查网络');
    } else {
      // 其他错误
      Message.error('操作失败: ' + error.message);
    }
    throw error;
  }
}
```

#### 2. 数据验证错误

```javascript
// 组件配置验证
function validateComponentConfig(component) {
  const errors = [];
  
  if (component.type === 'chart') {
    if (!component.dataConfig.datasetId) {
      errors.push('请选择数据集');
    }
    if (!component.dataConfig.metrics || component.dataConfig.metrics.length === 0) {
      errors.push('请至少配置一个指标');
    }
    if (component.dataConfig.metrics) {
      component.dataConfig.metrics.forEach((metric, index) => {
        if (!metric.field) {
          errors.push(`指标${index + 1}缺少字段`);
        }
        if (!metric.aggregation) {
          errors.push(`指标${index + 1}缺少聚合方法`);
        }
      });
    }
  }
  
  return {
    valid: errors.length === 0,
    errors: errors
  };
}
```

#### 3. 图表渲染错误

```javascript
// ChartWidget组件中的错误处理
methods: {
  async fetchData(params) {
    this.loading = true;
    this.error = null;
    
    try {
      const response = await queryData({
        datasetId: this.config.dataConfig.datasetId,
        dimensions: this.config.dataConfig.dimensions,
        metrics: this.config.dataConfig.metrics,
        filters: params
      });
      
      this.chartData = response.data;
      this.updateChart();
    } catch (error) {
      this.error = '数据加载失败: ' + (error.message || '未知错误');
      console.error('Chart data fetch error:', error);
    } finally {
      this.loading = false;
    }
  },
  
  updateChart() {
    try {
      const option = this.buildChartOption();
      this.chartInstance.setOption(option, true);
    } catch (error) {
      this.error = '图表渲染失败: ' + error.message;
      console.error('Chart render error:', error);
    }
  }
}
```

#### 4. 状态恢复错误

```javascript
// 加载仪表板时的错误处理
async function loadDashboard(id) {
  try {
    const response = await getDashboardConfig(id);
    const config = response.data;
    
    // 验证配置完整性
    if (!config.dashboard) {
      throw new Error('仪表板配置缺失');
    }
    
    // 过滤无效组件
    const validComponents = config.components.filter(comp => {
      if (!comp.type || !comp.position || !comp.size) {
        console.warn('跳过无效组件:', comp);
        return false;
      }
      return true;
    });
    
    if (validComponents.length < config.components.length) {
      Message.warning(`已跳过${config.components.length - validComponents.length}个无效组件`);
    }
    
    return {
      ...config,
      components: validComponents
    };
  } catch (error) {
    Message.error('加载仪表板失败: ' + error.message);
    throw error;
  }
}
```

### 后端错误处理

#### 1. 业务逻辑错误

```java
@Service
public class DashboardServiceImpl implements IDashboardService {
    
    @Override
    public int insertDashboard(Dashboard dashboard) {
        // 验证仪表板名称
        if (StringUtils.isEmpty(dashboard.getDashboardName())) {
            throw new ServiceException("仪表板名称不能为空");
        }
        
        // 检查名称重复
        Dashboard existing = dashboardMapper.selectDashboardByName(
            dashboard.getDashboardName()
        );
        if (existing != null) {
            throw new ServiceException("仪表板名称已存在");
        }
        
        // 验证JSON配置
        try {
            if (StringUtils.isNotEmpty(dashboard.getCanvasConfig())) {
                JSONObject.parseObject(dashboard.getCanvasConfig());
            }
            if (StringUtils.isNotEmpty(dashboard.getGlobalStyle())) {
                JSONObject.parseObject(dashboard.getGlobalStyle());
            }
        } catch (Exception e) {
            throw new ServiceException("配置格式错误: " + e.getMessage());
        }
        
        return dashboardMapper.insertDashboard(dashboard);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveDashboardConfig(Long dashboardId, DashboardConfig config) {
        // 验证仪表板存在
        Dashboard dashboard = dashboardMapper.selectDashboardById(dashboardId);
        if (dashboard == null) {
            throw new ServiceException("仪表板不存在");
        }
        
        try {
            // 更新仪表板
            dashboardMapper.updateDashboard(config.getDashboard());
            
            // 删除旧组件
            componentMapper.deleteComponentByDashboardId(dashboardId);
            
            // 插入新组件
            for (DashboardComponent component : config.getComponents()) {
                component.setDashboardId(dashboardId);
                componentMapper.insertComponent(component);
            }
            
            // 删除旧查询条件
            queryConditionMapper.deleteConditionByDashboardId(dashboardId);
            
            // 插入新查询条件
            for (QueryCondition condition : config.getQueryConditions()) {
                condition.setDashboardId(dashboardId);
                queryConditionMapper.insertCondition(condition);
            }
            
            // 删除旧条件映射
            conditionMappingMapper.deleteMappingByDashboardId(dashboardId);
            
            // 插入新条件映射
            for (ConditionMapping mapping : config.getConditionMappings()) {
                conditionMappingMapper.insertMapping(mapping);
            }
            
            return 1;
        } catch (Exception e) {
            throw new ServiceException("保存配置失败: " + e.getMessage());
        }
    }
}
```

#### 2. 数据库错误

```java
@RestController
@RequestMapping("/bi/dashboard")
public class DashboardController extends BaseController {
    
    @PostMapping
    @PreAuthorize("@ss.hasPermi('bi:dashboard:add')")
    @Log(title = "仪表板", businessType = BusinessType.INSERT)
    public AjaxResult add(@Validated @RequestBody Dashboard dashboard) {
        try {
            return toAjax(dashboardService.insertDashboard(dashboard));
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            logger.error("创建仪表板失败", e);
            return AjaxResult.error("创建仪表板失败,请联系管理员");
        }
    }
    
    @GetMapping("/{id}/config")
    @PreAuthorize("@ss.hasPermi('bi:dashboard:query')")
    public AjaxResult getConfig(@PathVariable Long id) {
        try {
            DashboardConfig config = dashboardService.getDashboardConfig(id);
            if (config == null) {
                return AjaxResult.error("仪表板不存在");
            }
            return AjaxResult.success(config);
        } catch (Exception e) {
            logger.error("获取仪表板配置失败", e);
            return AjaxResult.error("获取配置失败,请稍后重试");
        }
    }
}
```

#### 3. 权限错误

```java
// 通过Spring Security的@PreAuthorize注解处理
// 如果用户没有权限,会自动返回403错误

// 在Service层也可以添加额外的权限检查
@Override
public Dashboard selectDashboardById(Long id) {
    Dashboard dashboard = dashboardMapper.selectDashboardById(id);
    if (dashboard == null) {
        return null;
    }
    
    // 检查用户是否有权限访问该仪表板
    String currentUser = SecurityUtils.getUsername();
    if (!dashboard.getCreateBy().equals(currentUser) && 
        !SecurityUtils.isAdmin()) {
        throw new ServiceException("没有权限访问该仪表板");
    }
    
    return dashboard;
}
```

## 测试策略

### 双重测试方法

本系统采用单元测试和基于属性的测试相结合的方法:

- **单元测试**: 验证特定示例、边缘情况和错误条件
- **属性测试**: 通过随机化验证所有输入的通用属性
- **集成测试**: 验证组件之间的交互和端到端流程

两者是互补的,对于全面覆盖都是必需的。单元测试捕获具体的错误,属性测试验证一般正确性。

### 单元测试策略

#### 前端单元测试 (Jest + Vue Test Utils)

**测试重点**:
- 组件渲染和UI交互
- 特定边缘情况
- 错误处理
- 用户交互流程

**示例测试**:

```javascript
// tests/unit/components/DesignerCanvas.spec.js
import { mount } from '@vue/test-utils';
import DesignerCanvas from '@/components/DesignerCanvas.vue';

describe('DesignerCanvas', () => {
  // 示例测试: 空画布显示引导提示
  it('should display empty state guidance when no components', () => {
    const wrapper = mount(DesignerCanvas, {
      propsData: {
        components: [],
        gridSize: 10
      }
    });
    
    expect(wrapper.find('.empty-state').exists()).toBe(true);
    expect(wrapper.find('.empty-state').text()).toContain('添加组件');
  });
  
  // 示例测试: 组件选择显示高亮边框
  it('should show blue border when component is selected', async () => {
    const component = {
      id: 'comp_001',
      type: 'chart',
      position: { x: 100, y: 100 },
      size: { width: 400, height: 300 }
    };
    
    const wrapper = mount(DesignerCanvas, {
      propsData: {
        components: [component],
        gridSize: 10
      }
    });
    
    await wrapper.find('.component-wrapper').trigger('click');
    
    expect(wrapper.find('.component-wrapper').classes()).toContain('selected');
    expect(wrapper.emitted('component-select')).toBeTruthy();
  });
  
  // 边缘情况测试: 组件拖拽到画布边界外
  it('should constrain component position within canvas bounds', () => {
    const wrapper = mount(DesignerCanvas, {
      propsData: {
        components: [],
        gridSize: 10,
        canvasConfig: { width: 1920, height: 1080 }
      }
    });
    
    const result = wrapper.vm.constrainPosition({ x: 2000, y: 1200 });
    
    expect(result.x).toBeLessThanOrEqual(1920);
    expect(result.y).toBeLessThanOrEqual(1080);
  });
});
```

#### 后端单元测试 (JUnit 5 + Mockito)

**测试重点**:
- 业务逻辑验证
- 数据验证
- 错误处理
- 边界条件

**示例测试**:

```java
// src/test/java/com/zjrcu/iras/bi/platform/service/DashboardServiceTest.java
@SpringBootTest
public class DashboardServiceTest {
    
    @Autowired
    private IDashboardService dashboardService;
    
    @MockBean
    private DashboardMapper dashboardMapper;
    
    // 示例测试: 创建仪表板成功
    @Test
    public void testInsertDashboard_Success() {
        Dashboard dashboard = new Dashboard();
        dashboard.setDashboardName("测试仪表板");
        dashboard.setTheme("light");
        
        when(dashboardMapper.selectDashboardByName(anyString())).thenReturn(null);
        when(dashboardMapper.insertDashboard(any())).thenReturn(1);
        
        int result = dashboardService.insertDashboard(dashboard);
        
        assertEquals(1, result);
        verify(dashboardMapper).insertDashboard(dashboard);
    }
    
    // 边缘情况测试: 仪表板名称为空
    @Test
    public void testInsertDashboard_EmptyName() {
        Dashboard dashboard = new Dashboard();
        dashboard.setDashboardName("");
        
        assertThrows(ServiceException.class, () -> {
            dashboardService.insertDashboard(dashboard);
        });
    }
    
    // 边缘情况测试: 仪表板名称重复
    @Test
    public void testInsertDashboard_DuplicateName() {
        Dashboard existing = new Dashboard();
        existing.setId(1L);
        existing.setDashboardName("测试仪表板");
        
        Dashboard newDashboard = new Dashboard();
        newDashboard.setDashboardName("测试仪表板");
        
        when(dashboardMapper.selectDashboardByName("测试仪表板"))
            .thenReturn(existing);
        
        assertThrows(ServiceException.class, () -> {
            dashboardService.insertDashboard(newDashboard);
        });
    }
    
    // 边缘情况测试: 无效的JSON配置
    @Test
    public void testInsertDashboard_InvalidJSON() {
        Dashboard dashboard = new Dashboard();
        dashboard.setDashboardName("测试仪表板");
        dashboard.setCanvasConfig("{invalid json}");
        
        when(dashboardMapper.selectDashboardByName(anyString())).thenReturn(null);
        
        assertThrows(ServiceException.class, () -> {
            dashboardService.insertDashboard(dashboard);
        });
    }
}
```

### 基于属性的测试策略

#### 测试库选择

- **前端**: fast-check (JavaScript属性测试库)
- **后端**: jqwik (Java属性测试库)

#### 测试配置

- 每个属性测试最少运行 **100次迭代**
- 每个测试必须引用设计文档中的属性
- 标签格式: **Feature: dashboard-designer, Property {number}: {property_text}**

#### 前端属性测试示例

```javascript
// tests/property/gridSnap.spec.js
import fc from 'fast-check';
import { snapToGrid } from '@/utils/gridUtils';

describe('Property Tests: Grid Snap', () => {
  /**
   * Feature: dashboard-designer, Property 1: 网格吸附一致性
   * 对于任意组件和任意拖拽位置,拖拽后组件的位置坐标应该是网格大小的整数倍
   */
  it('should snap any position to grid multiples', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: -1000, max: 3000 }), // x坐标
        fc.integer({ min: -1000, max: 3000 }), // y坐标
        fc.integer({ min: 5, max: 50 }),       // 网格大小
        (x, y, gridSize) => {
          const snappedX = snapToGrid(x, gridSize);
          const snappedY = snapToGrid(y, gridSize);
          
          // 验证结果是网格大小的整数倍
          expect(snappedX % gridSize).toBe(0);
          expect(snappedY % gridSize).toBe(0);
          
          // 验证吸附距离不超过半个网格
          expect(Math.abs(snappedX - x)).toBeLessThanOrEqual(gridSize / 2);
          expect(Math.abs(snappedY - y)).toBeLessThanOrEqual(gridSize / 2);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-designer, Property 2: 尺寸吸附一致性
   * 对于任意组件和任意调整大小操作,调整后组件的宽度和高度应该是网格大小的整数倍
   */
  it('should snap any size to grid multiples', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 50, max: 2000 }),    // 宽度
        fc.integer({ min: 50, max: 2000 }),    // 高度
        fc.integer({ min: 5, max: 50 }),       // 网格大小
        (width, height, gridSize) => {
          const snappedWidth = snapToGrid(width, gridSize);
          const snappedHeight = snapToGrid(height, gridSize);
          
          expect(snappedWidth % gridSize).toBe(0);
          expect(snappedHeight % gridSize).toBe(0);
          expect(snappedWidth).toBeGreaterThan(0);
          expect(snappedHeight).toBeGreaterThan(0);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  /**
   * Feature: dashboard-designer, Property 18: 仪表板持久化往返
   * 对于任意仪表板配置,保存后重新加载应该得到等价的配置
   */
  it('should preserve dashboard config through save and load', () => {
    fc.assert(
      fc.property(
        fc.record({
          dashboardName: fc.string({ minLength: 1, maxLength: 100 }),
          theme: fc.constantFrom('light', 'dark'),
          components: fc.array(
            fc.record({
              type: fc.constantFrom('chart', 'query', 'text'),
              position: fc.record({
                x: fc.integer({ min: 0, max: 1920 }),
                y: fc.integer({ min: 0, max: 1080 })
              }),
              size: fc.record({
                width: fc.integer({ min: 100, max: 800 }),
                height: fc.integer({ min: 100, max: 600 })
              })
            }),
            { minLength: 0, maxLength: 10 }
          )
        }),
        async (config) => {
          // 序列化
          const serialized = JSON.stringify(config);
          
          // 反序列化
          const deserialized = JSON.parse(serialized);
          
          // 验证等价性
          expect(deserialized).toEqual(config);
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

#### 后端属性测试示例

```java
// src/test/java/com/zjrcu/iras/bi/platform/property/DashboardPropertyTest.java
@PropertyTest
public class DashboardPropertyTest {
    
    /**
     * Feature: dashboard-designer, Property 18: 仪表板持久化往返
     * 对于任意仪表板配置,保存后重新加载应该得到等价的配置
     */
    @Property(tries = 100)
    void dashboardConfigRoundTrip(
        @ForAll @StringLength(min = 1, max = 100) String name,
        @ForAll @From("themes") String theme,
        @ForAll @Size(max = 10) List<@From("components") DashboardComponent> components
    ) {
        // 创建仪表板配置
        Dashboard dashboard = new Dashboard();
        dashboard.setDashboardName(name);
        dashboard.setTheme(theme);
        
        DashboardConfig config = new DashboardConfig();
        config.setDashboard(dashboard);
        config.setComponents(components);
        
        // 序列化为JSON
        String json = JSONObject.toJSONString(config);
        
        // 反序列化
        DashboardConfig deserialized = JSONObject.parseObject(
            json, 
            DashboardConfig.class
        );
        
        // 验证等价性
        assertEquals(config.getDashboard().getDashboardName(), 
                    deserialized.getDashboard().getDashboardName());
        assertEquals(config.getDashboard().getTheme(), 
                    deserialized.getDashboard().getTheme());
        assertEquals(config.getComponents().size(), 
                    deserialized.getComponents().size());
    }
    
    @Provide
    Arbitrary<String> themes() {
        return Arbitraries.of("light", "dark");
    }
    
    @Provide
    Arbitrary<DashboardComponent> components() {
        return Combinators.combine(
            Arbitraries.of("chart", "query", "text"),
            Arbitraries.integers().between(0, 1920),
            Arbitraries.integers().between(0, 1080),
            Arbitraries.integers().between(100, 800),
            Arbitraries.integers().between(100, 600)
        ).as((type, x, y, width, height) -> {
            DashboardComponent comp = new DashboardComponent();
            comp.setComponentType(type);
            comp.setPositionX(x);
            comp.setPositionY(y);
            comp.setWidth(width);
            comp.setHeight(height);
            return comp;
        });
    }
}
```

### 集成测试策略

#### 端到端测试 (Cypress)

**测试场景**:
1. 创建新仪表板并添加组件
2. 配置查询条件并映射到图表
3. 保存、预览和发布仪表板
4. 加载已保存的仪表板并验证状态

```javascript
// cypress/integration/dashboard-designer.spec.js
describe('Dashboard Designer E2E', () => {
  beforeEach(() => {
    cy.login('admin', 'admin123');
    cy.visit('/bi/dashboard/designer');
  });
  
  it('should create dashboard with chart and query component', () => {
    // 创建新仪表板
    cy.get('[data-test="new-dashboard"]').click();
    cy.get('[data-test="dashboard-name"]').type('测试仪表板');
    
    // 添加查询组件
    cy.get('[data-test="add-query-component"]').click();
    cy.get('.canvas').should('contain', '查询组件');
    
    // 添加图表组件
    cy.get('[data-test="add-chart"]').click();
    cy.get('[data-test="chart-type-line"]').click();
    
    // 配置图表数据
    cy.get('[data-test="select-dataset"]').click();
    cy.get('[data-test="dataset-sales"]').click();
    cy.get('[data-test="add-dimension"]').click();
    cy.get('[data-test="dimension-field"]').select('date');
    cy.get('[data-test="add-metric"]').click();
    cy.get('[data-test="metric-field"]').select('amount');
    cy.get('[data-test="metric-aggregation"]').select('sum');
    
    // 保存仪表板
    cy.get('[data-test="save-dashboard"]').click();
    cy.get('.el-message').should('contain', '保存成功');
    
    // 验证保存后可以加载
    cy.reload();
    cy.get('.canvas').should('contain', '查询组件');
    cy.get('.canvas .chart-component').should('exist');
  });
});
```

### 测试覆盖率目标

- **单元测试**: 代码覆盖率 ≥ 80%
- **属性测试**: 所有27个正确性属性都有对应的属性测试
- **集成测试**: 覆盖主要用户流程和跨组件交互
- **E2E测试**: 覆盖关键业务场景

### 持续集成

- 所有测试在每次提交时自动运行
- 属性测试失败时保存反例用于调试
- 测试报告自动生成并归档
- 覆盖率报告集成到CI/CD流程
