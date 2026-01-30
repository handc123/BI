# Bug Fix: 数据源密码解密错误

## 问题描述

在测试数据源连接时，系统抛出AES解密错误：
```
AES解密失败: Input length must be multiple of 16 when decrypting with padded cipher
连接失败: 解密数据源密码失败
```

## 根本原因

在测试数据源连接时，系统在两个地方尝试解密密码：

1. **DataSourceServiceImpl.testConnection()** → 调用 `decryptPassword()`
2. **DataSourceManager.buildHikariConfig()** → 调用 `AesUtils.decrypt()`

但是：
- **新建数据源测试连接**：前端发送的密码是**明文**（未加密）
- **编辑已有数据源测试连接**：数据库中存储的密码是**密文**（已加密）

原代码无条件地尝试解密所有密码，导致对明文密码进行解密时失败。

## 解决方案

在两个文件中都添加密码格式检测：

### 1. DataSourceServiceImpl
在 `decryptPassword()` 方法中添加密码格式检测

### 2. DataSourceManager  
在 `buildHikariConfig()` 方法中添加密码格式检测

两处都添加 `isEncryptedPassword()` 方法判断密码是否已加密：
- 只对已加密的密码进行解密操作
- 对明文密码直接使用，不进行解密

### 判断逻辑

加密后的密码特征：
- Base64编码格式（只包含 `A-Za-z0-9+/=` 字符）
- 解码后的字节长度是16的倍数（AES块大小）

明文密码特征：
- 可能包含特殊字符（如 `@#$%` 等）
- 长度不一定是16的倍数

## 修改文件

1. `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/service/impl/DataSourceServiceImpl.java`
2. `iras-bi/src/main/java/com/zjrcu/iras/bi/platform/engine/DataSourceManager.java`

## 修改内容

### 1. DataSourceServiceImpl - 更新 `decryptPassword()` 方法

```java
private void decryptPassword(DataSource dataSource) {
    try {
        Map<String, Object> configMap = dataSource.getConfigMap();
        String encryptedPassword = (String) configMap.get("password");
        
        if (StringUtils.isNotEmpty(encryptedPassword) && !"********".equals(encryptedPassword)) {
            // 检查密码是否已加密
            if (isEncryptedPassword(encryptedPassword)) {
                String password = AesUtils.decrypt(encryptedPassword);
                configMap.put("password", password);
                dataSource.setConfigMap(configMap);
                log.debug("数据源密码解密成功: name={}", dataSource.getName());
            } else {
                // 密码是明文，不需要解密
                log.debug("数据源密码为明文，无需解密: name={}", dataSource.getName());
            }
        }
    } catch (Exception e) {
        log.error("数据源密码解密失败: name={}, error={}", dataSource.getName(), e.getMessage());
        throw new ServiceException("数据源密码解密失败");
    }
}
```

### 2. DataSourceManager - 更新密码处理逻辑

```java
if (StringUtils.isNotEmpty(encryptedPassword)) {
    try {
        // 检查密码是否已加密，如果是明文则直接使用
        String password;
        if (isEncryptedPassword(encryptedPassword)) {
            password = AesUtils.decrypt(encryptedPassword);
            log.debug("数据源密码已解密: dataSourceId={}", dataSource.getId());
        } else {
            password = encryptedPassword;
            log.debug("数据源密码为明文，直接使用: dataSourceId={}", dataSource.getId());
        }
        config.setPassword(password);
    } catch (Exception e) {
        log.error("解密数据源密码失败: dataSourceId={}, error={}", dataSource.getId(), e.getMessage());
        throw new ServiceException("解密数据源密码失败");
    }
}
```

### 3. 添加 `isEncryptedPassword()` 方法（两个文件都添加）

```java
private boolean isEncryptedPassword(String password) {
    if (StringUtils.isEmpty(password)) {
        return false;
    }
    
    // 检查是否为Base64格式
    if (!password.matches("^[A-Za-z0-9+/=]+$")) {
        return false;
    }
    
    // Base64解码后的长度应该是16的倍数（AES块大小）
    try {
        byte[] decoded = java.util.Base64.getDecoder().decode(password);
        return decoded.length % 16 == 0 && decoded.length > 0;
    } catch (Exception e) {
        return false;
    }
}
```

## 测试场景

修复后支持以下场景：

1. ✅ **新建数据源 + 测试连接**：明文密码，直接使用
2. ✅ **新建数据源 + 保存**：明文密码，加密后保存到数据库
3. ✅ **编辑数据源 + 测试连接**：密文密码，解密后测试
4. ✅ **编辑数据源 + 保存**：密文密码，保持加密状态
5. ✅ **查询数据源列表**：密文密码，显示为 `********`

## 影响范围

- 数据源连接测试功能
- 数据源新增/编辑功能
- 数据源连接池初始化
- 不影响已有数据（数据库中的密码仍然是加密的）

## 验证步骤

1. **重新编译并启动后端服务**（重要！）
2. 访问数据源管理页面
3. 点击"新增数据源"
4. 填写数据源信息：
   - 名称：test
   - 类型：MySQL
   - 主机：127.0.0.1
   - 端口：3306
   - 数据库：ry-vue
   - 用户名：root
   - 密码：123456（明文）
5. 点击"测试连接"
6. 应该成功连接，不再报解密错误 ✅

## 日期

2025-01-20
