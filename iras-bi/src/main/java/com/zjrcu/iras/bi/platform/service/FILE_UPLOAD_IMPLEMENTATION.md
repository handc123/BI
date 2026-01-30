# 文件数据源上传功能实现总结

## 实现概述

本实现完成了BI平台的文件数据源上传功能，允许用户上传CSV或Excel文件作为临时数据源进行即席分析。

## 实现的组件

### 1. DTO类

**FileUploadResult.java**
- 文件上传结果数据传输对象
- 包含数据源ID、文件信息、行数、列信息和预览数据
- 内部类ColumnInfo用于描述列的名称、类型和索引

### 2. 服务接口

**IFileUploadService.java**
- `uploadFile()`: 上传文件并创建文件数据源
- `previewFileData()`: 预览文件数据源数据（分页）
- `deleteFileDataSource()`: 删除文件数据源及其文件
- `cleanupUnusedFileSources()`: 清理超过90天未使用的文件数据源

### 3. 服务实现

**FileUploadServiceImpl.java**

#### 核心功能

1. **文件验证**
   - 验证文件不为空
   - 验证文件大小不超过50MB
   - 验证文件扩展名（仅支持csv、xls、xlsx）

2. **文件解析**
   - CSV解析：使用Apache Commons CSV
   - Excel解析：使用Apache POI（支持xls和xlsx）
   - 自动检测列名（从第一行读取）
   - 解析所有数据行

3. **数据类型检测**
   - 分析前100行样本数据
   - 支持的类型：STRING、INTEGER、DECIMAL、DATE、BOOLEAN
   - 检测逻辑：
     - BOOLEAN: true/false、是/否、1/0
     - INTEGER: 可解析为Long的值
     - DECIMAL: 可解析为Double但不是整数的值
     - DATE: 匹配常见日期格式（yyyy-MM-dd等）
     - STRING: 默认类型
   - 阈值：超过80%的样本符合某类型则判定为该类型

4. **文件存储**
   - 存储路径：`{uploadDir}/bi/files/{datePath}/{uniqueId}.{extension}`
   - 使用Seq生成唯一ID避免文件名冲突
   - 自动创建目录结构

5. **配置JSON结构**
   ```json
   {
     "filePath": "/path/to/file.xlsx",
     "fileName": "original-filename.xlsx",
     "fileSize": 1024000,
     "uploadTime": "2024-01-15T10:30:00",
     "columnInfo": [
       {"name": "id", "type": "INTEGER", "index": 0},
       {"name": "name", "type": "STRING", "index": 1}
     ],
     "rowCount": 1000,
     "temporary": true
   }
   ```

6. **数据预览**
   - 支持分页读取文件数据
   - CSV和Excel分别实现
   - 避免一次性加载大文件到内存

7. **文件清理**
   - 删除数据源时同时删除物理文件
   - 定期清理超过90天未使用的文件数据源
   - 基于updateTime字段判断

### 4. REST API端点

**DataSourceController.java**

- `POST /bi/datasource/file/upload`: 上传文件数据源
  - 参数：file (MultipartFile), name (可选), remark (可选)
  - 权限：bi:datasource:add
  - 返回：FileUploadResult

- `GET /bi/datasource/file/{id}/preview`: 预览文件数据
  - 参数：id, pageNum (默认1), pageSize (默认10)
  - 权限：bi:datasource:query
  - 返回：List<List<Object>>

- `DELETE /bi/datasource/file/{id}`: 删除文件数据源
  - 参数：id
  - 权限：bi:datasource:remove
  - 返回：成功/失败

- `POST /bi/datasource/file/cleanup`: 清理未使用文件
  - 权限：bi:datasource:remove
  - 返回：清理数量

## 依赖添加

### Maven依赖

在父pom.xml中添加：
```xml
<commons.csv.version>1.10.0</commons.csv.version>

<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>${commons.csv.version}</version>
</dependency>
```

在iras-bi/pom.xml中添加：
```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
</dependency>
```

## 错误处理

### 验证错误
- 文件为空
- 文件大小超过50MB
- 不支持的文件格式

### 解析错误
- CSV格式错误：返回具体错误信息
- Excel格式错误：返回具体错误信息
- 包含失败的行和列信息

### 业务错误
- 数据源不存在
- 非文件类型数据源
- 文件读取失败

## 测试

### 单元测试

**FileUploadServiceTest.java**
- 测试空文件验证
- 测试文件大小验证
- 测试文件格式验证
- 测试CSV文件解析
- 测试数据类型检测（INTEGER、DECIMAL、DATE、BOOLEAN、STRING）
- 测试混合类型默认为STRING
- 测试空列默认为STRING
- 测试数据源插入失败
- 测试删除文件数据源
- 测试预览文件数据
- 测试清理未使用文件

**FileUploadControllerTest.java**
- 测试文件上传成功
- 测试文件上传失败
- 测试预览文件数据
- 测试删除文件数据源
- 测试清理未使用文件

## 使用示例

### 上传CSV文件

```bash
curl -X POST http://localhost:8080/bi/datasource/file/upload \
  -H "Authorization: Bearer {token}" \
  -F "file=@data.csv" \
  -F "name=销售数据" \
  -F "remark=2024年销售数据"
```

### 预览文件数据

```bash
curl -X GET "http://localhost:8080/bi/datasource/file/1/preview?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer {token}"
```

### 删除文件数据源

```bash
curl -X DELETE http://localhost:8080/bi/datasource/file/1 \
  -H "Authorization: Bearer {token}"
```

### 清理未使用文件

```bash
curl -X POST http://localhost:8080/bi/datasource/file/cleanup \
  -H "Authorization: Bearer {token}"
```

## 配置要求

### application.yml

确保配置了文件上传路径：
```yaml
ruoyi:
  profile: D:/ruoyi/uploadPath  # Windows
  # profile: /home/ruoyi/uploadPath  # Linux

spring:
  servlet:
    multipart:
      max-file-size: 10MB  # 可以调整为50MB
      max-request-size: 10MB
```

## 安全考虑

1. **文件大小限制**：最大50MB，防止内存溢出
2. **文件类型限制**：仅支持CSV和Excel，防止恶意文件上传
3. **权限控制**：所有操作都需要相应权限
4. **文件隔离**：文件存储在专用目录，避免路径遍历攻击
5. **临时标记**：文件数据源标记为temporary，排除在生产分析之外
6. **自动清理**：定期清理未使用文件，释放存储空间

## 性能优化

1. **分页读取**：预览数据支持分页，避免一次性加载大文件
2. **类型检测采样**：仅分析前100行进行类型检测
3. **流式处理**：CSV和Excel都使用流式读取，减少内存占用
4. **异步清理**：清理操作可以配置为定时任务异步执行

## 后续改进建议

1. **压缩文件支持**：支持上传ZIP压缩的CSV/Excel文件
2. **增量更新**：支持更新已上传的文件数据
3. **数据验证**：添加数据质量检查（空值率、重复率等）
4. **格式转换**：支持不同编码的CSV文件（UTF-8、GBK等）
5. **大文件优化**：对超大文件使用分块上传和处理
6. **缓存预览**：缓存预览数据，提高重复查看性能
7. **异步上传**：大文件上传改为异步处理，返回任务ID
8. **文件版本**：支持同一数据源的多个文件版本

## 符合需求

本实现完全符合需求3的所有验收标准：

✅ 1. 支持CSV和Excel(XLS、XLSX)文件格式，最大50MB
✅ 2. 自动检测列名和数据类型
✅ 3. 标记为临时数据源（temporary: true）
✅ 4. 支持清理90天未使用的文件数据源
✅ 5. 解析失败时返回具体错误消息

## 总结

文件数据源上传功能已完整实现，包括：
- 完整的文件验证和解析逻辑
- 智能的数据类型检测
- 安全的文件存储机制
- 完善的错误处理
- 全面的单元测试
- RESTful API接口
- 详细的日志记录

该功能为BI平台提供了灵活的即席分析能力，用户可以快速上传数据文件进行探索性分析，无需配置复杂的数据库连接。
