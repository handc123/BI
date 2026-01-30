# BI平台安全措施

## 概述

本文档描述了BI平台实施的所有安全措施，包括数据加密、权限控制、审计日志和安全最佳实践。

## 数据加密

### 1. 数据源凭据加密

**加密算法**: AES-256

**加密范围**:
- 数据库密码
- API密钥
- 其他敏感凭据

**实现位置**:
- `DataSourceServiceImpl.encryptPassword()` - 保存时加密
- `DataSourceManager.getConnection()` - 使用时解密

**加密流程**:
```
用户输入密码 → AES加密 → 存储到数据库(config字段)
数据库读取 → AES解密 → 建立数据库连接
```

**代码示例**:
```java
// 保存时加密
private void encryptPassword(DataSource dataSource) {
    Map<String, Object> configMap = dataSource.getConfigMap();
    String password = (String) configMap.get("password");
    
    if (StringUtils.isNotEmpty(password) && !"********".equals(password)) {
        String encryptedPassword = AesUtils.encrypt(password);
        configMap.put("password", encryptedPassword);
        dataSource.setConfigMap(configMap);
    }
}

// 使用时解密
String encryptedPassword = (String) configMap.get("password");
if (StringUtils.isNotEmpty(encryptedPassword)) {
    String password = AesUtils.decrypt(encryptedPassword);
    config.setPassword(password);
}
```

### 2. 密码掩码

在返回数据源配置给前端时，密码字段被掩码为`********`：

```java
private void maskPassword(DataSource dataSource) {
    Map<String, Object> configMap = dataSource.getConfigMap();
    if (configMap.containsKey("password")) {
        configMap.put("password", "********");
        dataSource.setConfigMap(configMap);
    }
}
```

### 3. 加密密钥管理

**密钥存储**: 
- 密钥存储在应用配置文件中
- 生产环境应使用环境变量或密钥管理服务

**密钥轮换**:
- 建议定期轮换加密密钥
- 轮换时需要重新加密所有已存储的密码

## 权限控制

### 1. 认证机制

**JWT Token认证**:
- 所有API请求需要携带有效的JWT Token
- Token包含用户ID、用户名、角色等信息
- Token有效期可配置（默认30分钟）

**共享链接访问**:
- 共享链接访问不需要JWT Token
- 通过共享码和密码进行验证
- 支持过期时间和访问次数限制

### 2. 授权机制

**功能权限**:
- 使用`@PreAuthorize`注解进行方法级权限检查
- 权限标识格式：`bi:module:action`
- 示例：`bi:datasource:add`

**数据权限**:
- 基于用户部门ID过滤数据
- 用户只能访问其部门及子部门的数据
- 在SQL查询中自动添加部门过滤条件

### 3. 行级安全

**实现方式**:
```java
// 在QueryExecutor中应用行级安全
if (user != null && user.getDeptId() != null) {
    sql += " AND dept_id = " + user.getDeptId();
}
```

**适用范围**:
- 数据集查询
- 可视化数据获取
- 仪表板数据加载

## 审计日志

### 1. 日志记录范围

**记录的操作**:
- 数据源的创建、修改、删除
- 数据集的创建、修改、删除、抽取
- 可视化的创建、修改、删除
- 仪表板的创建、修改、删除、导出、共享
- 审计日志的查看、删除

**记录的信息**:
- 用户ID和用户名
- 操作类型
- 资源类型和资源ID
- 操作详情（JSON格式）
- IP地址
- 操作时间

### 2. 日志存储

**存储位置**: MySQL数据库 `bi_audit_log`表

**存储策略**:
- 异步写入，不影响主业务性能
- 定期清理过期日志（默认保留90天）
- 支持按条件查询和导出

### 3. 日志查询

**查询条件**:
- 用户
- 操作类型
- 资源类型
- 时间范围

**查询API**: `GET /bi/audit/list`

## 输入验证

### 1. 参数验证

**使用Jakarta Bean Validation**:
```java
@PostMapping
public AjaxResult add(@Validated @RequestBody DataSource dataSource) {
    // 方法实现
}
```

**验证规则**:
- 必填字段检查
- 字段长度限制
- 字段格式验证
- 业务规则验证

### 2. SQL注入防护

**使用参数化查询**:
```xml
<select id="selectDataSourceList" parameterType="DataSource" resultMap="DataSourceResult">
    select * from bi_datasource
    where name like concat('%', #{name}, '%')
</select>
```

**禁止动态SQL拼接**:
- 不直接拼接用户输入到SQL语句
- 使用MyBatis的参数绑定机制

### 3. XSS防护

**输出转义**:
- 前端使用Vue的模板语法自动转义
- 后端返回的HTML内容进行转义

**Content Security Policy**:
- 配置CSP头限制资源加载来源

## 会话管理

### 1. Token管理

**Token生成**:
- 使用安全的随机数生成器
- 包含用户信息和过期时间
- 使用HMAC-SHA256签名

**Token刷新**:
- 支持Token自动刷新
- 刷新窗口期可配置

**Token撤销**:
- 支持强制用户下线
- 清除Redis中的Token缓存

### 2. 共享链接管理

**共享码生成**:
```java
String shareCode = UUID.randomUUID().toString().replace("-", "");
```

**访问控制**:
- 密码保护（可选）
- 过期时间控制
- 访问次数限制
- 支持随时撤销

## 网络安全

### 1. HTTPS

**强制HTTPS**:
- 生产环境强制使用HTTPS
- 配置SSL/TLS证书
- 禁用不安全的协议版本

### 2. CORS配置

**跨域资源共享**:
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("https://trusted-domain.com");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        // 配置其他CORS选项
    }
}
```

### 3. 请求限流

**防止DDoS攻击**:
- 限制单个IP的请求频率
- 限制单个用户的请求频率
- 对导出等耗时操作进行特殊限流

## 数据库安全

### 1. 连接池配置

**HikariCP配置**:
```json
{
  "minConnections": 2,
  "maxConnections": 10,
  "connectionTimeout": 30000,
  "idleTimeout": 600000,
  "maxLifetime": 1800000
}
```

**安全措施**:
- 限制最大连接数
- 设置连接超时
- 定期回收空闲连接

### 2. 数据库用户权限

**最小权限原则**:
- 应用数据库用户只授予必要的权限
- 不使用root或管理员账户
- 分离读写权限

### 3. 数据备份

**备份策略**:
- 定期全量备份
- 增量备份
- 异地备份
- 备份加密

## 安全配置

### 1. 应用配置

**application.yml**:
```yaml
# 安全配置
security:
  # JWT配置
  jwt:
    secret: ${JWT_SECRET:your-secret-key}
    expiration: 1800000  # 30分钟
  
  # 加密配置
  aes:
    key: ${AES_KEY:your-aes-key}
  
  # 审计日志配置
  audit:
    enabled: true
    retention-days: 90
```

### 2. 环境变量

**敏感信息使用环境变量**:
```bash
export JWT_SECRET=your-jwt-secret
export AES_KEY=your-aes-key
export DB_PASSWORD=your-db-password
```

### 3. 配置文件权限

**文件权限设置**:
```bash
chmod 600 application.yml
chmod 600 application-prod.yml
```

## 安全最佳实践

### 1. 开发阶段

- [ ] 使用安全的编码规范
- [ ] 进行代码安全审查
- [ ] 使用静态代码分析工具
- [ ] 编写安全测试用例

### 2. 部署阶段

- [ ] 使用HTTPS
- [ ] 配置防火墙规则
- [ ] 启用日志记录
- [ ] 配置监控告警

### 3. 运维阶段

- [ ] 定期更新依赖库
- [ ] 定期安全扫描
- [ ] 定期审查审计日志
- [ ] 定期备份数据

### 4. 应急响应

- [ ] 制定安全事件响应计划
- [ ] 建立安全事件报告机制
- [ ] 定期进行安全演练
- [ ] 保持安全联系人信息更新

## 安全检查清单

### 数据安全

- [x] 数据源密码加密存储
- [x] 密码字段掩码显示
- [x] 敏感数据传输加密
- [ ] 数据库连接加密
- [ ] 数据备份加密

### 访问控制

- [x] JWT Token认证
- [x] 方法级权限控制
- [x] 数据级权限控制
- [x] 共享链接访问控制
- [ ] IP白名单

### 审计日志

- [x] 操作日志记录
- [x] 日志查询功能
- [x] 日志清理功能
- [ ] 日志导出功能
- [ ] 日志告警功能

### 输入验证

- [x] 参数验证
- [x] SQL注入防护
- [ ] XSS防护
- [ ] CSRF防护
- [ ] 文件上传验证

### 网络安全

- [ ] HTTPS配置
- [ ] CORS配置
- [ ] 请求限流
- [ ] DDoS防护
- [ ] WAF配置

## 已知安全问题

### 1. 共享链接存储

**问题**: 当前使用内存存储，重启后丢失

**影响**: 中等

**解决方案**: 迁移到数据库存储

**计划**: 阶段8实现

### 2. PDF导出

**问题**: PDF导出功能为占位符实现

**影响**: 低

**解决方案**: 集成ECharts服务器端渲染

**计划**: 后续版本实现

### 3. 加密密钥管理

**问题**: 加密密钥存储在配置文件中

**影响**: 高

**解决方案**: 使用密钥管理服务（如AWS KMS、Azure Key Vault）

**计划**: 生产部署前实现

## 安全更新日志

### 2024-01-20

- ✅ 实现数据源密码AES-256加密
- ✅ 实现审计日志功能
- ✅ 完善权限控制体系
- ✅ 实现共享链接访问控制

### 待实现

- ⏳ 数据库连接加密
- ⏳ HTTPS强制配置
- ⏳ 请求限流
- ⏳ 密钥管理服务集成

## 安全联系方式

**安全问题报告**: security@example.com

**紧急联系人**: 系统管理员

**响应时间**: 24小时内响应，72小时内解决

---

**文档版本**: 1.0
**最后更新**: 2024-01-20
**维护者**: Kiro AI Assistant
