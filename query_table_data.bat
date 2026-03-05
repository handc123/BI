@echo off
chcp 65001 >nul
echo ========================================
echo 查询表数据: t_cbis_t_daikuan_hzwdb
echo ========================================
echo.

REM 设置MySQL路径
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

REM 提示输入数据库连接信息
set /p DB_HOST="请输入数据库主机 (默认: localhost): "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="请输入数据库端口 (默认: 3306): "
if "%DB_PORT%"=="" set DB_PORT=3306

set /p DB_USER="请输入数据库用户名 (默认: root): "
if "%DB_USER%"=="" set DB_USER=root

set /p DB_NAME="请输入数据库名称: "
if "%DB_NAME%"=="" (
    echo 错误: 数据库名称不能为空！
    pause
    exit /b 1
)

set /p LIMIT="请输入查询行数 (默认: 10): "
if "%LIMIT%"=="" set LIMIT=10

echo.
echo 正在连接数据库...
echo.

REM 执行查询
%MYSQL_PATH% -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p %DB_NAME% -e "SELECT * FROM t_cbis_t_daikuan_hzwdb LIMIT %LIMIT%;"

echo.
echo ========================================
echo 查询完成
echo ========================================
pause
