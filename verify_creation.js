const mysql = require('mysql2');

console.log('========================================');
console.log('验证指标血缘表的创建');
console.log('========================================\n');

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
  console.log('[OK] Connected to MySQL database!\n');

  // 查询所有bi_metric开头的表
  connection.query(`
    SELECT
      TABLE_NAME as '表名',
      TABLE_ROWS as '行数',
      CREATE_TIME as '创建时间',
      TABLE_COMMENT as '说明'
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = 'iras'
      AND TABLE_NAME LIKE 'bi_metric%'
    ORDER BY TABLE_NAME
  `, (error, results) => {
    if (error) {
      console.error('Query failed:', error.message);
      connection.end();
      process.exit(1);
    }

    console.log('--- 指标血缘表列表 ---');
    console.log(`找到 ${results.length} 个表:\n`);

    results.forEach((row, index) => {
      console.log(`${index + 1}. ${row['表名']}`);
      console.log(`   说明: ${row['说明']}`);
      console.log(`   行数: ${row['行数']}`);
      console.log(`   创建时间: ${row['创建时间']}`);
      console.log('');
    });

    // 查询菜单
    console.log('--- 指标管理菜单 ---');
    connection.query(`
      SELECT
        menu_id as '菜单ID',
        menu_name as '菜单名称',
        parent_id as '父菜单ID',
        perms as '权限标识',
        menu_type as '菜单类型'
      FROM sys_menu
      WHERE menu_name LIKE '%指标%'
         OR perms LIKE 'bi:%'
      ORDER BY parent_id, order_num
    `, (error, menus) => {
      if (error) {
        console.error('Query failed:', error.message);
      } else {
        console.log(`找到 ${menus.length} 个菜单项:\n`);
        menus.forEach((menu, index) => {
          const type = menu['菜单类型'] === 'M' ? '目录' : menu['菜单类型'] === 'C' ? '菜单' : '按钮';
          console.log(`${index + 1}. ${menu['菜单名称']} (${type})`);
          console.log(`   权限: ${menu['权限标识'] || '无'}`);
          console.log('');
        });
      }

      connection.end();
      console.log('========================================');
      console.log('[SUCCESS] 验证完成!');
      console.log('========================================');
    });
  });
});
