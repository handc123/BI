# MySQL MCP 服务器配置指南

## 📋 安装状态

✅ **MySQL MCP 服务器已安装**: `@kevinwatt/mysql-mcp@0.1.3`
✅ **配置文件已创建**: `.mcp.json`
✅ **配置数据库**: `iras` (本地)

---

## 🔧 MCP 配置文件

配置文件位置: `D:\项目\智能监管\iras-smart-bi - 副本\.mcp.json`

```json
{
  "mcpServers": {
    "mysql-local": {
      "command": "npx",
      "args": [
        "-y",
        "@kevinwatt/mysql-mcp"
      ],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "root",
        "MYSQL_PASS": "",
        "MYSQL_DB": "iras"
      }
    }
  }
}
```

### ⚙️ 配置说明

| 参数 | 说明 | 默认值 | 当前设置 |
|------|------|--------|----------|
| MYSQL_HOST | MySQL服务器地址 | localhost | localhost |
| MYSQL_PORT | MySQL端口 | 3306 | 3306 |
| MYSQL_USER | 数据库用户名 | root | root |
| MYSQL_PASS | 数据库密码 | 空 | **需要设置** |
| MYSQL_DB | 数据库名称 | - | iras |

---

## 🔑 修改数据库密码

**重要**: 请在配置文件中设置您的MySQL密码：

1. 打开 `.mcp.json` 文件
2. 找到 `"MYSQL_PASS": ""`
3. 将空字符串改为您的MySQL密码：
   ```json
   "MYSQL_PASS": "your_password_here"
   ```

---

## 🚀 启动 MCP 服务器

### 方法1: 通过MCP客户端自动启动

如果您使用支持MCP的应用（如Cursor、Windsurf、Claude Desktop等）：

1. 在应用设置中添加MCP服务器配置
2. 粘贴上面的 `.mcp.json` 配置
3. 保存后应用会自动启动服务器

### 方法2: 手动启动MCP服务器

```bash
# Windows
npx -y @kevinwatt/mysql-mcp

# 或设置环境变量后启动
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASS=your_password
set MYSQL_DB=iras
npx @kevinwatt/mysql-mcp
```

---

## 📊 可用的MCP工具

MySQL MCP服务器提供以下工具：

### 1. `mysql_query` - 执行SELECT查询
**用途**: 执行只读的SELECT查询
**限制**:
- 最大查询长度: 4096字符
- 最大返回行数: 1000
- 查询超时: 30秒

**示例**:
```sql
-- 查询所有表
SHOW TABLES;

-- 查询指标元数据
SELECT * FROM bi_metric_metadata LIMIT 10;

-- 统计表记录数
SELECT COUNT(*) as total FROM bi_metric_metadata;
```

### 2. `mysql_execute` - 执行INSERT/UPDATE/DELETE
**用途**: 执行数据修改操作
**特性**:
- 支持参数化查询
- 自动事务处理
- 返回影响行数和插入ID

**示例**:
```sql
-- 插入数据
INSERT INTO bi_metric_metadata (metric_code, metric_name, status)
VALUES ('TEST_METRIC', '测试指标', '0');

-- 更新数据
UPDATE bi_metric_metadata SET status = '1' WHERE metric_code = 'TEST_METRIC';

-- 删除数据
DELETE FROM bi_metric_metadata WHERE metric_code = 'TEST_METRIC';
```

### 3. `list_tables` - 列出所有表
**用途**: 列出当前数据库的所有表
**无需参数**

### 4. `describe_table` - 查看表结构
**用途**: 显示表的结构信息
**参数**: `table` (表名)

---

## 💡 使用示例

### 通过MCP与数据库交互

1. **查看所有表**
   ```
   "列出iras数据库中的所有表"
   ```

2. **查看表结构**
   ```
   "显示bi_metric_metadata表的结构"
   ```

3. **查询数据**
   ```
   "查询前10条指标元数据"
   ```

4. **统计数据**
   ```
   "统计bi_metric_metadata表中有多少条记录"
   ```

5. **复杂查询**
   ```
   "查询所有状态为正常的指标，按创建时间降序排列"
   ```

---

## 🔍 测试连接

### 测试步骤

1. **确认MySQL服务运行**
   ```bash
   # Windows
   net start MySQL

   # 或检查MySQL进程
   tasklist | findstr mysql
   ```

2. **测试数据库连接**
   ```bash
   # 使用MySQL客户端（如果已安装）
   mysql -u root -p iras

   # 输入密码后，执行：
   SHOW TABLES;
   ```

3. **通过MCP测试**
   - 启动MCP服务器
   - 在MCP客户端调用 `list_tables` 工具
   - 应该看到iras数据库的所有表

---

## ⚠️ 安全建议

### 1. 使用只读用户（推荐）

为MCP创建一个只读用户：

```sql
-- 创建只读用户
CREATE USER 'mcp_user'@'localhost' IDENTIFIED BY 'secure_password';

-- 授予iras数据库的只读权限
GRANT SELECT ON iras.* TO 'mcp_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

然后更新 `.mcp.json`:
```json
{
  "env": {
    "MYSQL_USER": "mcp_user",
    "MYSQL_PASS": "secure_password"
  }
}
```

### 2. 限制查询结果

MCP服务器已内置限制：
- SELECT查询最多返回1000行
- 查询超时30秒
- 最大查询长度4096字符

### 3. 定期审计日志

定期检查数据库操作日志，确保只有授权操作被执行。

---

## 🐛 故障排查

### 问题1: MCP服务器无法启动

**症状**: 启动MCP服务器时出错

**解决方案**:
1. 检查Node.js版本 (需要18+)
   ```bash
   node --version
   ```

2. 重新安装MCP服务器
   ```bash
   npm uninstall -g @kevinwatt/mysql-mcp
   npm install -g @kevinwatt/mysql-mcp
   ```

### 问题2: 无法连接到数据库

**症状**: "Connection refused" 或 "Access denied"

**解决方案**:
1. 确认MySQL服务运行
   ```bash
   net start MySQL
   ```

2. 验证用户名和密码
   ```bash
   mysql -u root -p iras
   ```

3. 检查MySQL端口
   ```bash
   netstat -an | findstr 3306
   ```

### 问题3: 查询超时

**症状**: 查询执行超过30秒

**解决方案**:
1. 优化SQL查询
2. 添加适当的索引
3. 使用WHERE条件限制结果集

---

## 📚 相关资源

- **MCP官方文档**: https://modelcontextprotocol.io
- **MySQL MCP仓库**: https://github.com/kevinwatt/mysql-mcp
- **IRAS项目文档**: `CLAUDE.md`

---

## ✅ 检查清单

使用前请确认：

- [ ] MySQL服务正在运行
- [ ] iras数据库已创建
- [ ] 数据库用户名和密码正确
- [ ] `.mcp.json` 配置文件已更新
- [ ] 已设置适当的数据库权限

---

**配置完成时间**: 2025-02-26
**MCP服务器版本**: @kevinwatt/mysql-mcp@0.1.3
**状态**: ✅ 已安装并配置
