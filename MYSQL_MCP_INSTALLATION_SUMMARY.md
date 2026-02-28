# MySQL MCP 服务器安装总结

## ✅ 安装完成

MySQL MCP 服务器已成功安装并配置！

**安装时间**: 2025-02-26
**服务器版本**: @kevinwatt/mysql-mcp v0.1.3
**配置数据库**: iras (本地)

---

## 📁 已创建的文件

| 文件 | 位置 | 说明 |
|------|------|------|
| `.mcp.json` | 项目根目录 | MCP服务器配置文件 |
| `MYSQL_MCP_SETUP_GUIDE.md` | 项目根目录 | 完整配置和使用指南 |
| `test_mysql_mcp.bat` | 项目根目录 | Windows测试脚本 |
| `test_mysql_mcp.sh` | 项目根目录 | Linux/Mac测试脚本 |

---

## 🚀 快速开始

### 1. 设置数据库密码

编辑 `.mcp.json` 文件，设置您的MySQL密码：

```json
{
  "mcpServers": {
    "mysql-local": {
      "command": "npx",
      "args": ["-y", "@kevinwatt/mysql-mcp"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "root",
        "MYSQL_PASS": "在此处设置密码",  // ⬅️ 修改这里
        "MYSQL_DB": "iras"
      }
    }
  }
}
```

### 2. 测试连接

运行测试脚本：

```bash
# Windows
test_mysql_mcp.bat

# 或手动检查
net start MySQL  # 确保MySQL运行
mysql -u root -p iras  # 测试连接
```

### 3. 启动MCP服务器

**方法A - 通过MCP客户端** (推荐):
- 在支持MCP的应用（如Cursor、Claude Desktop）中
- 添加MCP服务器配置
- 粘贴 `.mcp.json` 的内容

**方法B - 手动启动**:
```bash
npx -y @kevinwatt/mysql-mcp
```

---

## 🎯 可用的MCP工具

安装后，您可以使用以下工具与MySQL数据库交互：

| 工具名 | 功能 | 说明 |
|--------|------|------|
| `mysql_query` | 执行SELECT查询 | 只读，最多返回1000行 |
| `mysql_execute` | 执行INSERT/UPDATE/DELETE | 支持事务和参数化查询 |
| `list_tables` | 列出所有表 | 无需参数 |
| `describe_table` | 查看表结构 | 需要表名参数 |

---

## 💡 使用示例

通过MCP与数据库对话：

```
用户: 列出iras数据库中的所有表
MCP: [调用 list_tables]
    返回: bi_metric_metadata, bi_metric_lineage, ...

用户: 显示bi_metric_metadata表的结构
MCP: [调用 describe_table, table="bi_metric_metadata"]
    返回: 字段列表、类型、键等信息

用户: 查询前5条指标元数据
MCP: [调用 mysql_query, sql="SELECT * FROM bi_metric_metadata LIMIT 5"]
    返回: 5条记录数据
```

---

## 📚 相关文档

详细配置和使用说明，请查看：

- **完整指南**: `MYSQL_MCP_SETUP_GUIDE.md`
- **IRAS项目文档**: `CLAUDE.md`

---

## ⚠️ 重要提示

### 安全建议

1. **创建只读用户**（推荐用于生产环境）
   ```sql
   CREATE USER 'mcp_user'@'localhost' IDENTIFIED BY 'secure_password';
   GRANT SELECT ON iras.* TO 'mcp_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **限制访问范围**
   - 只在受信任的网络环境中使用
   - 定期审查数据库访问日志

3. **数据备份**
   - 在执行写操作前备份数据
   - 定期备份重要数据

### 使用限制

- SELECT查询最多返回 **1000行**
- 查询超时时间为 **30秒**
- 最大查询长度 **4096字符**

---

## 🐛 遇到问题？

### 常见问题

**Q: MCP服务器无法启动？**
A: 检查Node.js版本（需要18+）: `node --version`

**Q: 无法连接到数据库？**
A:
1. 确认MySQL运行: `net start MySQL`
2. 验证密码: `mysql -u root -p iras`
3. 检查端口: `netstat -an | findstr 3306`

**Q: 查询超时？**
A: 优化SQL查询，添加WHERE条件和索引

---

## ✅ 验证清单

使用前请确认：

- [ ] MySQL服务正在运行
- [ ] iras数据库已创建
- [ ] `.mcp.json` 中已设置正确的数据库密码
- [ ] 数据库用户有足够的权限
- [ ] Node.js版本 >= 18

---

## 📞 获取帮助

- **MCP官方文档**: https://modelcontextprotocol.io
- **MySQL MCP仓库**: https://github.com/kevinwatt/mysql-mcp
- **项目文档**: 查看 `CLAUDE.md` 和 `MYSQL_MCP_SETUP_GUIDE.md`

---

**安装状态**: ✅ 完成
**下一步**: 在 `.mcp.json` 中设置数据库密码，然后开始使用！
