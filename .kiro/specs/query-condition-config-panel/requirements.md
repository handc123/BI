# 需求文档

## 简介

本文档规定了查询条件配置面板的需求,该面板使用户能够为 IRAS Smart BI 系统中的仪表板组件配置查询参数。该面板提供了一个全面的界面,用于管理查询条件、将其链接到数据字段以及配置其显示行为和默认值。

## 术语表

- **查询条件(Query_Condition)**: 用于过滤或约束仪表板组件中显示数据的可配置参数
- **条件映射(Condition_Mapping)**: 查询条件与特定数据库表字段之间的关联
- **显示类型(Display_Type)**: 用于呈现查询条件的 UI 控件类型(例如:时间选择器、下拉框、文本输入)
- **时间粒度(Time_Granularity)**: 基于时间的条件的精度级别(年、月、日、小时等)
- **配置面板(Configuration_Panel)**: 用于管理查询条件设置的 UI 界面
- **仪表板组件(Dashboard_Component)**: 显示数据的可视化元素(图表、表格等),可以通过查询条件进行过滤
- **字段关联(Field_Linking)**: 将查询条件与特定数据库表列关联的过程
- **自动模式(Auto_Mode)**: 系统从关联字段推断设置的自动配置模式
- **自定义模式(Custom_Mode)**: 用户显式定义所有设置的手动配置模式

## Requirements

### 需求 1: 查询条件列表管理

**用户故事:** 作为仪表板设计者,我希望管理查询条件列表,以便组织和控制仪表板组件可用的过滤参数。

#### 验收标准

1. WHEN 配置面板(Configuration_Panel)打开时, THE 系统(System) SHALL 在左侧列表面板中显示所有现有查询条件
2. WHEN 用户点击添加按钮时, THE 系统(System) SHALL 创建新的查询条件并将其添加到列表中
3. WHEN 用户从列表中选择查询条件时, THE 系统(System) SHALL 在中间和右侧面板中显示其配置详情
4. WHEN 用户删除查询条件时, THE 系统(System) SHALL 从列表中移除它并删除所有关联的条件映射
5. WHEN 用户在列表中重新排序查询条件时, THE 系统(System) SHALL 持久化新的顺序

### 需求 2: 字段关联配置

**用户故事:** 作为仪表板设计者,我希望将查询条件链接到特定的数据库字段,以便条件可以从正确的表和列过滤数据。

#### 验收标准

1. WHEN 选择查询条件时, THE 系统(System) SHALL 在中间面板中显示可用的表和字段
2. WHEN 用户选择表时, THE 系统(System) SHALL 加载并显示该表的所有字段
3. WHEN 用户勾选"全部"(所有字段)复选框时, THE 系统(System) SHALL 选择当前表的所有字段
4. WHEN 用户取消勾选"全部"复选框时, THE 系统(System) SHALL 取消选择当前表的所有字段
5. WHEN 用户选择单个字段时, THE 系统(System) SHALL 为每个选定的字段创建条件映射
6. WHEN 用户取消选择字段时, THE 系统(System) SHALL 删除相应的条件映射

### 需求 3: 显示类型配置

**用户故事:** 作为仪表板设计者,我希望配置查询条件的显示方式,以便用户看到适合不同数据类型的 UI 控件。

#### 验收标准

1. WHEN 选择查询条件时, THE 系统(System) SHALL 在右侧面板中显示显示类型选项
2. WHEN 用户选择"自动"模式时, THE 系统(System) SHALL 从关联字段的数据类型推断显示类型
3. WHEN 用户选择"自定义"模式时, THE 系统(System) SHALL 启用手动选择显示类型
4. WHILE 处于自定义模式时, THE 系统(System) SHALL 提供显示类型选项,包括时间选择器、下拉框、文本输入、数字输入和日期范围
5. WHEN 选择显示类型时, THE 系统(System) SHALL 验证与关联字段数据类型的兼容性
6. IF 选择了不兼容的显示类型, THEN THE 系统(System) SHALL 显示警告消息

### 需求 4: 基于时间的条件配置

**用户故事:** 作为仪表板设计者,我希望配置具有特定粒度和默认范围的基于时间的查询条件,以便用户可以按适当的时间段过滤数据。

#### 验收标准

1. WHEN 选择基于时间的显示类型时, THE 系统(System) SHALL 启用时间粒度配置选项
2. WHEN 用户选择时间粒度时, THE 系统(System) SHALL 提供年、月、日、小时、分钟和秒的选项
3. WHEN 配置时间粒度时, THE 系统(System) SHALL 启用默认时间范围配置
4. WHEN 用户配置默认时间范围时, THE 系统(System) SHALL 提供预设选项,包括今天、本周、本月、本季度、本年、最近7天、最近30天和自定义范围
5. WHEN 选择自定义范围时, THE 系统(System) SHALL 允许用户指定开始和结束日期或相对偏移量
6. WHEN 设置默认时间范围时, THE 系统(System) SHALL 验证其与配置的时间粒度匹配

### 需求 5: 默认值配置

**用户故事:** 作为仪表板设计者,我希望为查询条件设置默认值,以便仪表板组件在首次加载时显示有意义的数据。

#### 验收标准

1. WHEN 配置查询条件时, THE 系统(System) SHALL 在右侧面板中提供默认值输入字段
2. WHEN 用户输入默认值时, THE 系统(System) SHALL 根据配置的显示类型验证它
3. WHEN 基于时间的条件配置了默认范围时, THE 系统(System) SHALL 使用该范围作为默认值
4. WHEN 选择下拉框显示类型时, THE 系统(System) SHALL 提供从可用选项中选择默认值的选项
5. IF 输入了无效的默认值, THEN THE 系统(System) SHALL 显示验证错误消息

### 需求 6: 配置持久化

**用户故事:** 作为仪表板设计者,我希望保存我的查询条件配置,以便它们在会话之间持久化并在查看仪表板时可用。

#### 验收标准

1. WHEN 用户点击"确认"按钮时, THE 系统(System) SHALL 验证所有查询条件配置
2. WHEN 验证通过时, THE 系统(System) SHALL 将所有查询条件及其映射保存到数据库
3. WHEN 验证失败时, THE 系统(System) SHALL 显示错误消息并阻止保存
4. WHEN 保存配置时, THE 系统(System) SHALL 将它们与当前仪表板组件关联
5. WHEN 重新打开配置面板(Configuration_Panel)时, THE 系统(System) SHALL 从数据库加载现有配置

### 需求 7: 配置验证

**用户故事:** 作为仪表板设计者,我希望系统验证我的查询条件配置,以便在保存之前识别并修复错误。

#### 验收标准

1. WHEN 用户尝试保存配置时, THE 系统(System) SHALL 验证每个查询条件具有唯一的名称
2. WHEN 验证时, THE 系统(System) SHALL 检查每个查询条件至少有一个字段映射
3. WHEN 验证基于时间的条件时, THE 系统(System) SHALL 验证已指定时间粒度
4. WHEN 验证自定义显示类型时, THE 系统(System) SHALL 确保所选类型与关联字段类型兼容
5. IF 验证失败, THEN THE 系统(System) SHALL 显示具体的错误消息,指示哪些条件存在问题
6. WHEN 所有验证通过时, THE 系统(System) SHALL 启用确认按钮

### 需求 8: 配置面板 UI 布局

**用户故事:** 作为仪表板设计者,我希望有一个组织良好的配置界面,以便我可以高效地配置查询条件而不会混淆。

#### 验收标准

1. THE 配置面板(Configuration_Panel) SHALL 显示左、中、右三列布局
2. THE 左侧面板 SHALL 占据约 25% 的宽度并显示查询条件列表
3. THE 中间面板 SHALL 占据约 40% 的宽度并显示字段关联配置
4. THE 右侧面板 SHALL 占据约 35% 的宽度并显示显示设置
5. THE 配置面板(Configuration_Panel) SHALL 在底部显示操作按钮(取消、确认)
6. WHEN 面板宽度不足时, THE 系统(System) SHALL 提供水平滚动

### 需求 9: 查询条件命名

**用户故事:** 作为仪表板设计者,我希望为查询条件分配有意义的名称,以便我可以轻松识别它们的用途。

#### 验收标准

1. WHEN 创建新查询条件时, THE 系统(System) SHALL 分配默认名称,如"查询条件1"
2. WHEN 用户点击条件名称时, THE 系统(System) SHALL 启用内联编辑
3. WHEN 用户编辑条件名称时, THE 系统(System) SHALL 验证名称不为空
4. WHEN 用户编辑条件名称时, THE 系统(System) SHALL 验证名称在当前仪表板组件内是唯一的
5. IF 输入了重复的名称, THEN THE 系统(System) SHALL 显示错误消息并阻止更改

### 需求 10: 与仪表板组件集成

**用户故事:** 作为仪表板设计者,我希望查询条件与特定的仪表板组件关联,以便每个组件可以有自己的过滤参数。

#### 验收标准

1. WHEN 为仪表板组件打开配置面板(Configuration_Panel)时, THE 系统(System) SHALL 加载该组件特定的查询条件
2. WHEN 保存查询条件时, THE 系统(System) SHALL 将它们与组件 ID 关联
3. WHEN 删除仪表板组件时, THE 系统(System) SHALL 删除所有关联的查询条件和映射
4. WHEN 复制仪表板组件时, THE 系统(System) SHALL 将其查询条件复制到新组件
5. WHEN 修改查询条件时, THE 系统(System) SHALL 仅影响当前组件
