-- BI平台菜单数据
-- 注意：执行此脚本前，请确保已执行 bi_platform_schema.sql 创建相关表结构

-- 菜单 ID 说明：
-- 3000: BI平台（一级菜单）
-- 3001: BI平台首页
-- 3010: 数据源管理
-- 3020: 数据集管理
-- 3040: 仪表板管理（包含图表创建功能）

-- ========================================
-- 1. 插入BI平台一级菜单
-- ========================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3000, 'BI平台', 0, 5, 'bi', NULL, 1, 0, 'M', '0', '0', NULL, 'chart', 'admin', SYSDATE(), '', NULL, 'BI报表平台');

-- ========================================
-- 2. 插入BI平台首页菜单
-- ========================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3001, 'BI首页', 3000, 1, 'index', 'bi/index', 1, 0, 'C', '0', '0', 'bi:index:view', 'dashboard', 'admin', SYSDATE(), '', NULL, 'BI平台首页');

-- ========================================
-- 3. 插入数据源管理菜单及按钮
-- ========================================
-- 数据源管理主菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3010, '数据源管理', 3000, 2, 'datasource', 'bi/datasource/index', 1, 0, 'C', '0', '0', 'bi:datasource:list', 'server', 'admin', SYSDATE(), '', NULL, '数据源管理菜单');

-- 数据源查询按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3011, '数据源查询', 3010, 1, '#', '', 1, 0, 'F', '0', '0', 'bi:datasource:query', '#', 'admin', SYSDATE(), '', NULL, '');

-- 数据源新增按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3012, '数据源新增', 3010, 2, '#', '', 1, 0, 'F', '0', '0', 'bi:datasource:add', '#', 'admin', SYSDATE(), '', NULL, '');

-- 数据源修改按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3013, '数据源修改', 3010, 3, '#', '', 1, 0, 'F', '0', '0', 'bi:datasource:edit', '#', 'admin', SYSDATE(), '', NULL, '');

-- 数据源删除按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3014, '数据源删除', 3010, 4, '#', '', 1, 0, 'F', '0', '0', 'bi:datasource:remove', '#', 'admin', SYSDATE(), '', NULL, '');

-- 数据源导出按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3015, '数据源导出', 3010, 5, '#', '', 1, 0, 'F', '0', '0', 'bi:datasource:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- ========================================
-- 4. 插入数据集管理菜单及按钮
-- ========================================
-- 数据集管理主菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3020, '数据集管理', 3000, 3, 'dataset', 'bi/dataset/index', 1, 0, 'C', '0', '0', 'bi:dataset:list', 'table', 'admin', SYSDATE(), '', NULL, '数据集管理菜单');

-- 数据集查询按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3021, '数据集查询', 3020, 1, '#', '', 1, 0, 'F', '0', '0', 'bi:dataset:query', '#', 'admin', SYSDATE(), '', NULL, '');

-- 数据集新增按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3022, '数据集新增', 3020, 2, '#', '', 1, 0, 'F', '0', '0', 'bi:dataset:add', '#', 'admin', SYSDATE(), '', NULL, '');

-- 数据集修改按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3023, '数据集修改', 3020, 3, '#', '', 1, 0, 'F', '0', '0', 'bi:dataset:edit', '#', 'admin', SYSDATE(), '', NULL, '');

-- 数据集删除按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3024, '数据集删除', 3020, 4, '#', '', 1, 0, 'F', '0', '0', 'bi:dataset:remove', '#', 'admin', SYSDATE(), '', NULL, '');

-- 数据集导出按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3025, '数据集导出', 3020, 5, '#', '', 1, 0, 'F', '0', '0', 'bi:dataset:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- ========================================
-- 5. 插入仪表板管理菜单及按钮
-- ========================================
-- 仪表板管理主菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3040, '仪表板管理', 3000, 4, 'dashboard', 'bi/dashboard/index', 1, 0, 'C', '0', '0', 'bi:dashboard:list', 'monitor', 'admin', SYSDATE(), '', NULL, '仪表板管理菜单');

-- 仪表板查询按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3041, '仪表板查询', 3040, 1, '#', '', 1, 0, 'F', '0', '0', 'bi:dashboard:query', '#', 'admin', SYSDATE(), '', NULL, '');

-- 仪表板新增按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3042, '仪表板新增', 3040, 2, '#', '', 1, 0, 'F', '0', '0', 'bi:dashboard:add', '#', 'admin', SYSDATE(), '', NULL, '');

-- 仪表板修改按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3043, '仪表板修改', 3040, 3, '#', '', 1, 0, 'F', '0', '0', 'bi:dashboard:edit', '#', 'admin', SYSDATE(), '', NULL, '');

-- 仪表板删除按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3044, '仪表板删除', 3040, 4, '#', '', 1, 0, 'F', '0', '0', 'bi:dashboard:remove', '#', 'admin', SYSDATE(), '', NULL, '');

-- 仪表板查看按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3045, '仪表板查看', 3040, 5, '#', '', 1, 0, 'F', '0', '0', 'bi:dashboard:view', '#', 'admin', SYSDATE(), '', NULL, '');

-- 仪表板导出按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3046, '仪表板导出', 3040, 6, '#', '', 1, 0, 'F', '0', '0', 'bi:dashboard:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- 仪表板共享按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3047, '仪表板共享', 3040, 7, '#', '', 1, 0, 'F', '0', '0', 'bi:dashboard:share', '#', 'admin', SYSDATE(), '', NULL, '');

-- ========================================
-- 7. 为管理员角色分配BI平台菜单权限
-- ========================================
-- 注意：这里假设管理员角色ID为1，如果不同请修改role_id值
-- 插入角色菜单关联数据
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3001);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3010);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3011);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3012);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3013);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3014);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3015);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3020);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3021);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3022);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3023);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3024);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3025);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3040);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3041);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3042);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3043);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3044);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3045);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3046);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3047);

COMMIT;
