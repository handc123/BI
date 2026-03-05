@echo off
chcp 65001 >nul
echo ========================================
echo 查询表: t_cbis_t_daikuan_hzwdb
echo ========================================
echo.

REM 提示输入数据库名称
set /p DB_NAME="请输入数据库名称: "
if "%DB_NAME%"=="" (
    echo 错误: 数据库名称不能为空！
    pause
    exit /b 1
)

echo.
echo 正在连接数据库 %DB_NAME%...
echo 请输入MySQL密码...
echo.

REM 执行SQL文件
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p %DB_NAME% < query_table_full.sql

echo.
echo ========================================
echo 查询完成
echo ========================================
pause
