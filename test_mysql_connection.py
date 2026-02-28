#!/usr/bin/env python3
"""
MySQL MCP 连接测试脚本
"""

import json
import subprocess
import sys

def test_mysql_service():
    """测试MySQL服务状态"""
    print("=" * 50)
    print("MySQL 连接测试")
    print("=" * 50)
    print()

    # 1. 检查MySQL端口
    print("1. 检查MySQL端口...")
    try:
        result = subprocess.run(
            ['netstat', '-an'],
            capture_output=True,
            text=True,
            timeout=5
        )
        if ':3306' in result.stdout and 'LISTENING' in result.stdout:
            print("   ✅ MySQL 正在监听端口 3306")
        else:
            print("   ❌ MySQL 未监听端口 3306")
            return False
    except Exception as e:
        print(f"   ❌ 检查端口失败: {e}")
        return False

    print()

    # 2. 读取MCP配置
    print("2. 读取MCP配置...")
    try:
        with open('.mcp.json', 'r', encoding='utf-8') as f:
            config = json.load(f)
        mysql_config = config['mcpServers']['mysql-local']['env']
        print("   ✅ 配置文件加载成功")
        print(f"   - Host: {mysql_config['MYSQL_HOST']}")
        print(f"   - Port: {mysql_config['MYSQL_PORT']}")
        print(f"   - User: {mysql_config['MYSQL_USER']}")
        print(f"   - Database: {mysql_config['MYSQL_DB']}")
        print(f"   - Password: {'*' * len(mysql_config['MYSQL_PASS'])}")
    except Exception as e:
        print(f"   ❌ 配置文件读取失败: {e}")
        return False

    print()

    # 3. 测试MySQL连接（使用Node.js）
    print("3. 测试MySQL连接...")
    test_script = """
const mysql = require('mysql');
const connection = mysql.createConnection({
  host: 'localhost',
  port: 3306,
  user: 'root',
  password: '123456',
  database: 'iras'
});

connection.connect((err) => {
  if (err) {
    console.error('Connection failed:', err.message);
    process.exit(1);
  }
  console.log('✅ Connected to MySQL database!');

  connection.query('SELECT VERSION() as version', (error, results) => {
    if (error) {
      console.error('Query failed:', error.message);
      process.exit(1);
    }
    console.log('MySQL Version:', results[0].version);

    connection.query('SHOW TABLES', (error, results) => {
      if (error) {
        console.error('Query failed:', error.message);
        process.exit(1);
      }
      console.log('Tables count:', results.length);
      connection.end();
      process.exit(0);
    });
  });
});
"""

    try:
        result = subprocess.run(
            ['node', '-e', test_script],
            capture_output=True,
            text=True,
            timeout=10,
            cwd='D:\\software\\nodejs\\node_global\\node_modules\\mysql'
        )
        if result.returncode == 0:
            print("   " + result.stdout.strip().replace('\n', '\n   '))
        else:
            print(f"   ❌ 连接失败: {result.stderr}")
            return False
    except Exception as e:
        print(f"   ⚠️  无法直接测试连接: {e}")
        print("   💡 请手动测试: mysql -u root -p123456 iras")

    print()

    # 4. 测试MCP服务器
    print("4. 测试MCP服务器...")
    try:
        result = subprocess.run(
            ['npx', '-y', '@kevinwatt/mysql-mcp', '--version'],
            capture_output=True,
            text=True,
            timeout=15
        )
        if 'mysql-mcp' in result.stderr or result.returncode == 0:
            print("   ✅ MCP服务器可以启动")
        else:
            print("   ⚠️  MCP服务器响应异常")
    except Exception as e:
        print(f"   ⚠️  无法验证MCP服务器: {e}")

    print()
    print("=" * 50)
    print("测试完成！")
    print("=" * 50)
    print()
    print("下一步：")
    print("1. 在MCP客户端中添加 .mcp.json 配置")
    print("2. 使用以下工具与数据库交互：")
    print("   - list_tables: 列出所有表")
    print("   - describe_table: 查看表结构")
    print("   - mysql_query: 执行SELECT查询")
    print("   - mysql_execute: 执行INSERT/UPDATE/DELETE")

    return True

if __name__ == '__main__':
    try:
        test_mysql_service()
    except KeyboardInterrupt:
        print("\n\n测试已中断")
        sys.exit(1)
    except Exception as e:
        print(f"\n\n测试出错: {e}")
        sys.exit(1)
