-- =============================================
-- 指标管理模块 - 菜单权限配置
-- 执行前请根据实际情况调整父菜单ID
-- =============================================

SET NAMES utf8mb4;

-- 查询现有的BI平台父菜单ID
-- SELECT menu_id, menu_name, parent_id FROM sys_menu WHERE menu_name LIKE '%BI%' OR menu_name LIKE '%指标%';

-- 假设父菜单ID为 2000 (请根据实际情况修改)
-- 如果没有合适的父菜单，请先创建父菜单或使用其他现有菜单

-- =============================================
-- 步骤1: 创建指标管理的一级菜单
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('指标管理', 0, 5, 'metric', NULL, 1, 0, 'M', '0', '0', NULL, 'chart', 'admin', NOW());

-- =============================================
-- 步骤2: 创建指标管理子菜单
-- =============================================

-- 获取刚创建的指标管理菜单ID作为父菜单
SET @parent_menu_id = LAST_INSERT_ID();

-- 验证父菜单ID
SELECT @parent_menu_id as '父菜单ID';

-- 2.1 指标元数据管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('指标元数据', @parent_menu_id, 1, 'metadata', 'bi/metadata/index', 1, 0, 'C', '0', '0', 'bi:metadata:list', 'chart', 'admin', NOW(), '指标元数据管理菜单');

-- 获取刚插入的指标元数据菜单ID
SET @metadata_menu_id = LAST_INSERT_ID();

-- 2.2 指标元数据按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('指标查询', @metadata_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'bi:metadata:query', '#', 'admin', NOW(), ''),
('指标新增', @metadata_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'bi:metadata:add', '#', 'admin', NOW(), ''),
('指标修改', @metadata_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'bi:metadata:edit', '#', 'admin', NOW(), ''),
('指标删除', @metadata_menu_id, 4, '', '', 1, 0, 'F', '0', '0', 'bi:metadata:remove', '#', 'admin', NOW(), ''),
('指标导出', @metadata_menu_id, 5, '', '', 1, 0, 'F', '0', '0', 'bi:metadata:export', '#', 'admin', NOW(), '');

-- 2.3 指标血缘管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('指标血缘', @parent_menu_id, 2, 'lineage', 'bi/lineage/index', 1, 0, 'C', '0', '0', 'bi:lineage:list', 'tree', 'admin', NOW(), '指标血缘管理菜单');

SET @lineage_menu_id = LAST_INSERT_ID();

-- 2.4 指标血缘按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('血缘查询', @lineage_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'bi:lineage:query', '#', 'admin', NOW(), ''),
('血缘新增', @lineage_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'bi:lineage:add', '#', 'admin', NOW(), ''),
('血缘删除', @lineage_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'bi:lineage:remove', '#', 'admin', NOW(), '');

-- 2.5 指标数据查询菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('指标数据查询', @parent_menu_id, 3, 'data', 'bi/data/index', 1, 0, 'C', '0', '0', 'bi:metric:data', 'search', 'admin', NOW(), '指标数据查询菜单');

SET @data_menu_id = LAST_INSERT_ID();

-- 2.6 指标数据查询按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('数据查询', @data_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'bi:metric:query', '#', 'admin', NOW(), ''),
('数据导出', @data_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'bi:metric:export', '#', 'admin', NOW(), '');

-- =============================================
-- 步骤3: 验证菜单是否创建成功
-- =============================================

SELECT
    menu_id,
    menu_name,
    parent_id,
    order_num,
    path,
    perms,
    menu_type
FROM sys_menu
WHERE parent_id = @parent_menu_id
   OR menu_id IN (@metadata_menu_id, @lineage_menu_id, @data_menu_id)
ORDER BY parent_id, order_num;

-- =============================================
-- 步骤4: 为超级管理员角色分配权限（如果需要）
-- =============================================

-- 查询超级管理员角色ID（通常为1）
-- SELECT role_id, role_name, role_key FROM sys_role WHERE role_key = 'admin';

-- 假设超级管理员角色ID为1
SET @admin_role_id = 1;

-- 为超级管理员分配所有指标管理权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @admin_role_id, menu_id
FROM sys_menu
WHERE menu_name LIKE '%指标%'
   OR perms LIKE 'bi:%'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- =============================================
-- 说明
-- =============================================

/*
权限说明：

【指标元数据权限】
- bi:metadata:list    查看指标元数据列表
- bi:metadata:query   查询指标元数据详情
- bi:metadata:add     新增指标元数据
- bi:metadata:edit    修改指标元数据
- bi:metadata:remove  删除指标元数据
- bi:metadata:export  导出指标元数据

【指标血缘权限】
- bi:lineage:list     查看指标血缘列表
- bi:lineage:query    查询指标血缘详情
- bi:lineage:add      新增指标血缘
- bi:lineage:remove   删除指标血缘

【指标数据权限】
- bi:metric:data      查询指标数据
- bi:metric:query     查询指标详情
- bi:metric:export    导出指标数据

菜单类型说明：
- M: 目录
- C: 菜单
- F: 按钮

状态说明：
- 0: 正常
- 1: 停用

使用说明：
1. 执行前请修改 @parent_menu_id 为实际的父菜单ID
2. 如果需要创建新的父菜单，请取消注释步骤1中的SQL
3. 执行后可以通过步骤3的查询验证菜单是否创建成功
4. 步骤4会自动为超级管理员分配权限，其他角色需要手动分配
*/
