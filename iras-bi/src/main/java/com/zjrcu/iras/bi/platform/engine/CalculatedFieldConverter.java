package com.zjrcu.iras.bi.platform.engine;

import com.zjrcu.iras.bi.platform.domain.dto.CalculatedFieldDTO;
import com.zjrcu.iras.bi.platform.domain.dto.DatasetFieldVO;
import com.zjrcu.iras.bi.platform.security.SQLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 计算字段转换器
 * 负责将计算字段配置转换为SQL表达式
 * 
 * @author IRAS
 */
@Component
public class CalculatedFieldConverter {
    
    private static final Logger log = LoggerFactory.getLogger(CalculatedFieldConverter.class);
    
    @Autowired
    private SQLValidator sqlValidator;
    
    @Autowired
    private ExpressionParser expressionParser;
    
    @Autowired
    private SQLDialectAdapter dialectAdapter;
    
    @Autowired
    private BuiltinFunctionConverter builtinFunctionConverter;
    
    /**
     * 转换计算字段为SQL表达式
     * 需求: 7.1, 7.2, 7.3, 7.6, 10.6, 10.7
     * 
     * @param field 计算字段配置
     * @param datasetFields 数据集字段列表
     * @param dbType 数据库类型
     * @return SQL表达式
     */
    public String convertToSQL(
        CalculatedFieldDTO field,
        List<DatasetFieldVO> datasetFields,
        String dbType
    ) {
        if (field == null) {
            throw new IllegalArgumentException("计算字段配置不能为空");
        }
        
        if (datasetFields == null || datasetFields.isEmpty()) {
            throw new IllegalArgumentException("数据集字段列表不能为空");
        }
        
        if (dbType == null || dbType.trim().isEmpty()) {
            throw new IllegalArgumentException("数据库类型不能为空");
        }
        
        // 规范化表达式中的引号(在验证之前)
        String normalizedExpression = normalizeQuotes(field.getExpression());
        
        log.debug("开始转换计算字段: name={}, expression={}, normalized={}, dbType={}", 
            field.getName(), field.getExpression(), normalizedExpression, dbType);
        
        // 1. 验证表达式安全性
        sqlValidator.validateExpression(normalizedExpression);
        
        // 2. 提取字段引用
        Set<String> referencedFields = expressionParser.extractFieldReferences(normalizedExpression);
        log.debug("提取到的字段引用: {}", referencedFields);
        
        // 3. 验证字段引用存在性
        validateFieldReferences(referencedFields, datasetFields);
        
        // 4. 提取函数调用
        Set<String> functions = expressionParser.extractFunctions(normalizedExpression);
        log.debug("提取到的函数调用: {}", functions);
        
        // 5. 验证函数语法
        expressionParser.validateFunctionSyntax(normalizedExpression);
        
        // 6. 区分系统内置函数和数据库函数
        Map<String, String> builtinFunctions = new HashMap<>();
        Set<String> nativeFunctions = new HashSet<>();
        
        for (String func : functions) {
            if (builtinFunctionConverter.isBuiltinFunction(func)) {
                builtinFunctions.put(func, func);
            } else {
                nativeFunctions.add(func);
            }
        }
        
        log.debug("系统内置函数: {}, 数据库函数: {}", builtinFunctions.keySet(), nativeFunctions);
        
        // 7. 转换系统内置函数为目标数据库SQL
        String sql = normalizedExpression;
        for (String builtinFunc : builtinFunctions.keySet()) {
            sql = builtinFunctionConverter.convertFunction(sql, builtinFunc, dbType);
        }
        
        // 8. 验证数据库函数(仅记录警告,不阻止执行)
        validateNativeFunctions(nativeFunctions, dbType);
        
        // 9. 引用字段名(根据数据库类型)
        sql = quoteFieldNames(sql, referencedFields, dbType);
        
        // 10. 应用聚合(如果是指标类型且不是AUTO)
        if ("metric".equals(field.getFieldType()) && !"AUTO".equals(field.getAggregation())) {
            sql = field.getAggregation() + "(" + sql + ")";
        }
        
        // 11. 添加别名
        SQLDialectAdapter.DatabaseType databaseType = SQLDialectAdapter.DatabaseType.fromCode(dbType);
        String quotedAlias = dialectAdapter.quoteIdentifier(databaseType, field.getName());
        sql = sql + " AS " + quotedAlias;
        
        log.debug("转换后的SQL: {}", sql);
        
        return sql;
    }
    
    /**
     * 规范化字符串中的引号
     * 将智能引号转换为标准单引号
     * 
     * @param text 输入文本
     * @return 规范化后的文本
     */
    private String normalizeQuotes(String text) {
        if (text == null) {
            return null;
        }
        
        // 方法1: 替换已知的智能引号变体
        String result = text
            .replace("\u2018", "'")  // 左单引号 '
            .replace("\u2019", "'")  // 右单引号 '
            .replace("\u201C", "\"") // 左双引号 "
            .replace("\u201D", "\"") // 右双引号 "
            .replace("\u300C", "'")  // 中文左引号 「
            .replace("\u300D", "'")  // 中文右引号 」
            .replace("\u300E", "'")  // 中文左双引号 『
            .replace("\u300F", "'")  // 中文右双引号 』
            .replace("\uFF07", "'")  // 全角撇号 ＇
            .replace("\u02BC", "'")  // 修饰符字母撇号 ʼ
            .replace("\u02BB", "'"); // 修饰符字母反撇号 ʻ
        
        // 方法2: 使用正则表达式替换所有Unicode引号类字符
        // 匹配Unicode类别中的引号字符(除了标准ASCII单引号和双引号)
        result = result.replaceAll("[\\u2018\\u2019\\u201A\\u201B\\u201C\\u201D\\u201E\\u201F\\u2039\\u203A\\u300C\\u300D\\u300E\\u300F\\uFF07\\u02BC\\u02BB\\u02C8\\u02CA\\u0301\\u0313\\u0314\\u0315\\u055A\\u05F3\\u05F4\\u2032\\u2033\\u2034\\u2035\\u2036\\u2037\\u2057]", "'");
        
        return result;
    }
    
    /**
     * 验证字段引用存在性
     * 需求: 7.1
     * 
     * @param referencedFields 引用的字段名集合
     * @param datasetFields 数据集字段列表
     * @throws IllegalArgumentException 如果引用的字段不存在
     */
    private void validateFieldReferences(
        Set<String> referencedFields,
        List<DatasetFieldVO> datasetFields
    ) {
        Set<String> availableFields = datasetFields.stream()
            .map(DatasetFieldVO::getFieldName)
            .collect(Collectors.toSet());
            
        for (String field : referencedFields) {
            if (!availableFields.contains(field)) {
                throw new IllegalArgumentException(
                    "引用的字段不存在: " + field + ". 可用字段: " + availableFields
                );
            }
        }
    }
    
    /**
     * 验证数据库函数
     * 需求: 10.7
     * 
     * @param functions 函数名称集合
     * @param dbType 数据库类型
     */
    private void validateNativeFunctions(Set<String> functions, String dbType) {
        // 数据库函数不做严格验证,由数据库执行时报错
        // 但可以提供警告信息
        if (!functions.isEmpty()) {
            log.warn("表达式包含数据库原生函数: {}, 数据库类型: {}. " +
                "这些函数将直接传递给数据库,请确保它们在目标数据库中有效。", 
                functions, dbType);
        }
    }
    
    /**
     * 引用字段名(根据数据库类型添加引号)
     * 需求: 7.6
     * 
     * @param expression 表达式
     * @param fieldNames 字段名集合
     * @param dbType 数据库类型
     * @return 引用后的表达式
     */
    private String quoteFieldNames(String expression, Set<String> fieldNames, String dbType) {
        SQLDialectAdapter.DatabaseType databaseType = SQLDialectAdapter.DatabaseType.fromCode(dbType);
        
        String result = expression;
        
        // 按字段名长度降序排序,避免短字段名替换长字段名的一部分
        List<String> sortedFields = new ArrayList<>(fieldNames);
        sortedFields.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        for (String fieldName : sortedFields) {
            String quotedField = dialectAdapter.quoteIdentifier(databaseType, fieldName);
            // 使用单词边界匹配,避免替换字段名的一部分
            result = result.replaceAll("\\b" + fieldName + "\\b", quotedField);
        }
        
        return result;
    }
    
    /**
     * 批量转换计算字段为SQL
     * 
     * @param fields 计算字段列表
     * @param datasetFields 数据集字段列表
     * @param dbType 数据库类型
     * @return 字段名到SQL表达式的映射
     */
    public Map<String, String> convertBatchToSQL(
        List<CalculatedFieldDTO> fields,
        List<DatasetFieldVO> datasetFields,
        String dbType
    ) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String, String> sqlMap = new LinkedHashMap<>();
        
        for (CalculatedFieldDTO field : fields) {
            try {
                String sql = convertToSQL(field, datasetFields, dbType);
                sqlMap.put(field.getName(), sql);
            } catch (Exception e) {
                log.error("转换计算字段失败: name={}, error={}", field.getName(), e.getMessage());
                throw new IllegalArgumentException(
                    "转换计算字段 '" + field.getName() + "' 失败: " + e.getMessage(), e
                );
            }
        }
        
        return sqlMap;
    }
    
    /**
     * 验证计算字段配置
     * 
     * @param field 计算字段配置
     * @param datasetFields 数据集字段列表
     * @return 验证结果消息
     */
    public ValidationResult validateCalculatedField(
        CalculatedFieldDTO field,
        List<DatasetFieldVO> datasetFields
    ) {
        try {
            // 验证表达式安全性
            sqlValidator.validateExpression(field.getExpression());
            
            // 提取并验证字段引用
            Set<String> referencedFields = expressionParser.extractFieldReferences(field.getExpression());
            validateFieldReferences(referencedFields, datasetFields);
            
            // 验证函数语法
            expressionParser.validateFunctionSyntax(field.getExpression());
            
            return new ValidationResult(true, "验证通过", null);
            
        } catch (Exception e) {
            log.warn("计算字段验证失败: name={}, error={}", field.getName(), e.getMessage());
            return new ValidationResult(false, e.getMessage(), null);
        }
    }
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String sqlPreview;
        
        public ValidationResult(boolean valid, String message, String sqlPreview) {
            this.valid = valid;
            this.message = message;
            this.sqlPreview = sqlPreview;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getSqlPreview() {
            return sqlPreview;
        }
    }
}
