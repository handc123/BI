# Metric API Endpoints Implementation

## Overview
This document describes the three new API endpoints added to `DatasetController` for metric configuration management.

## Endpoints

### 1. Validate Metric Configuration
**Endpoint:** `POST /api/bi/dataset/metric/validate`

**Permission:** `bi:dataset:edit`

**Description:** Validates a metric configuration without saving it.

**Request Body:**
```json
{
  "baseMetrics": [
    {
      "name": "total_amount",
      "alias": "总金额",
      "field": "amount",
      "aggregation": "SUM"
    }
  ],
  "computedMetrics": [
    {
      "computeType": "conditional_ratio",
      "name": "npl_ratio",
      "alias": "不良贷款率",
      "field": "amount",
      "numeratorCondition": "status = 'NPL'",
      "denominatorCondition": "status IN ('NORMAL', 'NPL')",
      "asPercentage": true
    }
  ]
}
```

**Response:**
- Success: `{ "code": 200, "msg": "指标配置验证通过" }`
- Error: `{ "code": 500, "msg": "错误详情" }`

**Security:**
- Validates all user input through `SQLValidator`
- Catches `SecurityException` for SQL injection attempts
- Returns generic error message without exposing SQL details

**Audit Log:** Records validation attempts with title "指标配置验证"

---

### 2. Test Metric
**Endpoint:** `POST /api/bi/dataset/metric/test`

**Permission:** `bi:dataset:query`

**Description:** Tests a metric configuration by executing a sample query with LIMIT clause.

**Request Body:**
```json
{
  "datasetId": 123,
  "metric": {
    "baseMetrics": [...],
    "computedMetrics": [...]
  }
}
```

**Response:**
- Success: 
```json
{
  "code": 200,
  "data": {
    "success": true,
    "columns": ["total_amount", "npl_ratio"],
    "data": [
      {"total_amount": 1000000, "npl_ratio": 0.05}
    ],
    "duration": 125
  }
}
```
- Error: `{ "code": 500, "msg": "错误详情" }`

**Features:**
- Validates dataset existence
- Executes test query with sample data
- Returns execution time for performance monitoring
- Includes SQL query in response (test mode only)

**Security:**
- Validates metric configuration before execution
- Catches `SecurityException` for SQL injection attempts
- Limits result set size

**Audit Log:** Records test attempts with title "指标测试"

---

### 3. Save Metric Configuration
**Endpoint:** `PUT /api/bi/dataset/{id}/config`

**Permission:** `bi:dataset:edit`

**Description:** Saves metric configuration to the dataset's config field as JSON.

**Path Parameter:**
- `id`: Dataset ID

**Request Body:**
```json
{
  "baseMetrics": [...],
  "computedMetrics": [...]
}
```

**Response:**
- Success: `{ "code": 200, "msg": "指标配置保存成功" }`
- Error: `{ "code": 500, "msg": "错误详情" }`

**Features:**
- Validates dataset existence
- Validates metric configuration before saving
- Serializes configuration to JSON
- Records update user

**Security:**
- Validates all user input through `SQLValidator`
- Catches `SecurityException` for SQL injection attempts
- Returns generic error message without exposing internal details

**Audit Log:** Records save operations with title "保存指标配置" and business type UPDATE

---

## Error Handling

All endpoints implement consistent error handling:

1. **Validation Errors (400):**
   - Invalid field names
   - Invalid SQL syntax in conditions
   - Missing required fields
   - Field length violations

2. **Security Errors (403):**
   - SQL injection attempts
   - Dangerous SQL keywords detected
   - SQL comments or terminators found
   - Returns generic message: "配置包含不安全的内容"

3. **Not Found Errors (404):**
   - Dataset does not exist
   - Referenced metrics not found

4. **Server Errors (500):**
   - Database connection failures
   - Query execution errors
   - Serialization/deserialization errors

## Security Features

### SQL Injection Prevention
- All user input validated through `SQLValidator`
- Field names validated with regex: `^[a-zA-Z_][a-zA-Z0-9_]*$`
- Conditions checked for SQL keywords, comments, and terminators
- Expressions validated for dangerous patterns

### Audit Logging
- All operations logged with `@Log` annotation
- Records user, timestamp, and operation type
- Enables security monitoring and compliance

### Permission Control
- `@PreAuthorize` annotations enforce role-based access
- Edit operations require `bi:dataset:edit`
- Query operations require `bi:dataset:query`

## Request Validation

All endpoints use `@Validated` annotation with Jakarta Bean Validation:

- `@NotBlank`: Required string fields
- `@NotNull`: Required object fields
- `@Pattern`: Field name and aggregation validation
- `@Size`: Length constraints
- `@Valid`: Nested object validation

## Supporting Classes

### MetricTestRequest
New DTO class for test metric endpoint:
```java
public class MetricTestRequest {
    @NotNull private Long datasetId;
    @NotNull @Valid private MetricConfigDTO metric;
}
```

### MetricConfigDTO
Existing DTO with validation annotations for all metric types:
- BaseMetric
- ConditionalRatioMetric
- SimpleRatioMetric
- ConditionalSumMetric
- CustomExpressionMetric

## Requirements Satisfied

- **6.5**: Error handling with clear messages
- **11.4**: API endpoints with proper validation
- **11.5**: Metric validation and testing
- **12.1**: Request parameter validation
- **12.2**: Test query execution with results

## Next Steps

1. Implement service layer methods in `DatasetServiceImpl`
2. Add frontend API client in `ui/src/api/bi/metric.js`
3. Create UI components for metric configuration
4. Write integration tests for all endpoints
