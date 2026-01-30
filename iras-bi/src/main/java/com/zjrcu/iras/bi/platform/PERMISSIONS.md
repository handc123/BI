# BI平台权限配置

## 概述

本文档描述了BI平台的权限体系，包括所有权限定义、权限检查机制和权限配置方法。

## 权限定义

### 数据源管理权限

| 权限标识 | 权限名称 | 说明 |
|---------|---------|------|
| `bi:datasource:list` | 查看数据源列表 | 允许查看数据源列表 |
| `bi:datasource:query` | 查询数据源详情 | 允许查看数据源详细信息 |
| `bi:datasource:add` | 新增数据源 | 允许创建新的数据源 |
| `bi:datasource:edit` | 编辑数据源 | 允许修改数据源配置 |
| `bi:datasource:remove` | 删除数据源 | 允许删除数据源 |
| `bi:datasource:test` | 测试数据源连接 | 允许测试数据源连接 |
| `bi:datasource:upload` | 上传文件数据源 | 允许上传文件作为数据源 |

### 数据集管理权限

| 权限标识 | 权限名称 | 说明 |
|---------|---------|------|
| `bi:dataset:list` | 查看数据集列表 | 允许查看数据集列表 |
| `bi:dataset:query` | 查询数据集详情 | 允许查看数据集详细信息 |
| `bi:dataset:add` | 新增数据集 | 允许创建新的数据集 |
| `bi:dataset:edit` | 编辑数据集 | 允许修改数据集配置 |
| `bi:dataset:remove` | 删除数据集 | 允许删除数据集 |
| `bi:dataset:preview` | 预览数据集数据 | 允许预览数据集数据 |
| `bi:dataset:extract` | 执行数据抽取 | 允许立即执行数据抽取 |

### 可视化管理权限

| 权限标识 | 权限名称 | 说明 |
|---------|---------|------|
| `bi:visualization:list` | 查看可视化列表 | 允许查看可视化列表 |
| `bi:visualization:query` | 查询可视化详情 | 允许查看可视化详细信息 |
| `bi:visualization:add` | 新增可视化 | 允许创建新的可视化 |
| `bi:visualization:edit` | 编辑可视化 | 允许修改可视化配置 |
| `bi:visualization:remove` | 删除可视化 | 允许删除可视化 |
| `bi:visualization:export` | 导出可视化数据 | 允许导出可视化数据为CSV/Excel |

### 仪表板管理权限

| 权限标识 | 权限名称 | 说明 |
|---------|---------|------|
| `bi:dashboard:list` | 查看仪表板列表 | 允许查看仪表板列表 |
| `bi:dashboard:query` | 查询仪表板详情 | 允许查看仪表板详细信息 |
| `bi:dashboard:add` | 新增仪表板 | 允许创建新的仪表板 |
| `bi:dashboard:edit` | 编辑仪表板 | 允许修改仪表板配置 |
| `bi:dashboard:remove` | 删除仪表板 | 允许删除仪表板 |
| `bi:dashboard:view` | 查看仪表板数据 | 允许查看仪表板数据 |
| `bi:dashboard:export` | 导出仪表板 | 允许导出仪表板为PDF |
| `bi:dashboard:share` | 共享仪表板 | 允许生成仪表板共享链接 |

### 审计日志权限

| 权限标识 | 权限名称 | 说明 |
|---------|---------|------|
| `bi:audit:list` | 查看审计日志列表 | 允许查看审计日志列表 |
| `bi:audit:query` | 查询审计日志详情 | 允许查看审计日志详细信息 |
| `bi:audit:remove` | 删除审计日志 | 允许删除审计日志 |
| `bi:audit:clean` | 清理审计日志 | 允许清理过期审计日志 |

### 监控权限

| 权限标识 | 权限名称 | 说明 |
|---------|---------|------|
| `bi:monitor:query` | 查询监控数据 | 允许查看性能监控数据 |
| `bi:monitor:stats` | 查询统计信息 | 允许查看统计信息 |

## 权限检查机制

### Controller层权限检查

所有Controller方法都使用`@PreAuthorize`注解进行权限检查：

```java
@PreAuthorize("@ss.hasPermi('bi:datasource:add')")
@PostMapping
public AjaxResult add(@RequestBody DataSource dataSource) {
    // 方法实现
}
```

### Service层数据权限

Service层实现行级数据权限控制，基于用户的部门ID过滤数据：

```java
// 在QueryExecutor中应用数据权限
if (user != null && user.getDeptId() != null) {
    sql += " AND dept_id = " + user.getDeptId();
}
```

### 共享链接访问

共享链接访问不需要系统认证，但需要：
1. 有效的共享码
2. 正确的访问密码（如果设置）
3. 未过期
4. 未超过访问次数限制

## 权限配置方法

### 1. 在sys_menu表中添加菜单

```sql
-- BI平台主菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('BI平台', 0, 5, 'bi', NULL, 1, 0, 'M', '0', '0', NULL, 'chart', 'admin', NOW(), '', NULL, 'BI报表平台');

-- 数据源管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('数据源管理', (SELECT menu_id FROM sys_menu WHERE menu_name = 'BI平台'), 1, 'datasource', 'bi/datasource/index', 1, 0, 'C', '0', '0', 'bi:datasource:list', 'database', 'admin', NOW(), '', NULL, '数据源管理');
```

### 2. 为角色分配权限

在若依系统的角色管理界面中，为角色分配BI平台相关权限。

### 3. 权限继承

权限遵循若依框架的继承规则：
- 管理员角色拥有所有权限
- 普通角色需要显式分配权限
- 用户继承其角色的权限

## 权限验证流程

```
用户请求 → Spring Security过滤器 → JWT Token验证 → 
@PreAuthorize注解检查 → 权限验证 → 
通过：执行方法 / 失败：返回403
```

## 数据权限规则

### 部门数据权限

用户只能访问其所属部门及子部门的数据：

```java
// 示例：查询数据源时应用数据权限
public List<DataSource> selectDataSourceList(DataSource dataSource) {
    // 若依框架会自动注入数据权限SQL
    // 例如：AND dept_id IN (1, 2, 3)
    return dataSourceMapper.selectDataSourceList(dataSource);
}
```

### 创建者权限

用户可以管理自己创建的资源：

```java
// 检查是否为创建者
if (!dashboard.getCreateBy().equals(SecurityUtils.getUsername())) {
    throw new ServiceException("无权操作他人创建的仪表板");
}
```

## 权限最佳实践

### 1. 最小权限原则

只授予用户完成工作所需的最小权限集。

### 2. 权限分离

将管理权限和查看权限分离：
- 普通用户：查看、导出权限
- 分析师：创建、编辑可视化和仪表板
- 管理员：管理数据源、数据集、审计日志

### 3. 定期审计

定期检查用户权限，移除不再需要的权限。

### 4. 敏感操作保护

对敏感操作（删除、导出、共享）进行额外验证：
- 二次确认
- 操作日志记录
- 限制操作频率

## 权限测试

### 单元测试

```java
@Test
@WithMockUser(authorities = "bi:datasource:add")
void testAddDataSource_WithPermission() {
    // 测试有权限的情况
}

@Test
@WithMockUser(authorities = "other:permission")
void testAddDataSource_WithoutPermission() {
    // 测试无权限的情况，应该抛出AccessDeniedException
}
```

### 集成测试

```java
@Test
void testDataSourceAccess_DifferentRoles() {
    // 测试不同角色的数据访问权限
    // 管理员应该能看到所有数据源
    // 普通用户只能看到自己部门的数据源
}
```

## 常见问题

### Q1: 用户无法访问某个功能

**解决方案**:
1. 检查用户角色是否分配了相应权限
2. 检查菜单配置中的perms字段是否正确
3. 检查Controller方法的@PreAuthorize注解

### Q2: 数据权限不生效

**解决方案**:
1. 检查用户的数据权限范围设置
2. 检查Mapper XML中是否正确应用了数据权限
3. 检查用户的部门ID是否正确

### Q3: 共享链接无法访问

**解决方案**:
1. 共享链接访问不需要系统权限
2. 检查共享码是否正确
3. 检查共享链接是否过期
4. 检查访问密码是否正确

## 权限配置示例

### 示例1: BI分析师角色

```sql
-- 创建BI分析师角色
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time)
VALUES ('BI分析师', 'bi_analyst', 5, '2', '0', '0', 'admin', NOW());

-- 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 
    (SELECT role_id FROM sys_role WHERE role_key = 'bi_analyst'),
    menu_id
FROM sys_menu
WHERE perms LIKE 'bi:%' 
  AND perms NOT LIKE 'bi:datasource:%'
  AND perms NOT LIKE 'bi:audit:%';
```

### 示例2: BI查看者角色

```sql
-- 创建BI查看者角色
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time)
VALUES ('BI查看者', 'bi_viewer', 6, '2', '0', '0', 'admin', NOW());

-- 分配权限（只读权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 
    (SELECT role_id FROM sys_role WHERE role_key = 'bi_viewer'),
    menu_id
FROM sys_menu
WHERE perms IN (
    'bi:dashboard:list',
    'bi:dashboard:query',
    'bi:dashboard:view',
    'bi:visualization:list',
    'bi:visualization:query'
);
```

## 总结

BI平台的权限体系完全集成了若依框架的权限管理机制，提供了：
- 细粒度的功能权限控制
- 基于部门的数据权限过滤
- 灵活的角色权限配置
- 完整的权限验证流程

通过合理配置权限，可以确保系统的安全性和数据的隔离性。

---

**文档版本**: 1.0
**最后更新**: 2024-01-20
**维护者**: Kiro AI Assistant
