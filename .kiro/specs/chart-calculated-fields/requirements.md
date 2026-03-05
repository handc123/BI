# 需求文档:图表计算字段

## 简介

本功能为BI平台的数据配置面板引入图表级计算字段。当前所有字段混在一起,没有区分数据集字段(来自数据源)和图表特定的计算字段。此增强功能在"公共语义层"(数据集)和"临时加工层"(图表级计算)之间建立清晰的分离,使用户能够为单个图表创建自定义指标,而不会污染共享数据集。

## 术语表

- **数据集字段(Dataset_Field)**: 源自底层数据源并在数据集级别定义的字段,在使用该数据集的所有可视化中共享
- **计算字段(Calculated_Field)**: 通过表达式创建的图表特定字段,仅存在于单个图表的范围内
- **数据配置面板(Data_Configuration_Panel)**: 用户配置数据源、字段和图表映射的UI面板
- **字段管理区(Field_Management_Area)**: 数据配置面板中显示可用字段层次结构的新区域
- **表达式(Expression)**: 组合数据集字段、运算符和函数以计算新值的公式
- **聚合方式(Aggregation_Method)**: 应用于计算字段值的统计函数(SUM、AVG、MAX、MIN、COUNT或AUTO)
- **图表作用域(Chart_Scope)**: 计算字段有效和可访问的边界

## 需求

### 需求1:字段管理区

**用户故事:** 作为BI用户,我希望看到可用字段的清晰层次结构,以便区分数据集字段和图表特定的计算字段。

#### 验收标准

1. WHEN 打开数据配置面板时,THE 系统 SHALL 在图表配置区域上方显示"字段管理"部分
2. THE 字段管理区 SHALL 将字段组织为三个类别:"维度字段"、"指标字段"和"计算字段"
3. WHEN 显示维度和指标字段时,THE 系统 SHALL 显示来自当前选定数据集的字段
4. WHEN 显示计算字段时,THE 系统 SHALL 仅显示为当前图表创建的字段,并带有"fx"图标指示器
5. THE 字段管理区 SHALL 在计算字段部分包含"+ 新建计算字段"按钮

### 需求2:计算字段创建

**用户故事:** 作为BI用户,我希望使用基于数据集字段的表达式创建计算字段,以便执行特定于我的图表的自定义数据转换。

#### 验收标准

1. WHEN 用户点击"+ 新建计算字段"时,THE 系统 SHALL 打开右侧抽屉对话框
2. THE 计算字段对话框 SHALL 显示字段名称的输入字段
3. THE 计算字段对话框 SHALL 显示带有可用数据集字段列表的表达式编辑器
4. THE 计算字段对话框 SHALL 允许用户使用数据集字段名称、算术运算符(+、-、*、/)、括号和函数构建表达式
5. THE 计算字段对话框 SHALL 提供函数面板,包含常用函数(CASE、IF、CONCAT、SUBSTRING等),以便不熟悉SQL语法的业务用户使用
6. THE 计算字段对话框 SHALL 提供聚合方式选项:自动、SUM、AVG、MAX、MIN、COUNT
7. WHEN 用户保存计算字段时,THE 系统 SHALL 验证表达式语法
8. IF 表达式无效,THEN THE 系统 SHALL 显示错误消息并阻止保存
9. WHEN 保存有效的计算字段时,THE 系统 SHALL 将其添加到字段管理区的计算字段部分

### Requirement 3: Chart Scope Isolation

**User Story:** As a BI user, I want calculated fields to exist only within the current chart, so that my temporary calculations don't affect other charts or the shared dataset.

#### Acceptance Criteria

1. THE System SHALL store calculated field definitions within the chart configuration JSON, not in the dataset table
2. WHEN a chart is loaded, THE System SHALL restore only the calculated fields defined for that specific chart
3. WHEN a different chart is selected, THE System SHALL display only that chart's calculated fields
4. THE System SHALL NOT write calculated field definitions to the database dataset schema
5. THE System SHALL NOT make calculated fields available to other charts using the same dataset

### Requirement 4: Calculated Field Usage

**User Story:** As a BI user, I want to use calculated fields in chart configurations just like dataset fields, so that I can visualize my custom metrics.

#### Acceptance Criteria

1. WHEN a calculated field exists, THE System SHALL allow users to drag it to the chart configuration area (dimensions, metrics, or filters)
2. THE System SHALL apply the calculated field's expression during query execution
3. THE System SHALL apply the specified aggregation method when the calculated field is used as a metric
4. WHEN rendering the chart, THE System SHALL compute calculated field values based on the underlying dataset field values
5. THE System SHALL display calculated field names in the chart legend and tooltips

### Requirement 5: Calculated Field Lifecycle

**User Story:** As a BI user, I want calculated fields to be copied with charts and deleted with charts, so that field management follows intuitive lifecycle rules.

#### Acceptance Criteria

1. WHEN a chart is copied, THE System SHALL copy all calculated field definitions to the new chart
2. WHEN a chart is deleted, THE System SHALL remove all associated calculated field definitions
3. WHEN a chart is exported, THE System SHALL include calculated field definitions in the export data
4. WHEN a chart is imported, THE System SHALL restore calculated field definitions from the import data
5. THE System SHALL maintain calculated field references in chart configuration when copying or moving charts

### Requirement 6: Dataset Change Warning

**User Story:** As a BI user, I want to be warned when changing datasets will affect my calculated fields, so that I don't accidentally lose my work.

#### Acceptance Criteria

1. WHEN a user attempts to change the dataset for a chart that has calculated fields, THE System SHALL display a warning dialog
2. THE Warning_Dialog SHALL state: "The current chart contains calculated fields. Changing the dataset will clear all calculated fields. Do you want to continue?"
3. THE Warning_Dialog SHALL provide "Cancel" and "Continue" buttons
4. IF the user clicks "Cancel", THEN THE System SHALL abort the dataset change operation
5. IF the user clicks "Continue", THEN THE System SHALL remove all calculated fields and proceed with the dataset change

### Requirement 7: Expression Validation

**User Story:** As a BI user, I want my calculated field expressions to be validated, so that I can catch errors before using them in charts.

#### Acceptance Criteria

1. WHEN a user enters an expression, THE System SHALL validate that all referenced field names exist in the current dataset
2. THE System SHALL validate that the expression uses only supported operators: +, -, *, /, (, )
3. THE System SHALL validate that the expression uses only supported functions: CASE, IF, CONCAT, SUBSTRING, COALESCE, NULLIF, ROUND, FLOOR, CEIL, ABS, LENGTH, UPPER, LOWER, TRIM
4. THE System SHALL validate that parentheses are balanced
5. THE System SHALL validate that function syntax is correct (proper number of arguments, correct argument types)
6. THE System SHALL validate that the expression does not contain SQL injection patterns
7. IF validation fails, THEN THE System SHALL display a specific error message indicating the validation failure reason

### Requirement 8: Calculated Field Editing and Deletion

**User Story:** As a BI user, I want to edit or delete calculated fields, so that I can refine my calculations or remove unused fields.

#### Acceptance Criteria

1. WHEN a user clicks on a calculated field in the Field Management Area, THE System SHALL open the calculated field dialog with the existing configuration
2. THE System SHALL allow users to modify the field name, expression, and aggregation method
3. WHEN a user saves changes, THE System SHALL update the calculated field definition in the chart configuration
4. THE Calculated_Field_Dialog SHALL include a "Delete" button
5. WHEN a user clicks "Delete", THE System SHALL remove the calculated field from the chart configuration and Field Management Area
6. IF a calculated field is currently used in the chart configuration, THEN THE System SHALL display a warning before deletion

### Requirement 9: Expression Builder UI

**User Story:** As a BI user, I want an intuitive interface for building expressions, so that I can create calculated fields without memorizing syntax.

#### Acceptance Criteria

1. THE Expression_Editor SHALL display a list of available dataset fields that can be clicked to insert into the expression
2. WHEN a user clicks a dataset field, THE System SHALL insert the field name at the current cursor position in the expression editor
3. THE Expression_Editor SHALL provide clickable operator buttons for +, -, *, /, (, )
4. THE Expression_Editor SHALL provide a function panel with categorized functions (Text Functions, Numeric Functions, Logical Functions, Date Functions)
5. WHEN a user clicks a function in the function panel, THE System SHALL insert the function template with placeholder arguments at the current cursor position
6. THE Function_Panel SHALL display function syntax hints and examples for each function
7. THE Expression_Editor SHALL display the expression in a multi-line text area with syntax highlighting
8. THE Expression_Editor SHALL show a real-time preview of the expression structure

### Requirement 10: Function Support

**User Story:** As a BI user, I want to use common SQL functions in my calculated fields, so that I can perform complex data transformations without writing raw SQL.

#### Acceptance Criteria

1. THE System SHALL support two types of functions: system built-in functions and database native functions
2. THE System SHALL support system built-in text functions: CONCAT, SUBSTRING, LENGTH, UPPER, LOWER, TRIM
3. THE System SHALL support system built-in numeric functions: ROUND, FLOOR, CEIL, ABS, MOD, POWER
4. THE System SHALL support system built-in logical functions: CASE, IF, COALESCE, NULLIF
5. THE System SHALL support system built-in date functions: YEAR, MONTH, DAY, DATEDIFF
6. WHEN a system built-in function is used, THE System SHALL translate it to the appropriate SQL dialect for the underlying datasource
7. WHEN a database native function is used, THE System SHALL pass it directly to the database without translation
8. THE Function_Panel SHALL provide separate tabs for "System Built-in Functions" and "Database Functions"
9. THE Database_Functions_Tab SHALL display functions specific to the current datasource type (MySQL/PostgreSQL/ClickHouse)
10. THE System SHALL provide function documentation with syntax examples in the function panel
11. THE System SHALL validate function arguments match the expected types and count for built-in functions
12. THE System SHALL NOT validate database native functions (validation happens at database execution time)

### Requirement 11: Calculated Field Persistence

**User Story:** As a system, I want to persist calculated fields within chart configurations, so that they are preserved across sessions.

#### Acceptance Criteria

1. THE System SHALL store calculated field definitions in the `config_json` column of the `bi_dashboard_component` table
2. THE Calculated_Field_Definition SHALL include: field name, expression, aggregation method, field type (dimension or metric)
3. WHEN saving a chart, THE System SHALL serialize calculated fields as part of the chart configuration JSON
4. WHEN loading a chart, THE System SHALL deserialize calculated fields from the chart configuration JSON
5. THE System SHALL maintain backward compatibility with charts that do not have calculated fields
