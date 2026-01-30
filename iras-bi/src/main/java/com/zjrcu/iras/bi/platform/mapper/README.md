# BI平台 Mapper接口说明

本目录包含BI平台的MyBatis Mapper接口，用于数据库操作。

## Mapper列表

### 1. DataSourceMapper
**文件**: `DataSourceMapper.java` / `DataSourceMapper.xml`  
**对应表**: `bi_datasource`  
**功能**: 数据源管理的数据库操作

**主要方法**:
- `selectDataSourceById(Long id)` - 根据ID查询数据源
- `selectDataSourceList(DataSource dataSource)` - 查询数据源列表（支持分页和条件查询）
- `insertDataSource(DataSource dataSource)` - 新增数据源
- `updateDataSource(DataSource dataSource)` - 更新数据源
- `deleteDataSourceById(Long id)` - 删除数据源
- `deleteDataSourceByIds(Long[] ids)` - 批量删除数据源

**查询条件**:
- 名称模糊查询
- 类型精确查询
- 状态精确查询

---

### 2. DatasetMapper
**文件**: `DatasetMapper.java` / `DatasetMapper.xml`  
**对应表**: `bi_dataset`  
**功能**: 数据集管理的数据库操作

**主要方法**:
- `selectDatasetById(Long id)` - 根据ID查询数据集
- `selectDatasetList(Dataset dataset)` - 查询数据集列表（支持分页和条件查询）
- `insertDataset(Dataset dataset)` - 新增数据集
- `updateDataset(Dataset dataset)` - 更新数据集
- `deleteDatasetById(Long id)` - 删除数据集
- `deleteDatasetByIds(Long[] ids)` - 批量删除数据集

**查询条件**:
- 名称模糊查询
- 数据源ID精确查询
- 类型精确查询
- 状态精确查询

---

### 3. VisualizationMapper
**文件**: `VisualizationMapper.java` / `VisualizationMapper.xml`  
**对应表**: `bi_visualization`  
**功能**: 可视化组件管理的数据库操作

**主要方法**:
- `selectVisualizationById(Long id)` - 根据ID查询可视化
- `selectVisualizationList(Visualization visualization)` - 查询可视化列表（支持分页和条件查询）
- `insertVisualization(Visualization visualization)` - 新增可视化
- `updateVisualization(Visualization visualization)` - 更新可视化
- `deleteVisualizationById(Long id)` - 删除可视化
- `deleteVisualizationByIds(Long[] ids)` - 批量删除可视化
- `selectVisualizationByDatasetId(Long datasetId)` - 根据数据集ID查询可视化列表

**查询条件**:
- 名称模糊查询
- 数据集ID精确查询
- 图表类型精确查询

---

### 4. DashboardMapper
**文件**: `DashboardMapper.java` / `DashboardMapper.xml`  
**对应表**: `bi_dashboard`  
**功能**: 仪表板管理的数据库操作

**主要方法**:
- `selectDashboardById(Long id)` - 根据ID查询仪表板
- `selectDashboardList(Dashboard dashboard)` - 查询仪表板列表（支持分页和条件查询）
- `insertDashboard(Dashboard dashboard)` - 新增仪表板
- `updateDashboard(Dashboard dashboard)` - 更新仪表板
- `deleteDashboardById(Long id)` - 删除仪表板
- `deleteDashboardByIds(Long[] ids)` - 批量删除仪表板

**查询条件**:
- 名称模糊查询
- 状态精确查询

---

### 5. AuditLogMapper
**文件**: `AuditLogMapper.java` / `AuditLogMapper.xml`  
**对应表**: `bi_audit_log`  
**功能**: 审计日志管理的数据库操作

**主要方法**:
- `selectAuditLogById(Long id)` - 根据ID查询审计日志
- `selectAuditLogList(AuditLog auditLog)` - 查询审计日志列表（支持分页和条件查询）
- `insertAuditLog(AuditLog auditLog)` - 新增审计日志
- `deleteAuditLogById(Long id)` - 删除审计日志
- `deleteAuditLogByIds(Long[] ids)` - 批量删除审计日志
- `selectAuditLogByUserId(Long userId)` - 根据用户ID查询审计日志
- `selectAuditLogByResource(String resourceType, Long resourceId)` - 根据资源类型和ID查询审计日志

**查询条件**:
- 用户ID精确查询
- 用户名模糊查询
- 操作类型精确查询
- 资源类型精确查询
- 资源ID精确查询
- IP地址精确查询
- 时间范围查询（beginTime, endTime）

---

## 通用特性

### 1. 分页支持
所有列表查询方法都支持MyBatis PageHelper分页插件，在Service层调用`startPage()`即可实现分页。

### 2. 动态SQL
所有XML文件使用MyBatis动态SQL特性：
- `<if>` 标签实现条件查询
- `<trim>` 标签处理INSERT和UPDATE的动态字段
- `<foreach>` 标签处理批量删除

### 3. 主键自增
所有INSERT操作使用`useGeneratedKeys="true" keyProperty="id"`，插入后自动回填主键ID。

### 4. 时间字段
- 创建时间：使用`sysdate()`自动设置
- 更新时间：使用`sysdate()`自动设置

### 5. 排序规则
默认按`create_time desc`降序排列，最新记录在前。

---

## XML配置规范

### ResultMap命名
- 格式：`{实体名}Result`
- 示例：`DataSourceResult`, `VisualizationResult`

### SQL片段命名
- 格式：`select{实体名}Vo`
- 示例：`selectDataSourceVo`, `selectVisualizationVo`

### 字段映射
- Java属性使用驼峰命名：`datasetId`, `createTime`
- 数据库字段使用下划线命名：`dataset_id`, `create_time`
- MyBatis自动处理驼峰转换

---

## 使用示例

### Service层调用示例

```java
@Service
public class DataSourceServiceImpl implements IDataSourceService {
    
    @Autowired
    private DataSourceMapper dataSourceMapper;
    
    @Override
    public List<DataSource> selectDataSourceList(DataSource dataSource) {
        return dataSourceMapper.selectDataSourceList(dataSource);
    }
    
    @Override
    public DataSource selectDataSourceById(Long id) {
        return dataSourceMapper.selectDataSourceById(id);
    }
    
    @Override
    public int insertDataSource(DataSource dataSource) {
        return dataSourceMapper.insertDataSource(dataSource);
    }
    
    @Override
    public int updateDataSource(DataSource dataSource) {
        return dataSourceMapper.updateDataSource(dataSource);
    }
    
    @Override
    public int deleteDataSourceByIds(Long[] ids) {
        return dataSourceMapper.deleteDataSourceByIds(ids);
    }
}
```

---

## 注意事项

1. **事务管理**: Service层方法需要添加`@Transactional`注解以支持事务
2. **参数校验**: 在Controller层使用`@Validated`进行参数校验
3. **权限控制**: 在Controller层使用`@PreAuthorize`进行权限控制
4. **日志记录**: 在Controller层使用`@Log`注解记录操作日志
5. **异常处理**: Service层抛出`ServiceException`，由全局异常处理器统一处理

---

## 下一步工作

完成Mapper层后，需要继续实现：
1. Service接口和实现类
2. Controller REST API
3. 前端API服务和页面组件
4. 单元测试和集成测试

---

**创建时间**: 2024
**维护者**: IRAS开发团队
