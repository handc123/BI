# 数据集自动预览功能实现计划

## 实现步骤

### 第一步：后端 - DataSourceManager 添加方法

在 `DataSourceManager.java` 中添加以下方法：

1. `getTableSchema(Long dataSourceId, String tableName)` - 获取表结构
2. `getTablePreview(Long dataSourceId, String tableName, int limit)` - 获取表数据预览

### 第二步：后端 - IDataSourceService 接口

在 `IDataSourceService.java` 中添加接口方法：

1. `TableSchemaInfo getTableSchema(Long dataSourceId, String tableName)`
2. `TablePreviewData getTablePreview(Long dataSourceId, String tableName, int limit)`

### 第三步：后端 - DataSourceServiceImpl 实现

实现上述接口方法。

### 第四步：后端 - DataSourceController 添加端点

添加两个新的 REST API 端点：

1. `GET /bi/datasource/{id}/tables/{tableName}/schema`
2. `GET /bi/datasource/{id}/tables/{tableName}/preview`

### 第五步：前端 - API 函数

在 `ui/src/api/bi/dataset.js` 中添加：

1. `getTableSchema(dataSourceId, tableName)`
2. `getTablePreview(dataSourceId, tableName, limit)`

### 第六步：前端 - 组件修改

修改 `ui/src/views/bi/dataset/index.vue`：

1. 添加 watch 监听表名变化
2. 添加加载表结构的方法
3. 添加加载数据预览的方法
4. 添加维度和指标的显示区域
5. 添加数据预览表格

## 详细实现

### 1. DataSourceManager.getTableSchema()

```java
public TableSchemaInfo getTableSchema(Long dataSourceId, String tableName) {
    Connection connection = null;
    try {
        connection = getConnection(dataSourceId);
        DatabaseMetaData metaData = connection.getMetaData();
        
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        
        // 获取表注释
        String tableComment = getTableComment(metaData, catalog, schema, tableName);
        
        // 获取字段信息
        List<TableSchemaInfo.ColumnInfo> columns = new ArrayList<>();
        
        // 获取主键信息
        Set<String> primaryKeys = getPrimaryKeys(metaData, catalog, schema, tableName);
        
        // 获取字段列表
        try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, "%")) {
            while (rs.next()) {
                TableSchemaInfo.ColumnInfo column = new TableSchemaInfo.ColumnInfo();
                
                String columnName = rs.getString("COLUMN_NAME");
                column.setColumnName(columnName);
                column.setColumnComment(rs.getString("REMARKS"));
                column.setDataType(rs.getString("TYPE_NAME"));
                column.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                column.setDefaultValue(rs.getString("COLUMN_DEF"));
                column.setIsPrimaryKey(primaryKeys.contains(columnName));
                column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                
                // 判断字段类型（维度或指标）
                column.setFieldType(determineFieldType(column));
                
                columns.add(column);
            }
        }
        
        return new TableSchemaInfo(tableName, tableComment, columns);
    } catch (SQLException e) {
        log.error("获取表结构失败: dataSourceId={}, tableName={}, error={}", 
                dataSourceId, tableName, e.getMessage());
        throw new ServiceException("获取表结构失败: " + e.getMessage());
    } finally {
        releaseConnection(connection);
    }
}

private String determineFieldType(TableSchemaInfo.ColumnInfo column) {
    String dataType = column.getDataType().toUpperCase();
    String columnName = column.getColumnName().toLowerCase();
    
    // 主键默认为维度
    if (column.getIsPrimaryKey()) {
        return "DIMENSION";
    }
    
    // 根据字段名判断
    if (columnName.endsWith("_count") || columnName.endsWith("_amount") || 
        columnName.endsWith("_total") || columnName.endsWith("_sum") ||
        columnName.endsWith("_avg") || columnName.endsWith("_rate")) {
        return "MEASURE";
    }
    
    // 根据数据类型判断
    if (dataType.contains("INT") || dataType.contains("DECIMAL") || 
        dataType.contains("NUMERIC") || dataType.contains("DOUBLE") || 
        dataType.contains("FLOAT")) {
        // 数值类型，但以_id结尾的是维度
        if (columnName.endsWith("_id")) {
            return "DIMENSION";
        }
        return "MEASURE";
    }
    
    // 其他类型默认为维度
    return "DIMENSION";
}
```

### 2. DataSourceManager.getTablePreview()

```java
public TablePreviewData getTablePreview(Long dataSourceId, String tableName, int limit) {
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    
    try {
        connection = getConnection(dataSourceId);
        
        // 先获取表结构以获取字段注释
        TableSchemaInfo schema = getTableSchema(dataSourceId, tableName);
        Map<String, TableSchemaInfo.ColumnInfo> columnMap = schema.getColumns().stream()
                .collect(Collectors.toMap(
                        TableSchemaInfo.ColumnInfo::getColumnName,
                        col -> col
                ));
        
        // 构建查询SQL
        String sql = String.format("SELECT * FROM %s LIMIT %d", tableName, limit);
        
        statement = connection.createStatement();
        resultSet = statement.executeQuery(sql);
        
        // 获取结果集元数据
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // 构建字段元数据
        List<TablePreviewData.ColumnMeta> columns = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            TableSchemaInfo.ColumnInfo columnInfo = columnMap.get(columnName);
            
            TablePreviewData.ColumnMeta columnMeta = new TablePreviewData.ColumnMeta();
            columnMeta.setName(columnName);
            columnMeta.setComment(columnInfo != null ? columnInfo.getColumnComment() : columnName);
            columnMeta.setType(metaData.getColumnTypeName(i));
            columnMeta.setFieldType(columnInfo != null ? columnInfo.getFieldType() : "DIMENSION");
            
            columns.add(columnMeta);
        }
        
        // 读取数据行
        List<Map<String, Object>> rows = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            rows.add(row);
        }
        
        // 获取总行数
        Long total = getTableRowCount(connection, tableName);
        
        return new TablePreviewData(columns, rows, total);
    } catch (SQLException e) {
        log.error("获取表数据预览失败: dataSourceId={}, tableName={}, error={}", 
                dataSourceId, tableName, e.getMessage());
        throw new ServiceException("获取表数据预览失败: " + e.getMessage());
    } finally {
        closeResultSet(resultSet);
        closeStatement(statement);
        releaseConnection(connection);
    }
}

private Long getTableRowCount(Connection connection, String tableName) {
    String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            return rs.getLong(1);
        }
        return 0L;
    } catch (SQLException e) {
        log.warn("获取表行数失败: tableName={}, error={}", tableName, e.getMessage());
        return 0L;
    }
}
```

### 3. 前端 API 函数

```javascript
// ui/src/api/bi/dataset.js

// 获取表结构信息
export function getTableSchema(dataSourceId, tableName) {
  return request({
    url: `/bi/datasource/${dataSourceId}/tables/${tableName}/schema`,
    method: 'get'
  })
}

// 获取表数据预览
export function getTablePreview(dataSourceId, tableName, limit = 10) {
  return request({
    url: `/bi/datasource/${dataSourceId}/tables/${tableName}/preview`,
    method: 'get',
    params: { limit }
  })
}
```

### 4. 前端组件修改

```vue
<!-- ui/src/views/bi/dataset/index.vue -->

<script>
export default {
  data() {
    return {
      // ... 现有数据
      
      // 表结构信息
      tableSchema: null,
      dimensions: [],
      measures: [],
      
      // 数据预览
      previewColumns: [],
      previewRows: [],
      previewTotal: 0,
      previewLoading: false
    }
  },
  
  watch: {
    'form.queryConfig.tableName'(newVal) {
      if (newVal && this.form.datasourceId && this.form.queryConfig.sourceType === 'table') {
        this.loadTableSchemaAndPreview()
      }
    }
  },
  
  methods: {
    async loadTableSchemaAndPreview() {
      try {
        this.previewLoading = true
        
        // 加载表结构
        await this.loadTableSchema()
        
        // 加载数据预览
        await this.loadTableDataPreview()
        
        this.$modal.msgSuccess('数据预览加载成功')
      } catch (error) {
        console.error('加载失败:', error)
        this.$modal.msgError('加载失败: ' + (error.message || '未知错误'))
      } finally {
        this.previewLoading = false
      }
    },
    
    async loadTableSchema() {
      const response = await getTableSchema(
        this.form.datasourceId,
        this.form.queryConfig.tableName
      )
      
      this.tableSchema = response.data
      
      // 分类字段为维度和指标
      this.dimensions = response.data.columns.filter(c => c.fieldType === 'DIMENSION')
      this.measures = response.data.columns.filter(c => c.fieldType === 'MEASURE')
      
      // 自动填充字段配置
      this.form.fieldConfig.fields = response.data.columns.map(col => ({
        name: col.columnName,
        alias: col.columnComment || col.columnName,
        type: this.mapDataType(col.dataType),
        visible: true,
        calculated: false,
        expression: '',
        fieldType: col.fieldType
      }))
    },
    
    async loadTableDataPreview() {
      const response = await getTablePreview(
        this.form.datasourceId,
        this.form.queryConfig.tableName,
        10
      )
      
      this.previewColumns = response.data.columns
      this.previewRows = response.data.rows
      this.previewTotal = response.data.total
    },
    
    mapDataType(dbType) {
      const type = dbType.toUpperCase()
      if (type.includes('INT')) return 'INTEGER'
      if (type.includes('DECIMAL') || type.includes('NUMERIC') || 
          type.includes('DOUBLE') || type.includes('FLOAT')) return 'DECIMAL'
      if (type.includes('DATE') || type.includes('TIME')) return 'DATE'
      if (type.includes('BOOL')) return 'BOOLEAN'
      return 'STRING'
    }
  }
}
</script>
```

## 实现注意事项

1. **性能优化**：
   - 数据预览限制为 10 行
   - 考虑添加缓存机制
   - 异步加载，显示加载状态

2. **错误处理**：
   - 表不存在
   - 无权限访问
   - 网络错误
   - 数据库连接失败

3. **兼容性**：
   - 支持 MySQL、PostgreSQL、Oracle 等数据库
   - 处理不同数据库的注释获取方式

4. **用户体验**：
   - 显示加载进度
   - 提供友好的错误提示
   - 支持重新加载

## 测试计划

1. **单元测试**：
   - 测试字段类型判断逻辑
   - 测试数据类型映射

2. **集成测试**：
   - 测试获取表结构
   - 测试获取数据预览
   - 测试不同数据库类型

3. **UI 测试**：
   - 测试自动加载功能
   - 测试维度和指标显示
   - 测试中文注释显示

## 下一步

请确认这个实现计划，我将开始逐步实现代码。
