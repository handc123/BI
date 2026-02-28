# 设计文档

## 概述

查询条件配置面板是一个用于配置仪表板组件查询参数的综合界面。该面板允许用户创建、配置和管理查询条件,将条件链接到数据库字段,并设置显示行为和默认值。该设计基于现有的 IRAS Smart BI 平台架构,扩展了 `QueryCondition` 和 `ConditionMapping` 实体的功能。

## 架构

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                     前端层 (Vue 2.6.12)                      │
├─────────────────────────────────────────────────────────────┤
│  QueryConditionConfigPanel.vue                              │
│  ├─ ConditionList (左侧面板)                                │
│  ├─ FieldLinkingPanel (中间面板)                            │
│  └─ DisplaySettingsPanel (右侧面板)                         │
└─────────────────────────────────────────────────────────────┘
                            ↓ HTTP/REST
┌─────────────────────────────────────────────────────────────┐
│                  后端层 (Spring Boot 3.5.4)                  │
├─────────────────────────────────────────────────────────────┤
│  QueryConditionController                                    │
│  ├─ /bi/condition/list                                      │
│  ├─ /bi/condition/save                                      │
│  ├─ /bi/condition/delete/{id}                               │
│  └─ /bi/condition/reorder                                   │
├─────────────────────────────────────────────────────────────┤
│  IQueryConditionService                                      │
│  ├─ selectConditionList()                                   │
│  ├─ insertCondition()                                       │
│  ├─ updateCondition()                                       │
│  ├─ deleteCondition()                                       │
│  └─ validateConditionConfig()                               │
├─────────────────────────────────────────────────────────────┤
│  QueryConditionMapper (MyBatis)                              │
│  └─ QueryConditionMapper.xml                                │
└─────────────────────────────────────────────────────────────┘
                            ↓ JDBC
┌─────────────────────────────────────────────────────────────┐
│                    数据层 (MySQL 8.2.0)                      │
├─────────────────────────────────────────────────────────────┤
│  bi_query_condition                                          │
│  bi_condition_mapping                                        │
│  bi_dashboard_component                                      │
└─────────────────────────────────────────────────────────────┘
```

### 组件架构

```
QueryConditionConfigPanel
├─ ConditionListPanel (左侧 25%)
│  ├─ 添加按钮
│  ├─ 条件列表 (可拖拽排序)
│  └─ 删除/编辑操作
├─ FieldLinkingPanel (中间 40%)
│  ├─ 表选择器
│  ├─ 字段列表 (带"全部"复选框)
│  └─ 字段映射管理
└─ DisplaySettingsPanel (右侧 35%)
   ├─ 显示类型配置 (自动/自定义)
   ├─ 时间粒度配置
   ├─ 默认时间范围配置
   └─ 默认值配置
```

## 组件和接口

### 前端组件

#### 1. QueryConditionConfigPanel.vue

主配置面板组件,协调三个子面板。

**Props:**
```javascript
{
  visible: Boolean,           // 对话框可见性
  componentId: Number,        // 当前组件ID
  dashboardId: Number,        // 仪表板ID
  datasetId: Number          // 数据集ID (用于加载字段)
}
```

**Data:**
```javascript
{
  conditions: [],            // 查询条件列表
  selectedCondition: null,   // 当前选中的条件
  conditionMappings: [],     // 条件映射列表
  availableTables: [],       // 可用表列表
  availableFields: [],       // 可用字段列表
  loading: false,            // 加载状态
  validationErrors: []       // 验证错误
}
```

**Methods:**
```javascript
{
  loadConditions(),          // 加载条件列表
  addCondition(),            // 添加新条件
  deleteCondition(id),       // 删除条件
  selectCondition(condition), // 选择条件
  saveConditions(),          // 保存所有配置
  validateConfig(),          // 验证配置
  handleClose()              // 关闭面板
}
```

#### 2. ConditionListPanel (子组件)

左侧条件列表面板。

**Props:**
```javascript
{
  conditions: Array,         // 条件列表
  selectedCondition: Object  // 当前选中条件
}
```

**Events:**
```javascript
{
  'add-condition',           // 添加条件
  'select-condition',        // 选择条件
  'delete-condition',        // 删除条件
  'reorder-conditions'       // 重新排序
}
```

#### 3. FieldLinkingPanel (子组件)

中间字段关联面板。

**Props:**
```javascript
{
  selectedCondition: Object, // 当前选中条件
  datasetId: Number,         // 数据集ID
  mappings: Array            // 当前映射列表
}
```

**Events:**
```javascript
{
  'update-mappings',         // 更新映射
  'select-all-fields',       // 选择所有字段
  'deselect-all-fields'      // 取消选择所有字段
}
```

#### 4. DisplaySettingsPanel (子组件)

右侧显示设置面板。

**Props:**
```javascript
{
  selectedCondition: Object, // 当前选中条件
  fieldTypes: Array          // 关联字段的数据类型
}
```

**Events:**
```javascript
{
  'update-display-config',   // 更新显示配置
  'update-default-value'     // 更新默认值
}
```

### 后端接口

#### 1. QueryConditionController

**端点:**

```java
// 获取组件的查询条件列表
GET /bi/condition/list
参数: componentId (Long)
返回: List<QueryConditionVO>

// 保存查询条件配置
POST /bi/condition/save
请求体: QueryConditionConfigDTO
返回: AjaxResult

// 删除查询条件
DELETE /bi/condition/delete/{id}
参数: id (Long)
返回: AjaxResult

// 重新排序查询条件
PUT /bi/condition/reorder
请求体: List<ConditionOrderDTO>
返回: AjaxResult

// 获取数据集字段列表
GET /bi/condition/fields/{datasetId}
参数: datasetId (Long)
返回: List<DatasetFieldVO>

// 验证条件配置
POST /bi/condition/validate
请求体: QueryConditionConfigDTO
返回: ValidationResult
```

#### 2. IQueryConditionService

**方法签名:**

```java
public interface IQueryConditionService {
    // 查询条件列表
    List<QueryCondition> selectConditionList(QueryCondition condition);
    
    // 根据组件ID查询条件
    List<QueryCondition> selectConditionsByComponentId(Long componentId);
    
    // 插入条件
    int insertCondition(QueryCondition condition);
    
    // 更新条件
    int updateCondition(QueryCondition condition);
    
    // 删除条件
    int deleteCondition(Long id);
    
    // 批量保存条件配置
    int saveConditionConfig(QueryConditionConfigDTO config);
    
    // 重新排序条件
    int reorderConditions(List<ConditionOrderDTO> orders);
    
    // 验证条件配置
    ValidationResult validateConditionConfig(QueryConditionConfigDTO config);
    
    // 复制条件到新组件
    int copyConditionsToComponent(Long sourceComponentId, Long targetComponentId);
}
```

## 数据模型

### 前端数据模型

#### QueryConditionVO

```javascript
{
  id: Number,                // 条件ID
  componentId: Number,       // 组件ID
  conditionName: String,     // 条件名称
  conditionType: String,     // 条件类型 (time/dropdown/text/number/range)
  displayOrder: Number,      // 显示顺序
  isRequired: String,        // 是否必填 ('0'/'1')
  isVisible: String,         // 是否可见 ('0'/'1')
  defaultValue: String,      // 默认值
  config: {                  // 配置对象
    displayMode: String,     // 显示模式 (auto/custom)
    displayType: String,     // 显示类型 (time/dropdown/text/number/range)
    timeGranularity: String, // 时间粒度 (year/month/day/hour/minute/second)
    defaultTimeRange: String,// 默认时间范围 (today/thisWeek/thisMonth/...)
    customRangeStart: String,// 自定义范围开始
    customRangeEnd: String,  // 自定义范围结束
    dropdownOptions: Array,  // 下拉选项
    validationRules: Object  // 验证规则
  },
  mappings: [                // 字段映射列表
    {
      id: Number,
      conditionId: Number,
      componentId: Number,
      tableName: String,
      fieldName: String,
      mappingType: String    // auto/custom
    }
  ]
}
```

### 后端数据模型

#### QueryCondition (实体类)

```java
public class QueryCondition implements Serializable {
    private Long id;                    // 条件ID
    private Long componentId;           // 组件ID (新增字段)
    private Long dashboardId;           // 仪表板ID
    private String conditionName;       // 条件名称
    private String conditionType;       // 条件类型
    private Integer displayOrder;       // 显示顺序
    private String isRequired;          // 是否必填
    private String isVisible;           // 是否可见
    private String defaultValue;        // 默认值
    private String config;              // 配置JSON
    private Long parentConditionId;     // 父条件ID
    private Date createTime;            // 创建时间
    private Date updateTime;            // 更新时间
}
```

#### ConditionMapping (实体类)

```java
public class ConditionMapping implements Serializable {
    private Long id;                    // 映射ID
    private Long conditionId;           // 条件ID
    private Long componentId;           // 组件ID
    private String tableName;           // 表名 (新增字段)
    private String fieldName;           // 字段名
    private String mappingType;         // 映射类型 (auto/custom)
    private Date createTime;            // 创建时间
}
```

#### QueryConditionConfigDTO (数据传输对象)

```java
public class QueryConditionConfigDTO {
    private Long componentId;           // 组件ID
    private Long dashboardId;           // 仪表板ID
    private List<QueryConditionDTO> conditions; // 条件列表
    
    public static class QueryConditionDTO {
        private Long id;
        private String conditionName;
        private String conditionType;
        private Integer displayOrder;
        private String isRequired;
        private String isVisible;
        private String defaultValue;
        private ConditionConfigDTO config;
        private List<ConditionMappingDTO> mappings;
    }
    
    public static class ConditionConfigDTO {
        private String displayMode;
        private String displayType;
        private String timeGranularity;
        private String defaultTimeRange;
        private String customRangeStart;
        private String customRangeEnd;
        private List<DropdownOption> dropdownOptions;
        private Map<String, Object> validationRules;
    }
    
    public static class ConditionMappingDTO {
        private Long id;
        private String tableName;
        private String fieldName;
        private String mappingType;
    }
}
```

#### ValidationResult (验证结果)

```java
public class ValidationResult {
    private boolean valid;              // 是否有效
    private List<ValidationError> errors; // 错误列表
    
    public static class ValidationError {
        private String conditionId;
        private String field;
        private String message;
    }
}
```

## 正确性属性

*属性是一个特征或行为,应该在系统的所有有效执行中保持为真。属性是人类可读规范和机器可验证正确性保证之间的桥梁。*

在编写正确性属性之前,让我先使用 prework 工具分析验收标准。



### 属性 1: 条件列表加载完整性
*对于任何*组件ID,加载查询条件列表时,返回的条件应该与数据库中该组件关联的所有条件完全匹配
**验证需求: 1.1**

### 属性 2: 添加条件增加列表长度
*对于任何*初始条件列表,添加新条件后,列表长度应该增加1
**验证需求: 1.2**

### 属性 3: 选择条件显示正确配置
*对于任何*查询条件,选择该条件后,显示的配置数据应该与条件的存储数据完全匹配
**验证需求: 1.3**

### 属性 4: 删除条件级联清理
*对于任何*查询条件,删除该条件后,该条件及其所有关联的字段映射都应该从数据库中删除
**验证需求: 1.4**

### 属性 5: 条件排序持久化
*对于任何*条件列表和新的排序顺序,保存排序后重新加载应该得到相同的顺序
**验证需求: 1.5**

### 属性 6: 字段映射创建和删除
*对于任何*选定的字段集合,选择字段应该创建相应的映射记录,取消选择应该删除相应的映射记录
**验证需求: 2.5, 2.6**

### 属性 7: 全选往返一致性
*对于任何*字段列表,勾选"全部"然后取消勾选"全部"应该恢复到初始的未选中状态
**验证需求: 2.3, 2.4**

### 属性 8: 显示类型自动推断
*对于任何*字段数据类型,在自动模式下,系统应该推断出与该数据类型兼容的显示类型
**验证需求: 3.2**

### 属性 9: 显示类型兼容性验证
*对于任何*显示类型和字段数据类型的组合,如果它们不兼容,验证应该返回错误
**验证需求: 3.5, 7.4**

### 属性 10: 时间范围与粒度匹配验证
*对于任何*时间粒度和默认时间范围的组合,如果范围的精度超过粒度,验证应该返回错误
**验证需求: 4.6**

### 属性 11: 默认值类型验证
*对于任何*显示类型和默认值的组合,如果默认值不符合显示类型的格式要求,验证应该返回错误
**验证需求: 5.2**

### 属性 12: 时间范围作为默认值
*对于任何*配置了默认时间范围的时间条件,其默认值应该等于计算后的时间范围值
**验证需求: 5.3**

### 属性 13: 配置保存和加载往返一致性
*对于任何*有效的查询条件配置,保存到数据库后重新加载应该得到等价的配置数据
**验证需求: 6.2, 6.5**

### 属性 14: 条件名称唯一性
*对于任何*组件的条件集合,不应该存在两个具有相同名称的条件
**验证需求: 7.1, 9.4**

### 属性 15: 条件必须有字段映射
*对于任何*查询条件,在保存时,该条件应该至少有一个关联的字段映射
**验证需求: 7.2**

### 属性 16: 时间条件必须有粒度
*对于任何*时间类型的查询条件,其配置中应该包含有效的时间粒度设置
**验证需求: 7.3**

### 属性 17: 条件名称非空
*对于任何*查询条件,其名称不应该为空字符串或仅包含空白字符
**验证需求: 9.3**

### 属性 18: 新条件默认名称生成
*对于任何*新创建的查询条件,如果未指定名称,系统应该生成一个默认名称(如"查询条件N")
**验证需求: 9.1**

### 属性 19: 组件条件数据隔离
*对于任何*两个不同的组件ID,加载一个组件的条件不应该返回另一个组件的条件
**验证需求: 10.1, 10.5**

### 属性 20: 组件删除级联清理
*对于任何*组件ID,删除该组件后,所有关联的查询条件和字段映射都应该从数据库中删除
**验证需求: 10.3**

### 属性 21: 组件复制条件复制
*对于任何*源组件,复制到新组件后,新组件应该有与源组件等价的查询条件配置
**验证需求: 10.4**

## 错误处理

### 错误类型

1. **验证错误**
   - 条件名称为空
   - 条件名称重复
   - 缺少字段映射
   - 显示类型与字段类型不兼容
   - 时间粒度缺失
   - 时间范围与粒度不匹配
   - 默认值格式无效

2. **数据库错误**
   - 连接失败
   - 查询超时
   - 约束违反
   - 事务回滚

3. **业务逻辑错误**
   - 组件不存在
   - 数据集不存在
   - 字段不存在
   - 权限不足

### 错误处理策略

**前端错误处理:**
```javascript
// 验证错误 - 显示在表单中
{
  type: 'validation',
  field: 'conditionName',
  message: '条件名称不能为空'
}

// 业务错误 - 显示消息提示
this.$message.error('保存失败: ' + error.message)

// 网络错误 - 显示通知
this.$notify.error({
  title: '网络错误',
  message: '无法连接到服务器,请检查网络连接'
})
```

**后端错误处理:**
```java
// 验证错误
if (StringUtils.isEmpty(condition.getConditionName())) {
    throw new ServiceException("条件名称不能为空");
}

// 业务逻辑错误
if (componentService.selectComponentById(componentId) == null) {
    throw new ServiceException("组件不存在");
}

// 数据库错误
try {
    mapper.insertCondition(condition);
} catch (DataAccessException e) {
    log.error("保存查询条件失败", e);
    throw new ServiceException("保存失败,请稍后重试");
}
```

### 错误恢复

1. **自动重试**: 网络错误时自动重试最多3次
2. **事务回滚**: 保存失败时回滚所有更改
3. **数据恢复**: 提供"撤销"功能恢复到上一个有效状态
4. **错误日志**: 记录所有错误到日志系统供排查

## 测试策略

### 双重测试方法

本功能采用单元测试和基于属性的测试相结合的方法:

- **单元测试**: 验证特定示例、边缘情况和错误条件
- **属性测试**: 验证跨所有输入的通用属性
- 两者互补,共同提供全面的覆盖

### 单元测试

单元测试专注于:
- 特定示例,演示正确行为
- 组件之间的集成点
- 边缘情况和错误条件

**前端单元测试 (Jest + Vue Test Utils):**
```javascript
describe('QueryConditionConfigPanel', () => {
  it('应该在打开时加载条件列表', async () => {
    const wrapper = mount(QueryConditionConfigPanel, {
      propsData: { componentId: 1, visible: true }
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.conditions.length).toBeGreaterThan(0)
  })

  it('应该在点击添加按钮时创建新条件', async () => {
    const wrapper = mount(QueryConditionConfigPanel)
    const initialLength = wrapper.vm.conditions.length
    await wrapper.vm.addCondition()
    expect(wrapper.vm.conditions.length).toBe(initialLength + 1)
  })

  it('应该验证条件名称唯一性', () => {
    const wrapper = mount(QueryConditionConfigPanel)
    wrapper.vm.conditions = [
      { id: 1, conditionName: '条件1' },
      { id: 2, conditionName: '条件2' }
    ]
    const result = wrapper.vm.validateConfig()
    expect(result.valid).toBe(true)
    
    wrapper.vm.conditions.push({ id: 3, conditionName: '条件1' })
    const result2 = wrapper.vm.validateConfig()
    expect(result2.valid).toBe(false)
    expect(result2.errors[0].message).toContain('名称重复')
  })
})
```

**后端单元测试 (JUnit 5 + Mockito):**
```java
@SpringBootTest
class QueryConditionServiceTest {
    @Autowired
    private IQueryConditionService conditionService;
    
    @Test
    void testInsertCondition() {
        QueryCondition condition = new QueryCondition();
        condition.setComponentId(1L);
        condition.setConditionName("测试条件");
        condition.setConditionType("time");
        
        int result = conditionService.insertCondition(condition);
        assertEquals(1, result);
        assertNotNull(condition.getId());
    }
    
    @Test
    void testValidateUniqueConditionName() {
        QueryConditionConfigDTO config = new QueryConditionConfigDTO();
        config.setComponentId(1L);
        
        List<QueryConditionDTO> conditions = new ArrayList<>();
        conditions.add(createCondition("条件1"));
        conditions.add(createCondition("条件2"));
        config.setConditions(conditions);
        
        ValidationResult result = conditionService.validateConditionConfig(config);
        assertTrue(result.isValid());
        
        conditions.add(createCondition("条件1")); // 重复名称
        result = conditionService.validateConditionConfig(config);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> e.getMessage().contains("名称重复")));
    }
    
    @Test
    void testDeleteConditionCascade() {
        // 创建条件和映射
        Long conditionId = createTestCondition();
        createTestMapping(conditionId);
        
        // 删除条件
        conditionService.deleteCondition(conditionId);
        
        // 验证条件和映射都被删除
        assertNull(conditionService.selectConditionById(conditionId));
        List<ConditionMapping> mappings = 
            mappingService.selectMappingsByConditionId(conditionId);
        assertTrue(mappings.isEmpty());
    }
}
```

### 基于属性的测试

基于属性的测试使用随机生成的输入验证通用属性。

**测试库**: 
- 前端: fast-check (JavaScript)
- 后端: jqwik (Java)

**配置**: 每个属性测试运行最少 100 次迭代

**标签格式**: 
```
Feature: query-condition-config-panel, Property N: [属性文本]
```

**前端属性测试示例 (fast-check):**
```javascript
import fc from 'fast-check'

describe('Property Tests', () => {
  // Feature: query-condition-config-panel, Property 2: 添加条件增加列表长度
  it('添加条件应该增加列表长度', () => {
    fc.assert(
      fc.property(
        fc.array(fc.record({
          id: fc.integer(),
          conditionName: fc.string()
        })),
        (initialConditions) => {
          const wrapper = mount(QueryConditionConfigPanel)
          wrapper.vm.conditions = [...initialConditions]
          const initialLength = wrapper.vm.conditions.length
          
          wrapper.vm.addCondition()
          
          expect(wrapper.vm.conditions.length).toBe(initialLength + 1)
        }
      ),
      { numRuns: 100 }
    )
  })

  // Feature: query-condition-config-panel, Property 7: 全选往返一致性
  it('全选然后取消全选应该恢复初始状态', () => {
    fc.assert(
      fc.property(
        fc.array(fc.record({
          fieldName: fc.string(),
          selected: fc.boolean()
        })),
        (initialFields) => {
          const wrapper = mount(FieldLinkingPanel)
          wrapper.vm.fields = initialFields.map(f => ({ ...f }))
          const initialState = wrapper.vm.fields.map(f => f.selected)
          
          wrapper.vm.selectAllFields()
          wrapper.vm.deselectAllFields()
          
          const finalState = wrapper.vm.fields.map(f => f.selected)
          expect(finalState).toEqual(initialState)
        }
      ),
      { numRuns: 100 }
    )
  })

  // Feature: query-condition-config-panel, Property 14: 条件名称唯一性
  it('验证应该拒绝重复的条件名称', () => {
    fc.assert(
      fc.property(
        fc.array(fc.string(), { minLength: 2 }),
        (names) => {
          const conditions = names.map((name, i) => ({
            id: i,
            conditionName: name
          }))
          
          const wrapper = mount(QueryConditionConfigPanel)
          wrapper.vm.conditions = conditions
          const result = wrapper.vm.validateConfig()
          
          const uniqueNames = new Set(names)
          const hasDuplicates = uniqueNames.size < names.length
          
          expect(result.valid).toBe(!hasDuplicates)
        }
      ),
      { numRuns: 100 }
    )
  })
})
```

**后端属性测试示例 (jqwik):**
```java
@PropertyTest
class QueryConditionPropertyTest {
    @Autowired
    private IQueryConditionService conditionService;
    
    // Feature: query-condition-config-panel, Property 13: 配置保存和加载往返一致性
    @Property(tries = 100)
    void saveAndLoadRoundTrip(@ForAll("validConditionConfig") QueryConditionConfigDTO config) {
        // 保存配置
        conditionService.saveConditionConfig(config);
        
        // 重新加载
        List<QueryCondition> loaded = 
            conditionService.selectConditionsByComponentId(config.getComponentId());
        
        // 验证等价性
        assertEquals(config.getConditions().size(), loaded.size());
        for (int i = 0; i < loaded.size(); i++) {
            QueryCondition original = config.getConditions().get(i);
            QueryCondition reloaded = loaded.get(i);
            assertEquals(original.getConditionName(), reloaded.getConditionName());
            assertEquals(original.getConditionType(), reloaded.getConditionType());
            // ... 验证其他字段
        }
    }
    
    // Feature: query-condition-config-panel, Property 19: 组件条件数据隔离
    @Property(tries = 100)
    void componentDataIsolation(
        @ForAll @LongRange(min = 1, max = 1000) Long componentId1,
        @ForAll @LongRange(min = 1, max = 1000) Long componentId2
    ) {
        Assume.that(!componentId1.equals(componentId2));
        
        // 为组件1创建条件
        QueryCondition condition1 = createCondition(componentId1);
        conditionService.insertCondition(condition1);
        
        // 加载组件2的条件
        List<QueryCondition> conditions2 = 
            conditionService.selectConditionsByComponentId(componentId2);
        
        // 验证组件2的条件中不包含组件1的条件
        assertFalse(conditions2.stream()
            .anyMatch(c -> c.getId().equals(condition1.getId())));
    }
    
    // Feature: query-condition-config-panel, Property 9: 显示类型兼容性验证
    @Property(tries = 100)
    void displayTypeCompatibilityValidation(
        @ForAll("displayType") String displayType,
        @ForAll("fieldDataType") String fieldDataType
    ) {
        QueryConditionConfigDTO config = new QueryConditionConfigDTO();
        QueryConditionDTO condition = new QueryConditionDTO();
        condition.setConditionType(displayType);
        
        ConditionMappingDTO mapping = new ConditionMappingDTO();
        mapping.setFieldName("test_field");
        // 假设我们有方法获取字段类型
        
        condition.setMappings(Arrays.asList(mapping));
        config.setConditions(Arrays.asList(condition));
        
        ValidationResult result = conditionService.validateConditionConfig(config);
        
        boolean compatible = isCompatible(displayType, fieldDataType);
        assertEquals(compatible, result.isValid());
    }
    
    @Provide
    Arbitrary<QueryConditionConfigDTO> validConditionConfig() {
        return Combinators.combine(
            Arbitraries.longs().between(1, 1000),
            Arbitraries.list(validCondition())
        ).as((componentId, conditions) -> {
            QueryConditionConfigDTO config = new QueryConditionConfigDTO();
            config.setComponentId(componentId);
            config.setConditions(conditions);
            return config;
        });
    }
    
    @Provide
    Arbitrary<String> displayType() {
        return Arbitraries.of("time", "dropdown", "text", "number", "range");
    }
    
    @Provide
    Arbitrary<String> fieldDataType() {
        return Arbitraries.of("DATE", "DATETIME", "VARCHAR", "INT", "DECIMAL");
    }
}
```

### 集成测试

测试前后端集成:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class QueryConditionIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testSaveAndLoadConditionConfig() throws Exception {
        // 保存配置
        String configJson = "{ \"componentId\": 1, \"conditions\": [...] }";
        mockMvc.perform(post("/bi/condition/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(configJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
        
        // 加载配置
        mockMvc.perform(get("/bi/condition/list")
                .param("componentId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isArray())
            .andExpect(jsonPath("$.rows.length()").value(greaterThan(0)));
    }
}
```

### 测试覆盖率目标

- 单元测试代码覆盖率: ≥ 80%
- 属性测试: 每个属性至少 100 次迭代
- 集成测试: 覆盖所有主要用户流程
- E2E测试: 覆盖关键业务场景

