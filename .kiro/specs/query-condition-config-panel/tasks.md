# 实现计划: 查询条件配置面板

## 概述

本实现计划将查询条件配置面板功能分解为离散的编码步骤。该功能扩展了现有的 BI 平台,添加了一个综合的配置界面,用于管理仪表板组件的查询条件。实现遵循 RuoYi 框架约定,使用 Spring Boot 后端和 Vue 2 前端。

## 任务

- [x] 1. 扩展数据库模式和实体类
  - 为 `bi_query_condition` 表添加 `component_id` 列
  - 为 `bi_condition_mapping` 表添加 `table_name` 列
  - 更新 `QueryCondition.java` 实体类添加 `componentId` 字段
  - 更新 `ConditionMapping.java` 实体类添加 `tableName` 字段
  - 更新 MyBatis XML 映射文件以包含新字段
  - _需求: 10.1, 10.2, 2.1_

- [x] 2. 实现后端 DTO 和验证逻辑
  - [x] 2.1 创建 DTO 类
    - 创建 `QueryConditionConfigDTO.java` 及其内部类
    - 创建 `ConditionOrderDTO.java` 用于排序
    - 创建 `ValidationResult.java` 和 `ValidationError.java`
    - 创建 `DatasetFieldVO.java` 用于字段信息
    - _需求: 6.1, 7.1-7.6_

  - [x] 2.2 实现配置验证服务方法
    - 在 `IQueryConditionService` 中添加 `validateConditionConfig()` 方法
    - 实现条件名称唯一性验证
    - 实现字段映射存在性验证
    - 实现时间粒度必填验证
    - 实现显示类型兼容性验证
    - 实现默认值格式验证
    - _需求: 7.1, 7.2, 7.3, 7.4, 9.3, 9.4_

  - [ ] 2.3 为验证逻辑编写属性测试

    - **属性 14: 条件名称唯一性**
    - **验证需求: 7.1, 9.4**

  - [ ]* 2.4 为验证逻辑编写单元测试
    - 测试空名称验证
    - 测试重复名称验证
    - 测试缺少映射验证
    - 测试时间粒度验证
    - _需求: 7.1-7.4, 9.3_

- [x] 3. 实现后端服务层方法
  - [x] 3.1 扩展 IQueryConditionService 接口
    - 添加 `selectConditionsByComponentId(Long componentId)` 方法
    - 添加 `saveConditionConfig(QueryConditionConfigDTO config)` 方法
    - 添加 `reorderConditions(List<ConditionOrderDTO> orders)` 方法
    - 添加 `copyConditionsToComponent(Long sourceId, Long targetId)` 方法
    - _需求: 1.1, 1.5, 6.2, 10.4_

  - [x] 3.2 实现 QueryConditionServiceImpl 方法
    - 实现 `selectConditionsByComponentId()` 查询逻辑
    - 实现 `saveConditionConfig()` 事务性保存逻辑
    - 实现 `reorderConditions()` 批量更新顺序
    - 实现 `copyConditionsToComponent()` 复制逻辑
    - 实现级联删除逻辑(删除条件时删除映射)
    - _需求: 1.1, 1.4, 1.5, 6.2, 10.3, 10.4_

  - [ ]* 3.3 为服务方法编写属性测试
    - **属性 13: 配置保存和加载往返一致性**
    - **验证需求: 6.2, 6.5**
    - **属性 19: 组件条件数据隔离**
    - **验证需求: 10.1, 10.5**
    - **属性 21: 组件复制条件复制**
    - **验证需求: 10.4**

  - [ ]* 3.4 为服务方法编写单元测试
    - 测试按组件ID查询条件
    - 测试保存配置
    - 测试删除条件级联删除映射
    - 测试复制条件到新组件
    - _需求: 1.1, 1.4, 6.2, 10.4_

- [x] 4. 实现后端 Mapper 和 XML
  - [x] 4.1 更新 QueryConditionMapper 接口
    - 添加 `selectConditionsByComponentId(Long componentId)` 方法
    - 添加 `batchUpdateDisplayOrder(List<ConditionOrderDTO> orders)` 方法
    - 添加 `selectConditionsByIds(List<Long> ids)` 方法
    - _需求: 1.1, 1.5_

  - [x] 4.2 更新 ConditionMappingMapper 接口
    - 添加 `deleteByConditionId(Long conditionId)` 方法
    - 添加 `deleteByComponentId(Long componentId)` 方法
    - 添加 `batchInsert(List<ConditionMapping> mappings)` 方法
    - _需求: 1.4, 2.5, 10.3_

  - [x] 4.3 更新 MyBatis XML 映射文件
    - 在 `QueryConditionMapper.xml` 中实现新查询
    - 在 `ConditionMappingMapper.xml` 中实现新查询
    - 添加级联删除的 SQL 语句
    - _需求: 1.1, 1.4, 1.5, 10.3_

- [x] 5. 实现后端 Controller 端点
  - [x] 5.1 创建或更新 QueryConditionController
    - 实现 `GET /bi/condition/list` 端点(按组件ID查询)
    - 实现 `POST /bi/condition/save` 端点(保存配置)
    - 实现 `DELETE /bi/condition/delete/{id}` 端点(删除条件)
    - 实现 `PUT /bi/condition/reorder` 端点(重新排序)
    - 实现 `GET /bi/condition/fields/{datasetId}` 端点(获取字段列表)
    - 实现 `POST /bi/condition/validate` 端点(验证配置)
    - 添加权限注解 `@PreAuthorize`
    - 添加日志注解 `@Log`
    - _需求: 1.1, 1.4, 1.5, 2.1, 6.1, 6.2, 7.1-7.6_

  - [ ]* 5.2 为 Controller 编写集成测试
    - 测试保存和加载配置的完整流程
    - 测试删除条件
    - 测试重新排序
    - 测试验证端点
    - _需求: 6.1, 6.2, 1.4, 1.5_

- [ ] 6. 检查点 - 确保后端测试通过
  - 确保所有后端测试通过,如有问题请询问用户。

- [x] 7. 创建前端 API 服务
  - [x] 7.1 创建 `ui/src/api/bi/condition.js`
    - 实现 `listConditions(componentId)` 函数
    - 实现 `saveConditionConfig(data)` 函数
    - 实现 `deleteCondition(id)` 函数
    - 实现 `reorderConditions(data)` 函数
    - 实现 `getDatasetFields(datasetId)` 函数
    - 实现 `validateConditionConfig(data)` 函数
    - 使用 `request` 工具进行 HTTP 调用
    - _需求: 1.1, 1.4, 1.5, 2.1, 6.1, 6.2_

- [x] 8. 实现前端子组件
  - [x] 8.1 创建 ConditionListPanel 组件
    - 创建 `ui/src/components/QueryConditionConfigPanel/ConditionListPanel.vue`
    - 实现条件列表显示(使用 el-list)
    - 实现添加按钮功能
    - 实现删除按钮功能
    - 实现条件选择功能(高亮显示选中项)
    - 实现拖拽排序功能(使用 vuedraggable)
    - 实现内联编辑条件名称
    - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 9.1, 9.2_

  - [x] 8.2 创建 FieldLinkingPanel 组件
    - 创建 `ui/src/components/QueryConditionConfigPanel/FieldLinkingPanel.vue`
    - 实现表选择器(使用 el-select)
    - 实现字段列表显示(使用 el-checkbox-group)
    - 实现"全部"复选框功能
    - 实现字段选择/取消选择功能
    - 实现字段映射状态显示
    - _需求: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [x] 8.3 创建 DisplaySettingsPanel 组件
    - 创建 `ui/src/components/QueryConditionConfigPanel/DisplaySettingsPanel.vue`
    - 实现显示模式选择(自动/自定义)
    - 实现显示类型选择器
    - 实现时间粒度选择器(条件显示)
    - 实现默认时间范围选择器(条件显示)
    - 实现自定义范围输入(条件显示)
    - 实现默认值输入字段
    - 实现验证错误显示
    - _需求: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2_

  - [ ]* 8.4 为子组件编写单元测试
    - 测试 ConditionListPanel 的添加/删除功能
    - 测试 FieldLinkingPanel 的全选功能
    - 测试 DisplaySettingsPanel 的验证逻辑
    - _需求: 1.2, 1.4, 2.3, 2.4, 3.5_

- [x] 9. 实现主配置面板组件
  - [x] 9.1 更新 QueryConditionConfig 组件
    - 更新 `ui/src/components/QueryConditionConfig/index.vue`
    - 实现三列布局(左25%、中40%、右35%)
    - 集成 ConditionListPanel 子组件
    - 集成 FieldLinkingPanel 子组件
    - 集成 DisplaySettingsPanel 子组件
    - 实现组件间数据流(props 和 events)
    - 实现加载条件配置逻辑
    - 实现保存条件配置逻辑
    - 实现验证逻辑
    - 实现错误处理和消息显示
    - 添加加载状态指示器
    - _需求: 1.1, 6.1, 6.2, 6.3, 6.4, 6.5, 7.5, 7.6, 8.1-8.6_

  - [ ]* 9.2 为主组件编写属性测试
    - **属性 2: 添加条件增加列表长度**
    - **验证需求: 1.2**
    - **属性 7: 全选往返一致性**
    - **验证需求: 2.3, 2.4**

  - [ ]* 9.3 为主组件编写单元测试
    - 测试加载条件配置
    - 测试保存条件配置
    - 测试验证逻辑
    - 测试错误处理
    - _需求: 6.1, 6.2, 6.3, 7.1-7.6_

- [ ] 10. 实现验证和错误处理
  - [ ] 10.1 实现前端验证逻辑
    - 在主组件中实现 `validateConfig()` 方法
    - 验证条件名称唯一性
    - 验证条件名称非空
    - 验证每个条件有字段映射
    - 验证时间条件有粒度
    - 验证显示类型兼容性
    - 验证默认值格式
    - _需求: 7.1, 7.2, 7.3, 7.4, 9.3, 9.4_

  - [ ] 10.2 实现错误消息显示
    - 在表单字段旁显示验证错误
    - 使用 `this.$message.error()` 显示业务错误
    - 使用 `this.$notify.error()` 显示网络错误
    - 实现错误恢复机制(撤销功能)
    - _需求: 3.6, 5.5, 6.3, 7.5, 9.5_

- [ ] 11. 集成到仪表板设计器
  - [ ] 11.1 更新 BiDashboardDesigner 组件
    - 在 `ui/src/components/BiDashboardDesigner/index.vue` 中添加"查询条件配置"按钮
    - 在组件右键菜单中添加"配置查询条件"选项
    - 传递 `componentId` 和 `dashboardId` 到配置面板
    - 处理配置保存后的刷新逻辑
    - _需求: 10.1, 10.2_

  - [ ] 11.2 实现组件删除时的级联清理
    - 在删除组件时调用后端删除条件的 API
    - 确保条件和映射都被删除
    - _需求: 10.3_

  - [ ] 11.3 实现组件复制时的条件复制
    - 在复制组件时调用后端复制条件的 API
    - 确保新组件有相同的查询条件配置
    - _需求: 10.4_

- [ ] 12. 实现显示类型自动推断
  - [ ] 12.1 创建类型推断工具函数
    - 创建 `ui/src/utils/displayTypeInference.js`
    - 实现 `inferDisplayType(fieldDataType)` 函数
    - 映射数据库类型到显示类型(DATE→time, VARCHAR→text, INT→number等)
    - _需求: 3.2_

  - [ ] 12.2 创建兼容性验证工具函数
    - 创建 `ui/src/utils/displayTypeCompatibility.js`
    - 实现 `isCompatible(displayType, fieldDataType)` 函数
    - 定义兼容性规则矩阵
    - _需求: 3.5, 7.4_

  - [ ]* 12.3 为工具函数编写属性测试
    - **属性 8: 显示类型自动推断**
    - **验证需求: 3.2**
    - **属性 9: 显示类型兼容性验证**
    - **验证需求: 3.5, 7.4**

- [ ] 13. 实现默认值处理
  - [ ] 13.1 创建默认值工具函数
    - 创建 `ui/src/utils/defaultValueHandler.js`
    - 实现 `calculateTimeRange(rangeType, granularity)` 函数
    - 实现 `validateDefaultValue(value, displayType)` 函数
    - 实现 `formatDefaultValue(value, displayType)` 函数
    - _需求: 4.4, 4.5, 5.2, 5.3_

  - [ ]* 13.2 为默认值处理编写属性测试
    - **属性 11: 默认值类型验证**
    - **验证需求: 5.2**
    - **属性 12: 时间范围作为默认值**
    - **验证需求: 5.3**

- [ ] 14. 添加样式和响应式设计
  - [ ] 14.1 实现组件样式
    - 为 QueryConditionConfigPanel 添加 SCSS 样式
    - 实现三列布局(flex 或 grid)
    - 实现响应式设计(宽度不足时水平滚动)
    - 添加拖拽视觉反馈
    - 添加选中状态高亮
    - 添加加载状态样式
    - _需求: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [ ] 14.2 实现子组件样式
    - 为 ConditionListPanel 添加样式
    - 为 FieldLinkingPanel 添加样式
    - 为 DisplaySettingsPanel 添加样式
    - 确保与 Element UI 主题一致
    - _需求: 8.1-8.6_

- [ ] 15. 检查点 - 确保所有测试通过
  - 确保所有前端和后端测试通过,如有问题请询问用户。

- [ ] 16. 端到端测试和集成验证
  - [ ]* 16.1 编写 E2E 测试
    - 测试完整的配置流程(打开→配置→保存→重新打开)
    - 测试添加和删除条件
    - 测试字段映射
    - 测试验证错误显示
    - 测试组件删除级联清理
    - 测试组件复制条件复制
    - _需求: 1.1-1.5, 2.1-2.6, 6.1-6.5, 10.1-10.5_

  - [ ] 16.2 手动测试和验证
    - 在开发环境中测试所有功能
    - 验证与现有仪表板设计器的集成
    - 验证权限控制
    - 验证错误处理
    - 验证性能(大量条件和字段)
    - _需求: 所有需求_

- [ ] 17. 文档和代码审查
  - [ ] 17.1 添加代码注释
    - 为所有公共方法添加 JSDoc/JavaDoc 注释
    - 为复杂逻辑添加内联注释
    - 为组件添加使用示例
    - _需求: 所有需求_

  - [ ] 17.2 更新 README 文档
    - 在 `ui/src/components/QueryConditionConfig/README.md` 中记录组件用法
    - 添加配置示例
    - 添加 API 文档链接
    - _需求: 所有需求_

## 注意事项

- 标记为 `*` 的任务是可选的,可以跳过以加快 MVP 开发
- 每个任务引用特定需求以确保可追溯性
- 检查点确保增量验证
- 属性测试验证通用正确性属性
- 单元测试验证特定示例和边缘情况
