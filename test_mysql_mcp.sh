#!/bin/bash
# MySQL MCP 服务器快速测试脚本

echo "================================"
echo "MySQL MCP 服务器测试"
echo "================================"
echo ""

# 检查MySQL服务
echo "1. 检查MySQL服务状态..."
tasklist | findstr "mysqld" >nul 2>&1
if %errorlevel% == 0 (
    echo "   ✅ MySQL服务正在运行"
) else (
    echo "   ❌ MySQL服务未运行"
    echo "   请启动MySQL服务: net start MySQL"
)
echo ""

# 检查Node.js
echo "2. 检查Node.js..."
node --version
echo ""

# 检查MCP服务器
echo "3. 检查MySQL MCP服务器..."
npm list -g @kevinwatt/mysql-mcp 2>nul | findstr "@kevinwatt/mysql-mcp" >nul 2>&1
if %errorlevel% == 0 (
    echo "   ✅ MySQL MCP服务器已安装"
) else (
    echo "   ❌ MySQL MCP服务器未安装"
    echo "   安装命令: npm install -g @kevinwatt/mysql-mcp"
)
echo ""

# 显示配置
echo "4. 当前MCP配置..."
if exist ".mcp.json" (
    echo "   ✅ 配置文件存在: .mcp.json"
    echo ""
    echo "   配置内容:"
    type .mcp.json
) else (
    echo "   ❌ 配置文件不存在"
)
echo ""

echo "================================"
echo "测试完成！"
echo "================================"
echo ""
echo "下一步："
echo "1. 确保 MySQL 服务正在运行"
echo "2. 在 .mcp.json 中设置正确的数据库密码"
echo "3. 通过MCP客户端连接测试"
echo ""
echo "详细文档请查看: MYSQL_MCP_SETUP_GUIDE.md"
