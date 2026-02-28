const mysql = require('mysql2');

console.log('Testing MySQL connection...\n');

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
  console.log('[OK] Connected to MySQL database!');

  connection.query('SELECT VERSION() as version', (error, results) => {
    if (error) {
      console.error('Query failed:', error.message);
      connection.end();
      process.exit(1);
    }
    console.log('[OK] MySQL Version:', results[0].version);

    connection.query('SHOW TABLES', (error, results) => {
      if (error) {
        console.error('Query failed:', error.message);
        connection.end();
        process.exit(1);
      }
      console.log('[OK] Database: iras');
      console.log('[OK] Tables count:', results.length);
      console.log('\nTables in iras database:');
      results.forEach((row, index) => {
        const tableName = Object.values(row)[0];
        console.log(`  ${index + 1}. ${tableName}`);
      });

      connection.end();
      console.log('\n[SUCCESS] MySQL connection test completed!');
      console.log('\nYou can now use the MCP server with these tools:');
      console.log('  - list_tables: List all tables');
      console.log('  - describe_table: Show table structure');
      console.log('  - mysql_query: Execute SELECT queries');
      console.log('  - mysql_execute: Execute INSERT/UPDATE/DELETE');
    });
  });
});
