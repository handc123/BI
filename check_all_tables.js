const mysql = require('mysql2/promise');

async function checkTables() {
  const connection = await mysql.createConnection({
    host: 'localhost',
    port: 3306,
    user: 'root',
    password: '123456',
    database: 'iras'
  });

  try {
    const [rows] = await connection.query(`
      SELECT TABLE_NAME
      FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = 'iras'
        AND (
          TABLE_NAME = 'bi_metric_metadata'
          OR TABLE_NAME = 'bi_metric_lineage'
          OR TABLE_NAME = 'bi_lineage_node'
          OR TABLE_NAME = 'bi_lineage_edge'
          OR TABLE_NAME = 'bi_metric_data_query'
        )
      ORDER BY TABLE_NAME
    `);

    console.log('期望创建的5个表:\n');
    const expected = [
      'bi_metric_metadata',
      'bi_metric_lineage',
      'bi_lineage_node',
      'bi_lineage_edge',
      'bi_metric_data_query'
    ];

    expected.forEach((tableName, index) => {
      const exists = rows.some(row => row.TABLE_NAME === tableName);
      const status = exists ? '[OK]' : '[MISSING]';
      console.log(`${status} ${index + 1}. ${tableName}`);
    });

    console.log(`\n实际创建: ${rows.length} 个表`);
    console.log(`缺失: ${5 - rows.length} 个表`);

  } finally {
    await connection.end();
  }
}

checkTables();
