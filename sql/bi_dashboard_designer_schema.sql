-- =============================================
-- 仪表板设计器数据库表结构
-- =============================================

-- 1. 仪表板表
CREATE TABLE bi_dashboard (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仪表板ID',
  dashboard_name VARCHAR(100) NOT NULL COMMENT '仪表板名称',
  dashboard_desc VARCHAR(500) COMMENT '仪表板描述',
  theme VARCHAR(20) DEFAULT 'light' COMMENT '主题(light/dark)',
  canvas_config TEXT COMMENT '画布配置(JSON)',
  global_style TEXT COMMENT '全局样式配置(JSON)',
  status CHAR(1) DEFAULT '0' COMMENT '状态(0草稿 1已发布)',
  published_version INT DEFAULT 0 COMMENT '已发布版本号',
  create_by VARCHAR(64) COMMENT '创建者',
  create_time DATETIME COMMENT '创建时间',
  update_by VARCHAR(64) COMMENT '更新者',
  update_time DATETIME COMMENT '更新时间',
  remark VARCHAR(500) COMMENT '备注',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志(0正常 1删除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪表板表';

-- 2. 仪表板组件表
CREATE TABLE bi_dashboard_component (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '组件ID',
  dashboard_id BIGINT NOT NULL COMMENT '仪表板ID',
  component_type VARCHAR(50) NOT NULL COMMENT '组件类型(chart/query/text/media/tabs)',
  component_name VARCHAR(100) COMMENT '组件名称',
  position_x INT NOT NULL COMMENT 'X坐标',
  position_y INT NOT NULL COMMENT 'Y坐标',
  width INT NOT NULL COMMENT '宽度',
  height INT NOT NULL COMMENT '高度',
  z_index INT DEFAULT 0 COMMENT '层级',
  data_config TEXT COMMENT '数据配置(JSON)',
  style_config TEXT COMMENT '样式配置(JSON)',
  advanced_config TEXT COMMENT '高级配置(JSON)',
  create_time DATETIME COMMENT '创建时间',
  update_time DATETIME COMMENT '更新时间',
  FOREIGN KEY (dashboard_id) REFERENCES bi_dashboard(id) ON DELETE CASCADE,
  INDEX idx_dashboard_id (dashboard_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪表板组件表';

-- 3. 查询条件表
CREATE TABLE bi_query_condition (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '条件ID',
  dashboard_id BIGINT NOT NULL COMMENT '仪表板ID',
  condition_name VARCHAR(100) NOT NULL COMMENT '条件名称',
  condition_type VARCHAR(50) NOT NULL COMMENT '条件类型(time/dropdown/select/range)',
  display_order INT DEFAULT 0 COMMENT '显示顺序',
  is_required CHAR(1) DEFAULT '0' COMMENT '是否必填(0否 1是)',
  is_visible CHAR(1) DEFAULT '1' COMMENT '是否显示(0否 1是)',
  default_value VARCHAR(500) COMMENT '默认值',
  config TEXT COMMENT '条件配置(JSON:时间粒度/选项列表/范围限制等)',
  parent_condition_id BIGINT COMMENT '父条件ID(级联)',
  create_time DATETIME COMMENT '创建时间',
  update_time DATETIME COMMENT '更新时间',
  FOREIGN KEY (dashboard_id) REFERENCES bi_dashboard(id) ON DELETE CASCADE,
  FOREIGN KEY (parent_condition_id) REFERENCES bi_query_condition(id) ON DELETE SET NULL,
  INDEX idx_dashboard_id (dashboard_id),
  INDEX idx_parent_condition_id (parent_condition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='查询条件表';

-- 4. 条件映射表
CREATE TABLE bi_condition_mapping (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '映射ID',
  condition_id BIGINT NOT NULL COMMENT '条件ID',
  component_id BIGINT NOT NULL COMMENT '组件ID',
  field_name VARCHAR(100) NOT NULL COMMENT '映射字段名',
  mapping_type VARCHAR(20) DEFAULT 'auto' COMMENT '映射类型(auto/custom)',
  create_time DATETIME COMMENT '创建时间',
  FOREIGN KEY (condition_id) REFERENCES bi_query_condition(id) ON DELETE CASCADE,
  FOREIGN KEY (component_id) REFERENCES bi_dashboard_component(id) ON DELETE CASCADE,
  UNIQUE KEY uk_condition_component (condition_id, component_id, field_name),
  INDEX idx_condition_id (condition_id),
  INDEX idx_component_id (component_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='条件映射表';

-- 5. 组件模板表
CREATE TABLE bi_component_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  template_desc VARCHAR(500) COMMENT '模板描述',
  component_type VARCHAR(50) NOT NULL COMMENT '组件类型',
  template_config TEXT NOT NULL COMMENT '模板配置(JSON)',
  create_by VARCHAR(64) COMMENT '创建者',
  create_time DATETIME COMMENT '创建时间',
  update_time DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组件模板表';
