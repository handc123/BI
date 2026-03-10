# 生产级完整修复优化计划（冻结版）

## 1. 目标与范围

1. 目标：打通并稳定“数据源→数据集→仪表板设计→保存/修改→查看查询→穿透”全链路，达到可上线的生产级标准。
2. 范围：iras-bi（后端）+ ui（前端）+ sql（表结构/索引/配置）。
3. 核心问题：条件配置双通道、临时ID与真实ID混用、查询参数注入不一致、穿透字段协议不稳、执行层安全与权限下推不足、回归测试缺失。

---

## 2. 目标架构（必须落地）

1. 单一真源：设计态所有条件配置只落在dashboard config聚合保存。
2. ID契约统一：前端临时ID仅前端使用；后端仅接收Long真实ID。
3. 查询契约统一：params + filters + mappings在查看态稳定注入到每个图表。
4. 穿透契约统一：前端传字段名快照映射，后端只接受合法字段。
5. 安全与权限：参数化SQL + 字段白名单 + 行级权限下推。
6. 可观测与可回滚：关键接口打链路日志，所有高风险改造配特性开关。

---

## 3. 里程碑计划（两周/四周）

### M1（第1-2周）稳定性封板

1. 条件配置收敛为聚合保存（P0）
   范围：
   - `ui/src/views/bi/dashboard/designer.vue`
   - `ui/src/components/QueryConditionConfig/index.vue`
   - `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DashboardServiceImpl.java`
2. 映射ID语义修复（P0）
   范围：
   - `ui/src/components/QueryConditionConfigPanel/FieldLinkingPanel.vue`
   - `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/QueryConditionServiceImpl.java`
   - `iras-bi/src/main/resources/mapper/platform/ConditionMappingMapper.xml`
3. 查看态参数注入一致性（P0）
   范围：
   - `ui/src/views/bi/dashboard/view.vue`
   - `ui/src/utils/queryParamsInjector.js`
   - `ui/src/components/ChartWidget/index.vue`
4. 穿透字段快照协议固化（P1）
   范围：
   - `ui/src/views/bi/dashboard/drill.vue`
   - `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DrillServiceImpl.java`
5. 最小回归测试集（P0）
   范围：`iras-bi/src/test/java`新增3类链路测试（保存加载一致性、条件联动、穿透查询）。

#### M1 验收标准

1. “同一条件关联2图表”在查看页两个图都生效。
2. 无componentId不能为空、无uk_condition_component冲突。
3. 穿透无“非法字段: 数字ID”。
4. 回归测试通过后才允许发版。

---

### M2（第3-4周）生产化加固

1. 查询执行参数化改造（P0）
   范围：
   - `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/QueryExecutor.java`
2. 行级权限下推（P0）
   范围：`QueryExecutor` + 新增权限配置表（建议`dataset_permission_config`）。
3. 指标双轨穿透（P1）
   范围：
   - `ui/src/components/ChartWidget/index.vue`
   - `iras-bi/src/main/java/com/zjrcu/iras/bi/metric/service/impl/MetricMetadataServiceImpl.java`
4. 计算字段入元数据幂等治理（P1）
   - 同机构+同数据集+metricCode幂等，不重复创建。
5. 可观测性与审计（P1）
   - 保存/发布/查看/穿透加`requestId + dashboardId + componentId + conditionId`日志标签。

#### M2 验收标准

1. 注入测试全部拦截，功能查询正确。
2. 跨机构数据隔离通过。
3. 计算字段与普通字段都可穿透。
4. 发布后设计态与查看态一致率100%。

---

## 4. 实施顺序（严格依赖）

1. 先做配置通道收敛，再做ID修复。
2. 再做查看注入一致性与穿透协议。
3. M1稳定后，再动查询引擎与权限下推。
4. 最后做指标治理与可观测增强。

---

## 5. 风险与回滚

1. 风险：核心保存链路改造影响面大。  
   措施：开关`aggregate-save-only`灰度，保留旧逻辑一周观察。
2. 风险：查询引擎参数化可能引发性能波动。  
   措施：双实现并行压测，灰度10%看板流量。
3. 风险：权限下推误配导致“查不到数据”。  
   措施：权限策略支持“审计模式”先记录不拦截，再转强拦截。

---

## 6. 资源与节奏

1. 人员：FE 1、BE 2（业务/引擎）、QA 1、DBA 0.5。
2. 节奏：
   - 第1周：配置收敛+ID修复。
   - 第2周：查看/穿透修复+回归发版。
   - 第3周：参数化SQL+灰度。
   - 第4周：权限下推+指标治理+全量发布。

---

## 7. 交付物

1. 代码交付：上述模块改造PR（按Epic拆分）。
2. 数据库交付：权限配置表DDL与索引脚本。
3. 测试交付：链路回归用例与报告。
4. 运行交付：监控看板（错误率、慢查询、穿透成功率、条件命中率）。
5. 发布交付：灰度方案、回滚方案、上线检查单。
