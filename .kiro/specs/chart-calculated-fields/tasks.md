# 实现计划:图表计算字段

## 概述

本实现计划将图表计算字段功能分解为离散的编码步骤。每个任务都基于前面的任务构建,最终实现完整的功能。实现语言为Java 17 (后端) 和 Vue.js 2.6.12 (前端)。

## 任务

- [x] 1. 后端基础设施
  - [x] 1.1 创建CalculatedFieldDTO数据传输对象
    - 定义字段: name, alias, fieldType, expression, aggregation
    - 添加验证注解
    - _需求: 11.2_
  
  - [x] 1.2 创建BuiltinFunctionConverter组件
    - 实现isBuiltinFunction方法
    - 实现convertFunction方法
    - 实现IF、CONCAT、SUBSTRING、DATEDIFF函数转换
    - _需求: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_
  
  - [x] 1.3 创建CalculatedFieldConverter组件
    - 实现convertToSQL方法
    - 集成SQLValidator验证
    - 集成ExpressionParser解析
    - 区分系统内置函数和数据库函数
    - _需求: 7.1, 7.2, 7.3, 7.6, 10.6, 10.7_
  
  - [x] 1.4 增强ExpressionParser
    - 添加extractFieldReferences方法
    - 添加extractFunctions方法
    - 添加validateFunctionSyntax方法
    - _需求: 7.1, 7.3, 7.5_

- [x] 2. 后端API端点
  - [x] 2.1 添加验证计算字段API
    - POST /api/bi/component/calculated-field/validate
    - 验证表达式语法
    - 验证字段引用
    - 验证函数调用
    - 返回SQL预览
    - _需求: 2.7, 2.8, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_
  
  - [x] 2.2 添加测试计算字段API
    - POST /api/bi/component/calculated-field/test
    - 执行SQL查询(LIMIT 10)
    - 返回样本数据
    - 返回执行时间
    - _需求: 4.2, 4.3, 4.4_
  
  - [x] 2.3 更新ComponentController保存方法
    - 支持保存calculatedFields到config_json
    - 验证计算字段配置
    - _需求: 11.1, 11.3_

- [x] 3. 前端核心组件
  - [x] 3.1 创建FieldManagementPanel组件
    - 显示维度字段、指标字段、计算字段(中文comment)
    - 实现"+ 新建计算字段"按钮
    - 实现字段拖拽功能
    - 显示计算字段的"fx"图标
    - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_
  
  - [x] 3.2 创建CalculatedFieldDialog组件
    - 实现字段名称输入(英文+中文)
    - 实现字段类型选择
    - 集成ExpressionEditor
    - 集成FunctionPanel
    - 实现聚合方式选择
    - 实现保存和验证逻辑
    - _需求: 2.1, 2.2, 2.3, 2.6, 2.7, 2.8, 2.9_
  
  - [x] 3.3 创建ExpressionEditor组件
    - 实现多行文本输入框
    - 实现可用字段列表(显示中文comment)
    - 实现运算符按钮
    - 实现字段插入功能(插入英文字段名)
    - 实现实时验证
    - _需求: 2.4, 9.1, 9.2, 9.3_
  
  - [x] 3.4 创建FunctionPanel组件
    - 实现标签切换(系统内置函数/数据库函数)
    - 实现函数分类显示
    - 实现函数模板插入
    - 实现函数语法提示
    - 根据数据源类型显示对应的数据库函数
    - _需求: 2.5, 9.4, 9.5, 9.6, 10.8, 10.9, 10.10_

- [x] 4. 前端集成
  - [x] 4.1 更新DataConfig.vue组件
    - 集成FieldManagementPanel
    - 实现数据集切换警告
    - 实现计算字段管理(添加/编辑/删除)
    - 实现计算字段持久化
    - _需求: 6.1, 6.2, 6.3, 6.4, 6.5, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_
  
  - [x] 4.2 实现计算字段拖拽到图表配置区
    - 支持拖拽到维度区、指标区、过滤区
    - 显示中文名称
    - _需求: 4.1, 4.5_
  
  - [x] 4.3 实现图表查询时包含计算字段
    - 将计算字段转换为SQL
    - 合并到数据集查询中
    - _需求: 4.2, 4.3, 4.4_

- [x] 5. 前端API服务
  - [x] 5.1 创建calculated-field.js API服务
    - validateCalculatedField方法
    - testCalculatedField方法
    - 复用现有的component API保存方法

- [x] 6. 生命周期管理
  - [x] 6.1 实现图表复制时复制计算字段
    - 复制calculatedFields数组
    - 保持引用完整性
    - _需求: 5.1, 5.5_
  
  - [x] 6.2 实现图表删除时删除计算字段
    - 自动删除(存储在config_json中)
    - _需求: 5.2_
  
  - [x] 6.3 实现图表导出时包含计算字段
    - 序列化calculatedFields
    - _需求: 5.3_
  
  - [x] 6.4 实现图表导入时恢复计算字段
    - 反序列化calculatedFields
    - 验证字段引用
    - _需求: 5.4_

- [x] 7. 作用域隔离
  - [x] 7.1 实现计算字段存储在图表config_json中
    - 不写入数据集表
    - _需求: 3.1, 3.4, 11.1_
  
  - [x] 7.2 实现加载图表时仅加载该图表的计算字段
    - 从config_json反序列化
    - _需求: 3.2, 3.3, 3.5_
  
  - [x] 7.3 实现向后兼容性
    - 处理不包含calculatedFields的旧版本图表
    - _需求: 11.5_

- [ ] 8. Checkpoint - 核心功能验证
  - 验证计算字段创建、编辑、删除流程
  - 验证表达式验证和SQL生成
  - 验证作用域隔离
  - 确保所有测试通过,询问用户是否有问题

- [ ] 9. 测试实现
  - [ ]* 9.1 后端单元测试
    - BuiltinFunctionConverter测试
    - CalculatedFieldConverter测试
    - ExpressionParser增强功能测试
    - _需求: 所有后端需求_
  
  - [ ]* 9.2 后端属性测试
    - 属性1: 作用域隔离
    - 属性2: 表达式验证完整性
    - 属性3: 函数支持完整性
    - 属性5: 持久化Round-trip
    - 属性6: SQL生成正确性
    - 属性7: 函数参数验证
    - _需求: 3.1-3.5, 7.1-7.7, 10.1-10.7, 11.3-11.4_
  
  - [ ]* 9.3 前端单元测试
    - FieldManagementPanel测试
    - CalculatedFieldDialog测试
    - ExpressionEditor测试
    - FunctionPanel测试
    - DataConfig集成测试
    - _需求: 所有前端需求_
  
  - [ ]* 9.4 端到端测试
    - 完整的创建-配置-保存-刷新流程
    - 数据集切换警告流程
    - 图表复制流程
    - _需求: 所有需求_

- [ ] 10. 文档和部署
  - [ ] 10.1 更新用户文档
    - 添加计算字段使用指南
    - 添加函数参考文档
    - 添加实际案例(不良贷款率)
  
  - [ ] 10.2 更新API文档
    - 添加新的API端点文档
    - 更新ComponentController文档
  
  - [ ] 10.3 部署准备
    - 确认无需数据库迁移
    - 准备灰度发布计划
    - 配置监控和日志

- [ ] 11. Final Checkpoint - 完整功能验证
  - 运行所有测试(单元测试+属性测试+集成测试)
  - 验证安全性(SQL注入防护)
  - 验证性能(复杂表达式、大量计算字段)
  - 验证向后兼容性
  - 确保所有测试通过,询问用户是否有问题

## 注意事项

- 任务标记为"*"的是可选的测试任务,可以根据项目进度决定是否实施
- 每个任务都引用了相关的需求编号,便于追溯
- Checkpoint任务用于确保增量验证,及时发现问题
- 测试任务使用JUnit 5 + jqwik (后端) 和 Jest + fast-check (前端)
- 所有代码必须通过getDiagnostics验证,确保无语法错误
