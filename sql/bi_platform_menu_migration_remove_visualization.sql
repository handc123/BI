-- ========================================
-- BI平台菜单迁移脚本：移除可视化管理
-- ========================================
-- 说明：此脚本用于已部署的系统，删除可视化管理相关菜单
-- 执行时间：2026-01-20
-- 变更原因：将可视化管理功能整合到仪表板设计器中
-- ========================================

-- 1. 删除角色菜单关联（可视化管理相关）
DELETE FROM sys_role_menu WHERE menu_id IN (3030, 3031, 3032, 3033, 3034, 3035);

-- 2. 删除可视化管理菜单及其子菜单
DELETE FROM sys_menu WHERE menu_id IN (3030, 3031, 3032, 3033, 3034, 3035);

-- 3. 更新仪表板管理菜单的排序（从5改为4）
UPDATE sys_menu SET order_num = 4 WHERE menu_id = 3040;

-- 4. 验证删除结果
SELECT 
    menu_id, 
    menu_name, 
    parent_id, 
    order_num, 
    path, 
    component, 
    perms
FROM sys_menu 
WHERE parent_id = 3000 
ORDER BY order_num;

-- 预期结果：
-- 3001 | BI首页       | 3000 | 1 | index      | bi/index            | bi:index:view
-- 3010 | 数据源管理   | 3000 | 2 | datasource | bi/datasource/index | bi:datasource:list
-- 3020 | 数据集管理   | 3000 | 3 | dataset    | bi/dataset/index    | bi:dataset:list
-- 3040 | 仪表板管理   | 3000 | 4 | dashboard  | bi/dashboard/index  | bi:dashboard:list

COMMIT;

-- ========================================
-- 注意事项
-- ========================================
-- 1. 后端Visualization API保持不变，仍然可以使用
-- 2. bi_visualization表和数据保留，不删除
-- 3. 现有仪表板引用的可视化组件继续工作
-- 4. 新创建的图表组件将直接保存在仪表板的layout_config中
-- 5. 执行此脚本后，用户需要刷新浏览器才能看到菜单变化
-- ========================================
