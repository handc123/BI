-- 查询可用的父菜单
SELECT menu_id, menu_name, parent_id, order_num
FROM sys_menu
WHERE parent_id = 0 OR menu_name LIKE '%BI%' OR menu_name LIKE '%指标%'
ORDER BY parent_id, order_num
LIMIT 20;
