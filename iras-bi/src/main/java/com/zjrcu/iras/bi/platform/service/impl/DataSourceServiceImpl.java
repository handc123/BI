package com.zjrcu.iras.bi.platform.service.impl;

import com.zjrcu.iras.bi.platform.domain.DataSource;
import com.zjrcu.iras.bi.platform.domain.Dataset;
import com.zjrcu.iras.bi.platform.domain.dto.ConnectionTestResult;
import com.zjrcu.iras.bi.platform.engine.DataSourceManager;
import com.zjrcu.iras.bi.platform.mapper.DataSourceMapper;
import com.zjrcu.iras.bi.platform.mapper.DatasetMapper;
import com.zjrcu.iras.bi.platform.service.IDataSourceService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.AesUtils;
import com.zjrcu.iras.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * BI数据源服务实现
 *
 * @author iras
 */
@Service
public class DataSourceServiceImpl implements IDataSourceService {
    private static final Logger log = LoggerFactory.getLogger(DataSourceServiceImpl.class);

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private DataSourceManager dataSourceManager;

    /**
     * 查询数据源列表
     *
     * @param dataSource 数据源查询条件
     * @return 数据源列表
     */
    @Override
    public List<DataSource> selectDataSourceList(DataSource dataSource) {
        List<DataSource> list = dataSourceMapper.selectDataSourceList(dataSource);
        
        // 解密密码字段用于显示（屏蔽显示）
        for (DataSource ds : list) {
            maskPassword(ds);
        }
        
        log.debug("查询数据源列表成功，共{}条记录", list.size());
        return list;
    }

    /**
     * 根据ID查询数据源
     *
     * @param id 数据源ID
     * @return 数据源信息
     */
    @Override
    public DataSource selectDataSourceById(Long id) {
        if (id == null) {
            throw new ServiceException("数据源ID不能为空");
        }

        DataSource dataSource = dataSourceMapper.selectDataSourceById(id);
        if (dataSource == null) {
            log.warn("数据源不存在: id={}", id);
            throw new ServiceException("数据源不存在");
        }

        // 解密密码字段
        decryptPassword(dataSource);
        
        log.info("查询数据源成功: id={}, name={}", id, dataSource.getName());
        return dataSource;
    }

    /**
     * 新增数据源
     *
     * @param dataSource 数据源信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDataSource(DataSource dataSource) {
        validateDataSource(dataSource);
        
        // 加密密码
        encryptPassword(dataSource);
        
        int result = dataSourceMapper.insertDataSource(dataSource);
        
        if (result > 0) {
            log.info("新增数据源成功: id={}, name={}, type={}", 
                    dataSource.getId(), dataSource.getName(), dataSource.getType());
            
            // 如果是数据库类型，初始化连接池
            if (isDatabaseType(dataSource.getType())) {
                try {
                    // 解密密码用于初始化连接池
                    decryptPassword(dataSource);
                    dataSourceManager.initializeDataSource(dataSource);
                    log.info("数据源连接池初始化成功: id={}", dataSource.getId());
                } catch (Exception e) {
                    log.error("数据源连接池初始化失败: id={}, error={}", dataSource.getId(), e.getMessage());
                    // 不抛出异常，允许数据源创建成功，后续使用时再初始化
                }
            }
        } else {
            log.error("新增数据源失败: name={}", dataSource.getName());
        }
        
        return result;
    }

    /**
     * 修改数据源
     *
     * @param dataSource 数据源信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDataSource(DataSource dataSource) {
        if (dataSource.getId() == null) {
            throw new ServiceException("数据源ID不能为空");
        }

        validateDataSource(dataSource);
        
        // 获取原数据源信息
        DataSource oldDataSource = dataSourceMapper.selectDataSourceById(dataSource.getId());
        if (oldDataSource == null) {
            throw new ServiceException("数据源不存在");
        }

        // 检查密码是否修改
        Map<String, Object> newConfigMap = dataSource.getConfigMap();
        String newPassword = (String) newConfigMap.get("password");
        
        // 如果密码为空或为掩码，使用原密码
        if (StringUtils.isEmpty(newPassword) || "********".equals(newPassword)) {
            Map<String, Object> oldConfigMap = oldDataSource.getConfigMap();
            newConfigMap.put("password", oldConfigMap.get("password"));
            dataSource.setConfigMap(newConfigMap);
        } else {
            // 加密新密码
            encryptPassword(dataSource);
        }
        
        int result = dataSourceMapper.updateDataSource(dataSource);
        
        if (result > 0) {
            log.info("修改数据源成功: id={}, name={}, type={}", 
                    dataSource.getId(), dataSource.getName(), dataSource.getType());
            
            // 如果是数据库类型，重新初始化连接池
            if (isDatabaseType(dataSource.getType())) {
                try {
                    // 关闭旧连接池
                    dataSourceManager.closeDataSource(dataSource.getId());
                    
                    // 解密密码用于初始化连接池
                    decryptPassword(dataSource);
                    
                    // 初始化新连接池
                    dataSourceManager.initializeDataSource(dataSource);
                    log.info("数据源连接池重新初始化成功: id={}", dataSource.getId());
                } catch (Exception e) {
                    log.error("数据源连接池重新初始化失败: id={}, error={}", dataSource.getId(), e.getMessage());
                }
            }
        } else {
            log.error("修改数据源失败: id={}", dataSource.getId());
        }
        
        return result;
    }

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDataSourceById(Long id) {
        if (id == null) {
            throw new ServiceException("数据源ID不能为空");
        }

        // 检查数据源是否存在
        DataSource dataSource = dataSourceMapper.selectDataSourceById(id);
        if (dataSource == null) {
            throw new ServiceException("数据源不存在");
        }

        // 检查是否有数据集依赖此数据源
        checkDatasetDependency(id);
        
        // 关闭连接池
        if (isDatabaseType(dataSource.getType())) {
            try {
                dataSourceManager.closeDataSource(id);
                log.info("数据源连接池已关闭: id={}", id);
            } catch (Exception e) {
                log.warn("关闭数据源连接池失败: id={}, error={}", id, e.getMessage());
            }
        }
        
        int result = dataSourceMapper.deleteDataSourceById(id);
        
        if (result > 0) {
            log.info("删除数据源成功: id={}, name={}", id, dataSource.getName());
        } else {
            log.error("删除数据源失败: id={}", id);
        }
        
        return result;
    }

    /**
     * 批量删除数据源
     *
     * @param ids 数据源ID数组
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDataSourceByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("数据源ID不能为空");
        }

        int successCount = 0;
        StringBuilder errorMsg = new StringBuilder();
        
        for (Long id : ids) {
            try {
                // 检查依赖关系
                checkDatasetDependency(id);
                
                // 删除数据源
                int result = deleteDataSourceById(id);
                if (result > 0) {
                    successCount++;
                }
            } catch (ServiceException e) {
                log.warn("删除数据源失败: id={}, error={}", id, e.getMessage());
                errorMsg.append("数据源ID[").append(id).append("]: ").append(e.getMessage()).append("; ");
            }
        }
        
        if (errorMsg.length() > 0) {
            throw new ServiceException("部分数据源删除失败: " + errorMsg.toString());
        }
        
        log.info("批量删除数据源成功，共删除{}条记录", successCount);
        return successCount;
    }

    /**
     * 测试数据源连接
     *
     * @param dataSource 数据源配置
     * @return 连接测试结果
     */
    @Override
    public ConnectionTestResult testConnection(DataSource dataSource) {
        if (dataSource == null) {
            return ConnectionTestResult.failure("数据源配置不能为空");
        }

        validateDataSource(dataSource);
        
        // 如果密码是脱敏值（********），则从数据库读取真实密码
        if (dataSource.getId() != null) {
            Map<String, Object> configMap = dataSource.getConfigMap();
            String password = (String) configMap.get("password");
            
            if ("********".equals(password)) {
                log.debug("检测到脱敏密码，从数据库读取真实密码: dataSourceId={}", dataSource.getId());
                
                // 从数据库读取完整的数据源信息
                DataSource dbDataSource = dataSourceMapper.selectDataSourceById(dataSource.getId());
                if (dbDataSource != null) {
                    Map<String, Object> dbConfigMap = dbDataSource.getConfigMap();
                    String realPassword = (String) dbConfigMap.get("password");
                    
                    // 使用真实密码替换脱敏密码
                    configMap.put("password", realPassword);
                    dataSource.setConfigMap(configMap);
                    
                    log.debug("已使用数据库中的真实密码进行测试连接");
                } else {
                    log.warn("无法从数据库读取数据源信息: dataSourceId={}", dataSource.getId());
                }
            }
        }
        
        // 解密密码用于测试连接
        decryptPassword(dataSource);
        
        log.info("开始测试数据源连接: name={}, type={}", dataSource.getName(), dataSource.getType());
        
        ConnectionTestResult result = dataSourceManager.testConnection(dataSource);
        
        if (result.isSuccess()) {
            log.info("数据源连接测试成功: name={}, type={}, duration={}ms", 
                    dataSource.getName(), dataSource.getType(), result.getDuration());
        } else {
            log.warn("数据源连接测试失败: name={}, type={}, error={}", 
                    dataSource.getName(), dataSource.getType(), result.getMessage());
        }
        
        return result;
    }

    /**
     * 获取数据源的表列表
     *
     * @param dataSourceId 数据源ID
     * @return 表名列表
     */
    @Override
    public List<String> getTableList(Long dataSourceId) {
        if (dataSourceId == null) {
            throw new ServiceException("数据源ID不能为空");
        }

        // 获取数据源信息
        DataSource dataSource = dataSourceMapper.selectDataSourceById(dataSourceId);
        if (dataSource == null) {
            throw new ServiceException("数据源不存在");
        }

        // 只支持数据库类型的数据源
        if (!isDatabaseType(dataSource.getType())) {
            throw new ServiceException("只有数据库类型的数据源支持获取表列表");
        }

        // 解密密码
        decryptPassword(dataSource);

        log.info("开始获取数据源表列表: dataSourceId={}, name={}, type={}", 
                dataSourceId, dataSource.getName(), dataSource.getType());

        try {
            // 初始化数据源连接池（如果还没有初始化）
            if (!dataSourceManager.isDataSourceInitialized(dataSourceId)) {
                dataSourceManager.initializeDataSource(dataSource);
            }

            // 获取表列表
            List<String> tables = dataSourceManager.getTableList(dataSourceId);
            
            log.info("获取数据源表列表成功: dataSourceId={}, tableCount={}", dataSourceId, tables.size());
            return tables;
        } catch (Exception e) {
            log.error("获取数据源表列表失败: dataSourceId={}, error={}", dataSourceId, e.getMessage());
            throw new ServiceException("获取表列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取表结构信息
     *
     * @param dataSourceId 数据源ID
     * @param tableName    表名
     * @return 表结构信息
     */
    @Override
    public com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo getTableSchema(Long dataSourceId, String tableName) {
        if (dataSourceId == null) {
            throw new ServiceException("数据源ID不能为空");
        }
        if (StringUtils.isEmpty(tableName)) {
            throw new ServiceException("表名不能为空");
        }

        // 获取数据源信息
        DataSource dataSource = dataSourceMapper.selectDataSourceById(dataSourceId);
        if (dataSource == null) {
            throw new ServiceException("数据源不存在");
        }

        // 只支持数据库类型的数据源
        if (!isDatabaseType(dataSource.getType())) {
            throw new ServiceException("只有数据库类型的数据源支持获取表结构");
        }

        // 解密密码
        decryptPassword(dataSource);

        log.info("开始获取表结构: dataSourceId={}, tableName={}", dataSourceId, tableName);

        try {
            // 初始化数据源连接池（如果还没有初始化）
            if (!dataSourceManager.isDataSourceInitialized(dataSourceId)) {
                dataSourceManager.initializeDataSource(dataSource);
            }

            // 获取表结构
            com.zjrcu.iras.bi.platform.domain.dto.TableSchemaInfo schema = 
                    dataSourceManager.getTableSchema(dataSourceId, tableName);
            
            log.info("获取表结构成功: dataSourceId={}, tableName={}, columnCount={}", 
                    dataSourceId, tableName, schema.getColumns().size());
            return schema;
        } catch (Exception e) {
            log.error("获取表结构失败: dataSourceId={}, tableName={}, error={}", 
                    dataSourceId, tableName, e.getMessage());
            throw new ServiceException("获取表结构失败: " + e.getMessage());
        }
    }

    /**
     * 获取表数据预览
     *
     * @param dataSourceId 数据源ID
     * @param tableName    表名
     * @param limit        限制行数
     * @return 表数据预览
     */
    @Override
    public com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData getTablePreview(Long dataSourceId, String tableName, int limit) {
        if (dataSourceId == null) {
            throw new ServiceException("数据源ID不能为空");
        }
        if (StringUtils.isEmpty(tableName)) {
            throw new ServiceException("表名不能为空");
        }
        if (limit <= 0 || limit > 100) {
            throw new ServiceException("限制行数必须在1-100之间");
        }

        // 获取数据源信息
        DataSource dataSource = dataSourceMapper.selectDataSourceById(dataSourceId);
        if (dataSource == null) {
            throw new ServiceException("数据源不存在");
        }

        // 只支持数据库类型的数据源
        if (!isDatabaseType(dataSource.getType())) {
            throw new ServiceException("只有数据库类型的数据源支持获取数据预览");
        }

        // 解密密码
        decryptPassword(dataSource);

        log.info("开始获取表数据预览: dataSourceId={}, tableName={}, limit={}", 
                dataSourceId, tableName, limit);

        try {
            // 初始化数据源连接池（如果还没有初始化）
            if (!dataSourceManager.isDataSourceInitialized(dataSourceId)) {
                dataSourceManager.initializeDataSource(dataSource);
            }

            // 获取数据预览
            com.zjrcu.iras.bi.platform.domain.dto.TablePreviewData preview = 
                    dataSourceManager.getTablePreview(dataSourceId, tableName, limit);
            
            log.info("获取表数据预览成功: dataSourceId={}, tableName={}, previewRows={}, totalRows={}", 
                    dataSourceId, tableName, preview.getRows().size(), preview.getTotal());
            return preview;
        } catch (Exception e) {
            log.error("获取表数据预览失败: dataSourceId={}, tableName={}, error={}", 
                    dataSourceId, tableName, e.getMessage());
            throw new ServiceException("获取表数据预览失败: " + e.getMessage());
        }
    }

    /**
     * 验证数据源配置
     *
     * @param dataSource 数据源
     */
    private void validateDataSource(DataSource dataSource) {
        if (StringUtils.isEmpty(dataSource.getName())) {
            throw new ServiceException("数据源名称不能为空");
        }

        if (StringUtils.isEmpty(dataSource.getType())) {
            throw new ServiceException("数据源类型不能为空");
        }

        if (StringUtils.isEmpty(dataSource.getConfig())) {
            throw new ServiceException("数据源配置不能为空");
        }

        // 验证配置JSON格式
        try {
            Map<String, Object> configMap = dataSource.getConfigMap();
            if (configMap.isEmpty()) {
                throw new ServiceException("数据源配置格式错误");
            }

            // 根据类型验证必需字段
            String type = dataSource.getType().toLowerCase();
            if (isDatabaseType(type)) {
                validateDatabaseConfig(configMap);
            } else if ("api".equals(type)) {
                validateApiConfig(configMap);
            } else if ("file".equals(type)) {
                validateFileConfig(configMap);
            } else {
                throw new ServiceException("不支持的数据源类型: " + type);
            }
        } catch (Exception e) {
            log.error("数据源配置验证失败: {}", e.getMessage());
            throw new ServiceException("数据源配置验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证数据库配置
     *
     * @param configMap 配置Map
     */
    private void validateDatabaseConfig(Map<String, Object> configMap) {
        if (!configMap.containsKey("host") || StringUtils.isEmpty((String) configMap.get("host"))) {
            throw new ServiceException("数据库主机地址不能为空");
        }

        if (!configMap.containsKey("port")) {
            throw new ServiceException("数据库端口不能为空");
        }

        if (!configMap.containsKey("database") || StringUtils.isEmpty((String) configMap.get("database"))) {
            throw new ServiceException("数据库名称不能为空");
        }

        if (!configMap.containsKey("username") || StringUtils.isEmpty((String) configMap.get("username"))) {
            throw new ServiceException("数据库用户名不能为空");
        }
    }

    /**
     * 验证API配置
     *
     * @param configMap 配置Map
     */
    private void validateApiConfig(Map<String, Object> configMap) {
        if (!configMap.containsKey("url") || StringUtils.isEmpty((String) configMap.get("url"))) {
            throw new ServiceException("API URL不能为空");
        }
    }

    /**
     * 验证文件配置
     *
     * @param configMap 配置Map
     */
    private void validateFileConfig(Map<String, Object> configMap) {
        if (!configMap.containsKey("filePath") || StringUtils.isEmpty((String) configMap.get("filePath"))) {
            throw new ServiceException("文件路径不能为空");
        }
    }

    /**
     * 检查数据集依赖
     *
     * @param dataSourceId 数据源ID
     */
    private void checkDatasetDependency(Long dataSourceId) {
        Dataset queryDataset = new Dataset();
        queryDataset.setDatasourceId(dataSourceId);
        List<Dataset> datasets = datasetMapper.selectDatasetList(queryDataset);
        
        if (datasets != null && !datasets.isEmpty()) {
            StringBuilder datasetNames = new StringBuilder();
            for (Dataset dataset : datasets) {
                if (datasetNames.length() > 0) {
                    datasetNames.append(", ");
                }
                datasetNames.append(dataset.getName());
            }
            
            String errorMsg = String.format("数据源正在被以下数据集使用，无法删除: %s", datasetNames.toString());
            log.warn("数据源删除失败: dataSourceId={}, 依赖的数据集: {}", dataSourceId, datasetNames);
            throw new ServiceException(errorMsg);
        }
    }

    /**
     * 加密密码
     *
     * @param dataSource 数据源
     */
    private void encryptPassword(DataSource dataSource) {
        try {
            Map<String, Object> configMap = dataSource.getConfigMap();
            String password = (String) configMap.get("password");
            
            if (StringUtils.isNotEmpty(password) && !"********".equals(password)) {
                String encryptedPassword = AesUtils.encrypt(password);
                configMap.put("password", encryptedPassword);
                dataSource.setConfigMap(configMap);
                log.debug("数据源密码加密成功: name={}", dataSource.getName());
            }
        } catch (Exception e) {
            log.error("数据源密码加密失败: name={}, error={}", dataSource.getName(), e.getMessage());
            throw new ServiceException("数据源密码加密失败");
        }
    }

    /**
     * 解密密码
     *
     * @param dataSource 数据源
     */
    private void decryptPassword(DataSource dataSource) {
        try {
            Map<String, Object> configMap = dataSource.getConfigMap();
            String encryptedPassword = (String) configMap.get("password");
            
            if (StringUtils.isNotEmpty(encryptedPassword) && !"********".equals(encryptedPassword)) {
                // 检查密码是否已加密（加密后的密码长度是16的倍数且为Base64格式）
                // 如果密码看起来像明文（不是Base64或长度不对），则不解密
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

    /**
     * 判断密码是否已加密
     * 加密后的密码是Base64编码，且长度应该是16的倍数（AES加密后）
     *
     * @param password 密码
     * @return 是否已加密
     */
    private boolean isEncryptedPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return false;
        }
        
        // 检查是否为Base64格式（只包含Base64字符）
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

    /**
     * 屏蔽密码显示
     *
     * @param dataSource 数据源
     */
    private void maskPassword(DataSource dataSource) {
        try {
            Map<String, Object> configMap = dataSource.getConfigMap();
            if (configMap.containsKey("password")) {
                configMap.put("password", "********");
                dataSource.setConfigMap(configMap);
            }
        } catch (Exception e) {
            log.warn("屏蔽密码失败: name={}, error={}", dataSource.getName(), e.getMessage());
        }
    }

    /**
     * 判断是否为数据库类型
     *
     * @param type 数据源类型
     * @return 是否为数据库类型
     */
    private boolean isDatabaseType(String type) {
        if (StringUtils.isEmpty(type)) {
            return false;
        }
        
        String lowerType = type.toLowerCase();
        return "mysql".equals(lowerType) 
                || "postgresql".equals(lowerType) 
                || "clickhouse".equals(lowerType) 
                || "doris".equals(lowerType) 
                || "oracle".equals(lowerType);
    }
}
