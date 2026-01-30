# 文件数据源上传功能使用指南

## 功能概述

文件数据源上传功能允许用户上传CSV或Excel文件作为临时数据源，用于即席分析和数据探索。该功能自动检测列名和数据类型，无需手动配置。

## 支持的文件格式

- **CSV**: 逗号分隔值文件 (.csv)
- **Excel 97-2003**: 旧版Excel文件 (.xls)
- **Excel 2007+**: 新版Excel文件 (.xlsx)

## 文件限制

- **最大文件大小**: 50MB
- **编码**: CSV文件使用UTF-8编码
- **表头**: 第一行必须是列名
- **工作表**: Excel文件仅读取第一个工作表

## 数据类型检测

系统会自动分析前100行数据，检测每列的数据类型：

| 类型 | 检测规则 | 示例 |
|------|---------|------|
| INTEGER | 可解析为整数 | 1, 100, -50 |
| DECIMAL | 可解析为小数 | 3.14, 100.50, -0.5 |
| BOOLEAN | true/false、是/否、1/0 | true, false, 是, 否 |
| DATE | 常见日期格式 | 2024-01-15, 2024/01/15 |
| STRING | 默认类型 | 任何文本 |

**检测阈值**: 如果某列超过80%的样本符合某种类型，则判定为该类型，否则默认为STRING。

## API接口

### 1. 上传文件

**端点**: `POST /bi/datasource/file/upload`

**权限**: `bi:datasource:add`

**请求参数**:
- `file` (必需): 上传的文件
- `name` (可选): 数据源名称，默认使用文件名
- `remark` (可选): 备注说明

**请求示例**:
```bash
curl -X POST http://localhost:8080/bi/datasource/file/upload \
  -H "Authorization: Bearer {token}" \
  -F "file=@sales_data.csv" \
  -F "name=2024年销售数据" \
  -F "remark=第一季度销售数据"
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "dataSourceId": 1,
    "fileName": "sales_data.csv",
    "fileSize": 102400,
    "rowCount": 1000,
    "columns": [
      {
        "name": "id",
        "type": "INTEGER",
        "index": 0
      },
      {
        "name": "product_name",
        "type": "STRING",
        "index": 1
      },
      {
        "name": "price",
        "type": "DECIMAL",
        "index": 2
      },
      {
        "name": "sale_date",
        "type": "DATE",
        "index": 3
      }
    ],
    "previewData": [
      ["1", "产品A", "99.99", "2024-01-15"],
      ["2", "产品B", "149.50", "2024-01-16"]
    ]
  }
}
```

### 2. 预览文件数据

**端点**: `GET /bi/datasource/file/{id}/preview`

**权限**: `bi:datasource:query`

**请求参数**:
- `id` (路径参数): 数据源ID
- `pageNum` (查询参数): 页码，默认1
- `pageSize` (查询参数): 每页大小，默认10

**请求示例**:
```bash
curl -X GET "http://localhost:8080/bi/datasource/file/1/preview?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    ["1", "产品A", "99.99", "2024-01-15"],
    ["2", "产品B", "149.50", "2024-01-16"],
    ["3", "产品C", "199.00", "2024-01-17"]
  ]
}
```

### 3. 删除文件数据源

**端点**: `DELETE /bi/datasource/file/{id}`

**权限**: `bi:datasource:remove`

**请求参数**:
- `id` (路径参数): 数据源ID

**请求示例**:
```bash
curl -X DELETE http://localhost:8080/bi/datasource/file/1 \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "操作成功"
}
```

### 4. 清理未使用文件

**端点**: `POST /bi/datasource/file/cleanup`

**权限**: `bi:datasource:remove`

**功能**: 清理超过90天未使用的文件数据源

**请求示例**:
```bash
curl -X POST http://localhost:8080/bi/datasource/file/cleanup \
  -H "Authorization: Bearer {token}"
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "成功清理 5 个未使用的文件数据源"
}
```

## 错误处理

### 常见错误

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| 上传文件不能为空 | 未选择文件 | 选择有效的文件 |
| 文件大小超过限制 | 文件超过50MB | 压缩文件或分割数据 |
| 不支持的文件格式 | 文件格式不是CSV/Excel | 转换为支持的格式 |
| CSV文件解析失败 | CSV格式错误 | 检查文件格式和编码 |
| Excel文件解析失败 | Excel文件损坏 | 使用Excel修复工具 |
| 数据源不存在 | ID无效 | 检查数据源ID |
| 不是文件类型数据源 | 操作了非文件数据源 | 确认数据源类型 |

### 错误响应格式

```json
{
  "code": 500,
  "msg": "文件解析失败: 第5行第3列数据格式错误",
  "data": null
}
```

## 使用场景

### 场景1: 快速数据探索

用户有一份Excel销售数据，想快速查看数据分布和趋势：

1. 上传Excel文件
2. 系统自动检测列类型
3. 创建临时数据源
4. 在BI平台中创建可视化分析

### 场景2: 数据对比分析

用户需要将外部CSV数据与数据库数据进行对比：

1. 上传CSV文件作为临时数据源
2. 创建数据集关联文件数据源
3. 创建JOIN数据集连接文件和数据库
4. 在仪表板中对比分析

### 场景3: 临时报表生成

用户需要为特定会议生成一次性报表：

1. 上传会议数据文件
2. 快速创建可视化组件
3. 生成仪表板
4. 会议后删除临时数据源

## 最佳实践

### 1. 文件准备

**CSV文件**:
```csv
id,name,amount,date
1,张三,1000.50,2024-01-15
2,李四,2000.75,2024-01-16
3,王五,1500.00,2024-01-17
```

- 第一行必须是列名
- 使用UTF-8编码
- 避免特殊字符
- 日期使用标准格式

**Excel文件**:
- 数据放在第一个工作表
- 第一行是列名
- 避免合并单元格
- 避免复杂公式

### 2. 数据类型优化

为了获得更准确的类型检测：

- **整数列**: 确保所有值都是整数
- **小数列**: 使用统一的小数位数
- **日期列**: 使用统一的日期格式（推荐yyyy-MM-dd）
- **布尔列**: 使用true/false或1/0

### 3. 性能优化

- **大文件**: 考虑分割为多个小文件
- **频繁使用**: 转换为数据库数据源
- **定期清理**: 删除不再使用的文件数据源

### 4. 安全建议

- **敏感数据**: 避免上传包含敏感信息的文件
- **临时性**: 文件数据源仅用于临时分析
- **定期清理**: 使用cleanup接口定期清理旧文件
- **权限控制**: 确保只有授权用户可以上传文件

## 配置说明

### application.yml配置

```yaml
# 文件上传配置
ruoyi:
  # 文件上传路径
  profile: D:/ruoyi/uploadPath  # Windows
  # profile: /home/ruoyi/uploadPath  # Linux

spring:
  servlet:
    multipart:
      # 单个文件大小限制
      max-file-size: 50MB
      # 请求总大小限制
      max-request-size: 50MB
```

### 存储路径结构

```
{uploadPath}/
└── bi/
    └── files/
        └── 2024/
            └── 01/
                └── 15/
                    ├── 1234567890_sales.csv
                    ├── 1234567891_products.xlsx
                    └── 1234567892_customers.xls
```

## 监控和维护

### 定期任务

建议配置定时任务定期清理未使用的文件：

```java
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
public void cleanupUnusedFiles() {
    fileUploadService.cleanupUnusedFileSources();
}
```

### 监控指标

- 文件数据源总数
- 文件总大小
- 平均文件大小
- 最近上传时间
- 清理的文件数量

### 日志查看

```bash
# 查看上传日志
grep "文件数据源创建成功" logs/iras.log

# 查看清理日志
grep "清理未使用的文件数据源" logs/iras.log

# 查看错误日志
grep "文件上传失败\|文件解析失败" logs/iras.log
```

## 故障排查

### 问题1: 文件上传失败

**症状**: 上传文件时返回500错误

**排查步骤**:
1. 检查文件大小是否超过限制
2. 检查文件格式是否支持
3. 检查上传目录是否有写权限
4. 查看后端日志获取详细错误

### 问题2: 类型检测不准确

**症状**: 数字列被识别为字符串

**原因**: 列中包含非数字值或格式不统一

**解决方案**:
1. 清理数据，确保列中所有值类型一致
2. 对于混合类型列，接受STRING类型
3. 在数据集中手动配置字段类型

### 问题3: 预览数据为空

**症状**: 预览接口返回空数组

**排查步骤**:
1. 检查文件是否存在
2. 检查文件路径是否正确
3. 检查文件是否损坏
4. 查看后端日志

### 问题4: 文件无法删除

**症状**: 删除数据源后文件仍然存在

**原因**: 文件被其他进程占用或权限不足

**解决方案**:
1. 检查文件是否被其他程序打开
2. 检查文件权限
3. 手动删除文件

## 技术实现

### 核心技术栈

- **CSV解析**: Apache Commons CSV 1.10.0
- **Excel解析**: Apache POI 4.1.2
- **文件存储**: 本地文件系统
- **数据库**: MySQL (元数据存储)

### 关键类

- `FileUploadServiceImpl`: 文件上传服务实现
- `FileUploadResult`: 上传结果DTO
- `DataSourceController`: REST API控制器

### 扩展点

如需扩展功能，可以：

1. **添加新文件格式**: 实现新的解析方法
2. **自定义类型检测**: 修改`detectColumnType`方法
3. **云存储支持**: 替换文件存储实现
4. **异步处理**: 大文件使用异步上传

## 常见问题 (FAQ)

**Q: 支持哪些日期格式？**
A: 支持常见格式如yyyy-MM-dd、yyyy/MM/dd、dd-MM-yyyy等。

**Q: CSV文件编码问题怎么办？**
A: 确保CSV文件使用UTF-8编码，可以用记事本另存为UTF-8格式。

**Q: Excel文件有多个工作表怎么办？**
A: 系统只读取第一个工作表，如需其他工作表，请单独保存为新文件。

**Q: 文件数据源可以用于生产环境吗？**
A: 不建议。文件数据源标记为临时，仅用于即席分析和测试。

**Q: 如何提高大文件上传速度？**
A: 可以压缩文件、优化网络、或考虑分批上传。

**Q: 文件数据源会自动删除吗？**
A: 超过90天未使用的文件会在执行cleanup操作时被删除。

## 更新日志

### v1.0.0 (2024-01-15)
- ✨ 初始版本发布
- ✅ 支持CSV和Excel文件上传
- ✅ 自动数据类型检测
- ✅ 文件预览功能
- ✅ 自动清理机制
- ✅ 完整的错误处理
- ✅ 单元测试覆盖

## 联系支持

如有问题或建议，请联系：
- 技术支持: support@zjrcu.com
- 文档反馈: docs@zjrcu.com
