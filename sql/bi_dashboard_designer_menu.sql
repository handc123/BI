-- BI仪表板设计器菜单权限补充
-- 添加发布权限按钮

-- 仪表板发布按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3048, '仪表板发布', 3040, 8, '#', '', 1, 0, 'F', '0', '0', 'bi:dashboard:publish', '#', 'admin', SYSDATE(), '', NULL, '');

-- 为管理员角色分配发布权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 3048);

COMMIT;
