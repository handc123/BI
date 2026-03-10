# 2026-03-10 图表单击穿透并下线指标详情设计

## 1. 目标
- 仪表板图表交互统一为“单击跳转穿透明细页”。
- 全项目下线指标详情能力，不再保留 `MetricDetailDialog` 入口或调用链。

## 2. 已确认决策
- 仅保留单击跳穿透。
- 全项目下线指标详情（不是只限查看页）。
- 下线方式为彻底删除相关组件与引用。
- 未配置可穿透指标时仅提示，不打开任何详情弹窗。

## 3. 行为定义
1. 图表点击优先解析 `metricId/metricIds`，成功则触发穿透跳转。
2. 跳转保持既有参数透传：`metricId`、`componentId`、`querySnapshot`（及便捷展示字段）。
3. 若无法获得可用指标，提示“未配置可穿透指标，请在图表配置中绑定指标”。
4. 不再出现指标详情弹窗，不再保留详情相关触发入口。

## 4. 架构与改动范围
### 4.1 ChartWidget
- `handleChartClick` 仅负责穿透相关事件触发。
- 修复 `listMetricMetadata` 导入来源，避免 `is not a function`。
- 删除与指标详情语义绑定的逻辑与文案。

### 4.2 DashboardView 与页面集成
- 删除 `MetricDetailDialog` 的导入、模板渲染、状态变量和事件处理分支。
- `handleMetricClick/handleMultiMetricClick` 保留并仅执行 `navigateToDrill`。

### 4.3 全项目清理
- 全量检索并移除 `MetricDetailDialog`、`metric-detail` 等残留引用。
- 删除无引用组件文件与冗余样式/注释。

## 5. 错误处理
- 元数据匹配失败不抛出未捕获异常，统一给用户提示。
- 参数不完整时不发起跳转，保持页面稳定。

## 6. 测试与验收
1. 单指标图表点击进入穿透页。
2. 多指标图表点击进入对应指标穿透页。
3. 无指标配置时仅提示，不出现弹窗。
4. 控制台不再出现 `listMetricMetadata is not a function`。
5. 全项目无 `MetricDetailDialog` 引用。

## 7. 风险与回滚
- 风险：全项目删除可能影响隐藏入口。
- 控制：全仓搜索 + 编译检查，保证无引用残留。
- 回滚：按提交粒度回滚文档与代码变更，优先恢复 `ChartWidget` 与 `DashboardView` 的稳定路径。
