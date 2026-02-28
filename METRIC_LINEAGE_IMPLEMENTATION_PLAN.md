# 指标血缘与数据探索功能实施计划

## 概述
为 IRAS Smart BI 平台添加指标血缘和数据探索功能，使用户能够：
1. 点击图表查看指标规范
2. 查看指标的技术血缘关系
3. 点击血缘节点查看具体数据
4. 使用条件查询数据
5. 带权限控制的数据导出

## 实施方法
**推荐方案**: 创建专用数据库表和新的指标模块
**理由**: 遵循现有的 RuoYi/IRAS 模式，保持关注点分离，提供可扩展性

---

## 第一阶段: 数据库架构 (第1周)

### 需要创建的文件

#### 1. 数据库迁移脚本
```
D:\项目\智能监管\iras-smart-bi - 副本\sql\bi_metric_schema.sql
```

创建 5 个新表：

```sql
-- 1. 指标元数据表
CREATE TABLE bi_metric_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_code VARCHAR(50) NOT NULL UNIQUE COMMENT '业务唯一编码',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    dataset_id BIGINT COMMENT '关联数据集ID',
    visualization_id BIGINT COMMENT '关联可视化ID',
    business_definition TEXT COMMENT '业务定义',
    technical_formula TEXT COMMENT '技术计算公式',
    calculation_logic TEXT COMMENT '计算逻辑说明',
    owner_dept VARCHAR(64) COMMENT '责任部门',
    data_freshness VARCHAR(20) COMMENT '数据新鲜度(T-1,RT)',
    update_frequency VARCHAR(20) COMMENT '更新频率',
    status CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT '',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark VARCHAR(500),
    INDEX idx_metric_code (metric_code),
    INDEX idx_dataset (dataset_id),
    CONSTRAINT fk_metric_dataset FOREIGN KEY (dataset_id) REFERENCES bi_dataset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标元数据表';

-- 2. 指标血缘表
CREATE TABLE bi_metric_lineage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    upstream_metric_id BIGINT NOT NULL COMMENT '上游指标ID',
    downstream_metric_id BIGINT NOT NULL COMMENT '下游指标ID',
    transformation_type VARCHAR(50) COMMENT '转换类型:aggregation,calculation,filter',
    transformation_logic TEXT COMMENT '转换逻辑说明',
    dependency_strength INT DEFAULT 1 COMMENT '依赖强度1-5',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_upstream (upstream_metric_id),
    INDEX idx_downstream (downstream_metric_id),
    CONSTRAINT fk_lineage_upstream FOREIGN KEY (upstream_metric_id) REFERENCES bi_metric_metadata(id),
    CONSTRAINT fk_lineage_downstream FOREIGN KEY (downstream_metric_id) REFERENCES bi_metric_metadata(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标血缘关系表';

-- 3. 血缘节点表
CREATE TABLE bi_lineage_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    node_type VARCHAR(20) NOT NULL COMMENT '节点类型:metric,dataset,datasource,table,field',
    node_id BIGINT NOT NULL COMMENT '节点业务ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_attributes TEXT COMMENT '节点属性(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_node_type_id (node_type, node_id),
    UNIQUE KEY uk_node (node_type, node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血缘节点表';

-- 4. 血缘边表
CREATE TABLE bi_lineage_edge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_node_id BIGINT NOT NULL COMMENT '源节点ID',
    target_node_id BIGINT NOT NULL COMMENT '目标节点ID',
    edge_type VARCHAR(50) COMMENT '边类型:flows_to,derived_from,depends_on',
    edge_attributes TEXT COMMENT '边属性(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_source (source_node_id),
    INDEX idx_target (target_node_id),
    CONSTRAINT fk_edge_source FOREIGN KEY (source_node_id) REFERENCES bi_lineage_node(id),
    CONSTRAINT fk_edge_target FOREIGN KEY (target_node_id) REFERENCES bi_lineage_node(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血缘边表';

-- 5. 指标数据查询模板
CREATE TABLE bi_metric_data_query (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_id BIGINT NOT NULL COMMENT '指标ID',
    query_name VARCHAR(100) COMMENT '查询名称',
    query_template TEXT COMMENT '查询模板',
    default_filters TEXT COMMENT '默认过滤条件(JSON)',
    required_permissions VARCHAR(500) COMMENT '所需权限(逗号分隔)',
    row_limit INT DEFAULT 1000 COMMENT '默认行数限制',
    status CHAR(1) DEFAULT '0',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_metric (metric_id),
    CONSTRAINT fk_query_metric FOREIGN KEY (metric_id) REFERENCES bi_metric_metadata(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标数据查询模板表';
```

#### 2. 菜单权限 SQL
```
D:\项目\智能监管\iras-smart-bi - 副本\sql\bi_metric_menu.sql
```

```sql
-- 指标管理菜单 (假设父菜单ID为2000，需根据实际情况调整)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('指标元数据', 2000, 1, 'metadata', 'bi/metadata/index', 1, 0, 'C', '0', '0', 'bi:metric:query', 'chart', 'admin', NOW(), '', NULL, '指标元数据菜单'),
('指标血缘', 2000, 2, 'lineage', 'bi/lineage/index', 1, 0, 'C', '0', '0', 'bi:metric:lineage', 'tree', 'admin', NOW(), '', NULL, '指标血缘菜单'),
('指标查询', 2000, 3, 'query', 'bi/metric/query', 1, 0, 'C', '0', '0', 'bi:metric:data', 'search', 'admin', NOW(), '', NULL, '指标数据查询');

-- 指标管理按钮权限
SET @last_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('指标查询', @last_id, 1, '', '', 1, 0, 'F', '0', '0', 'bi:metric:query', '#', 'admin', NOW(), ''),
('指标新增', @last_id, 2, '', '', 1, 0, 'F', '0', '0', 'bi:metric:add', '#', 'admin', NOW(), ''),
('指标修改', @last_id, 3, '', '', 1, 0, 'F', '0', '0', 'bi:metric:edit', '#', 'admin', NOW(), ''),
('指标删除', @last_id, 4, '', '', 1, 0, 'F', '0', '0', 'bi:metric:remove', '#', 'admin', NOW(), ''),
('指标导出', @last_id, 5, '', '', 1, 0, 'F', '0', '0', 'bi:metric:export', '#', 'admin', NOW(), '');
```

---

## 第二阶段: 后端实现 (第2-3周)

### 包结构
```
iras-bi/src/main/java/com/zjrcu/iras/bi/metric/
├── controller/
│   ├── MetricMetadataController.java
│   ├── MetricLineageController.java
│   └── MetricDataController.java
├── domain/
│   ├── MetricMetadata.java
│   ├── MetricLineage.java
│   ├── LineageNode.java
│   ├── LineageEdge.java
│   └── MetricDataQuery.java
├── dto/
│   ├── LineageGraphDTO.java
│   ├── LineagePathDTO.java
│   └── MetricQueryRequest.java
├── mapper/
│   ├── MetricMetadataMapper.java
│   ├── MetricLineageMapper.java
│   ├── LineageNodeMapper.java
│   ├── LineageEdgeMapper.java
│   └── MetricDataQueryMapper.java
├── service/
│   ├── IMetricMetadataService.java
│   ├── IMetricLineageService.java
│   ├── IMetricDataService.java
│   └── impl/
│       ├── MetricMetadataServiceImpl.java
│       ├── MetricLineageServiceImpl.java
│       └── MetricDataServiceImpl.java
└── handler/
    └── MetricDataQueryHandler.java
```

### 需要创建的关键文件

#### 1. 领域实体
```
D:\项目\智能监管\iras-smart-bi - 副本\iras-bi\src\main\java\com\zjrcu\iras\bi\metric\domain\MetricMetadata.java
```

#### 2. Mapper 接口和 XML
```
D:\项目\智能监管\iras-smart-bi - 副本\iras-bi\src\main\java\com\zjrcu\iras\bi\metric\mapper\MetricMetadataMapper.java
D:\项目\智能监管\iras-smart-bi - 副本\iras-bi\src\main\resources\mapper\metric\MetricMetadataMapper.xml
```

#### 3. 控制器
关键端点：
- `GET /bi/metadata/{id}` - 获取指标规范
- `GET /bi/lineage/metric/{metricId}` - 获取血缘图
- `GET /bi/lineage/upstream/{metricId}` - 获取上游血缘
- `GET /bi/lineage/downstream/{metricId}` - 获取下游血缘
- `POST /bi/metric/data/{metricId}/query` - 查询指标数据（分页）

#### 4. 服务实现
实现：
- 图遍历算法（BFS，最大深度5）
- Redis 缓存（1小时TTL）
- 路径查找算法

### 需要修改的文件

#### 应用配置
```
D:\项目\智能监管\iras-smart-bi - 副本\iras-admin\src\main\resources\application.yml
```

添加 swagger 组：
```yaml
swagger:
  api-groups:
    metric: 指标管理模块
```

---

## 第三阶段: 前端实现 (第3-4周)

### 需要创建的关键文件

#### 1. API 服务
```
D:\项目\智能监管\iras-smart-bi - 副本\ui\src\api\bi\metric.js
```

```javascript
import request from '@/utils/request'

export function getMetricMetadata(id) {
  return request({
    url: `/bi/metadata/${id}`,
    method: 'get'
  })
}

export function getMetricLineage(metricId, mode = 'graph') {
  return request({
    url: `/bi/lineage/metric/${metricId}`,
    method: 'get',
    params: { mode }
  })
}

export function queryMetricData(metricId, data) {
  return request({
    url: `/bi/metric/data/${metricId}/query`,
    method: 'post',
    data: data
  })
}

export function exportMetricData(metricId, data) {
  return request({
    url: `/bi/metric/data/${metricId}/export`,
    method: 'post',
    data: data
  })
}
```

#### 2. 主对话框组件
```
D:\项目\智能监管\iras-smart-bi - 副本\ui\src\components\MetricDetailDialog\index.vue
```

功能：
- 3个标签页：规范、血缘、数据查询
- 使用 v-hasPermi 控制可见性
- 加载状态
- 响应式布局

#### 3. 血缘可视化组件
```
D:\项目\智能监管\iras-smart-bi - 副本\ui\src\components\MetricDetailDialog\LineageTab.vue
```

功能：
- ECharts 图形可视化
- 查看模式：全图、上游、下游
- 节点点击事件
- 按节点类型着色
- 导出图形为图片

#### 4. 数据查询组件
```
D:\项目\智能监管\iras-smart-bi - 副本\ui\src\components\MetricDetailDialog\DataQueryTab.vue
```

功能：
- 过滤器构建器
- 分页（最大10,000行）
- 导出功能
- 大数据集虚拟滚动

#### 5. 规范展示组件
```
D:\项目\智能监管\iras-smart-bi - 副本\ui\src\components\MetricDetailDialog\SpecificationTab.vue
```

展示：
- 业务定义
- 技术公式
- 计算逻辑
- 责任部门
- 数据新鲜度
- 更新频率

### 需要修改的文件

#### 1. ChartWidget
```
D:\项目\智能监管\iras-smart-bi - 副本\ui\src\components\ChartWidget\index.vue
```

添加点击处理器：
```vue
<template>
  <div
    ref="chartContainer"
    class="chart-container"
    :class="{ 'clickable': hasMetadata && !isEditMode }"
    @click="handleChartClick"
  ></div>
</template>

<script>
methods: {
  handleChartClick() {
    if (this.isEditMode) return
    const metricId = this.config.dataConfig?.metricId
    if (metricId) {
      this.$emit('metric-click', metricId)
    }
  }
}
</script>

<style scoped>
.chart-container.clickable {
  cursor: pointer;
}
</style>
```

#### 2. Dashboard View
```
D:\项目\智能监管\iras-smart-bi - 副本\ui\src\views\bi\dashboard\view.vue
```

添加对话框和事件处理器：
```vue
<template>
  <metric-detail-dialog
    :visible.sync="metricDialogVisible"
    :metric-id="selectedMetricId"
    @close="metricDialogVisible = false"
  />
</template>

<script>
import MetricDetailDialog from '@/components/MetricDetailDialog'

export default {
  components: { MetricDetailDialog },
  data() {
    return {
      metricDialogVisible: false,
      selectedMetricId: null
    }
  },
  methods: {
    handleMetricClick(metricId) {
      this.selectedMetricId = metricId
      this.metricDialogVisible = true
    }
  }
}
</script>
```

---

## 第四阶段: 集成与测试 (第4周)

### 后端测试
1. 所有服务的单元测试
2. API 端点的集成测试
3. 血缘图查询的性能测试
4. 权限执行的安全测试

### 前端测试
1. 组件单元测试
2. 用户流程的 E2E 测试：
   - 点击图表 → 查看规范
   - 查看血缘 → 点击节点 → 查看数据
   - 构建查询 → 执行 → 导出
3. 使用 v-hasPermi 进行权限测试
4. 大图形（>100节点）的性能测试

### 集成测试
1. 测试指标到数据集的链接
2. 测试血缘图遍历
3. 测试带过滤器的数据查询
4. 测试导出功能
5. 测试错误处理

---

## 第五阶段: 性能与安全 (第5周)

### 性能优化
1. 为血缘图添加 Redis 缓存
2. 使用 PageHelper 实现分页
3. 添加查询结果限制（最大10,000行）
4. 优化图遍历（限制深度为5）
5. 在外键上添加数据库索引

### 安全实现
1. 在所有控制器方法中添加 @PreAuthorize
2. 使用 @Log 进行审计跟踪
3. 通过 dept_id 实现行级安全
4. 添加查询参数验证
5. 添加导出权限检查

---

## 验证步骤

### 后端验证
1. 运行数据库迁移：`mysql -u root -p iras < sql/bi_metric_schema.sql`
2. 运行菜单权限：`mysql -u root -p iras < sql/bi_metric_menu.sql`
3. 启动后端：`mvn spring-boot:run -pl iras-admin`
4. 访问 Swagger：`http://localhost:8080/swagger-ui.html`
5. 测试端点：
   - `GET /bi/metadata/list` - 列出指标
   - `POST /bi/metadata` - 创建指标
   - `GET /bi/lineage/metric/1` - 获取血缘图

### 前端验证
1. 构建前端：`cd ui && npm run build:prod`
2. 启动开发服务器：`npm run dev`
3. 访问仪表板：`http://localhost:80`
4. 创建带有图表的测试仪表板
5. 将指标与图表关联
6. 在查看模式下点击图表
7. 验证对话框打开，显示3个标签页
8. 测试血缘可视化
9. 测试带过滤器的数据查询
10. 测试导出功能

### 端到端测试场景
1. **设置**：创建带有血缘关系的指标元数据
2. **查看规范**：点击图表 → 规范标签显示业务定义
3. **查看血缘**：切换到血缘标签 → 图显示上游/下游
4. **向下钻取**：点击图中的节点 → 数据标签显示节点数据
5. **查询**：添加过滤器 → 执行查询 → 结果分页
6. **导出**：点击导出 → 验证权限 → 下载文件

---

## 关键文件摘要

### 必须创建 (10个文件)
1. `sql/bi_metric_schema.sql` - 数据库表
2. `sql/bi_metric_menu.sql` - 菜单权限
3. `iras-bi/.../metric/domain/MetricMetadata.java` - 领域实体
4. `iras-bi/.../metric/controller/MetricMetadataController.java` - REST API
5. `iras-bi/.../metric/controller/MetricLineageController.java` - 血缘 API
6. `iras-bi/.../metric/controller/MetricDataController.java` - 数据查询 API
7. `iras-bi/.../metric/service/impl/MetricLineageServiceImpl.java` - 图逻辑
8. `ui/src/api/bi/metric.js` - API 服务
9. `ui/src/components/MetricDetailDialog/index.vue` - 主对话框
10. `ui/src/components/MetricDetailDialog/LineageTab.vue` - 图可视化

### 必须修改 (3个文件)
1. `iras-admin/.../application.yml` - 添加 swagger 组
2. `ui/src/components/ChartWidget/index.vue` - 添加点击处理器
3. `ui/src/views/bi/dashboard/view.vue` - 显示对话框

---

## 安全与权限

### 所需权限
- `bi:metric:query` - 查看指标元数据
- `bi:metric:add` - 创建指标
- `bi:metric:edit` - 编辑指标
- `bi:metric:remove` - 删除指标
- `bi:metric:lineage` - 查看血缘图
- `bi:metric:data` - 查询指标数据
- `bi:metric:export` - 导出指标数据

### 数据保护
- 通过 dept_id 实现行级安全
- 查询结果限制（最大10,000行）
- 所有数据访问的审计日志
- 所有端点的权限验证

---

## 时间表
- **第1周**: 数据库架构 + 后端基础
- **第2周**: 后端 API 开发
- **第3周**: 前端组件
- **第4周**: 集成 + 测试
- **第5周**: 性能 + 安全 + 文档

**总计：完整实施需要5周**
