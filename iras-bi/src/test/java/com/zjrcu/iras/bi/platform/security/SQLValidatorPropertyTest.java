package com.zjrcu.iras.bi.platform.security;

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SQLValidator 属性测试
 * 
 * 使用 jqwik 进行基于属性的测试,验证 SQL 注入防护和条件表达式验证。
 * 
 * 验证需求: 6.2, 6.3, 6.4, 6.7, 2.2, 2.3, 4.2, 4.4
 * 
 * @author IRAS BI Platform
 */
class SQLValidatorPropertyTest {

    private SQLValidator sqlValidator;

    @BeforeEach
    void setUp() {
        sqlValidator = new SQLValidator();
    }

    /**
     * 属性 6: SQL注入防护 - SQL关键字黑名单
     * 
     * 验证需求: 6.2
     * 
     * 对于任意包含SQL关键字的输入,SQLValidator应该拒绝它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 6: SQL注入防护 - SQL关键字")
    void sqlInjectionPrevention_Keywords(@ForAll("maliciousInputsWithKeywords") String input) {
        assertThatThrownBy(() -> sqlValidator.validateCondition(input))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("SQL关键字");
    }

    /**
     * 属性 6: SQL注入防护 - SQL注释符号
     * 
     * 验证需求: 6.3
     * 
     * 对于任意包含SQL注释符号的输入,SQLValidator应该拒绝它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 6: SQL注入防护 - SQL注释")
    void sqlInjectionPrevention_Comments(@ForAll("maliciousInputsWithComments") String input) {
        assertThatThrownBy(() -> sqlValidator.validateCondition(input))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("注释符号");
    }

    /**
     * 属性 6: SQL注入防护 - SQL终止符
     * 
     * 验证需求: 6.4
     * 
     * 对于任意包含分号的输入,SQLValidator应该拒绝它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 6: SQL注入防护 - SQL终止符")
    void sqlInjectionPrevention_Terminators(@ForAll("maliciousInputsWithTerminators") String input) {
        assertThatThrownBy(() -> sqlValidator.validateCondition(input))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("分号");
    }

    /**
     * 属性 6: SQL注入防护 - 字段名验证
     * 
     * 验证需求: 6.7
     * 
     * 对于任意不符合字段名格式的输入,SQLValidator应该拒绝它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 6: SQL注入防护 - 字段名格式")
    void sqlInjectionPrevention_InvalidFieldNames(@ForAll("invalidFieldNames") String fieldName) {
        assertThatThrownBy(() -> sqlValidator.validateFieldName(fieldName))
            .isInstanceOf(SecurityException.class);
    }

    /**
     * 属性 7: 条件表达式验证 - 有效条件应该通过
     * 
     * 验证需求: 2.2, 2.3, 4.2
     * 
     * 对于任意有效的条件表达式,SQLValidator应该接受它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 7: 条件表达式验证 - 有效条件")
    void conditionValidation_ValidConditions(@ForAll("validConditions") String condition) {
        // 不应该抛出异常
        sqlValidator.validateCondition(condition);
    }

    /**
     * 属性 7: 条件表达式验证 - 括号匹配
     * 
     * 验证需求: 4.4
     * 
     * 对于任意括号不匹配的条件表达式,SQLValidator应该拒绝它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 7: 条件表达式验证 - 括号匹配")
    void conditionValidation_UnmatchedParentheses(@ForAll("conditionsWithUnmatchedParentheses") String condition) {
        assertThatThrownBy(() -> sqlValidator.validateCondition(condition))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("括号不匹配");
    }

    /**
     * 属性 6: 数学表达式验证 - SQL关键字防护
     * 
     * 验证需求: 6.7
     * 
     * 对于任意包含SQL关键字的数学表达式,SQLValidator应该拒绝它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 6: 数学表达式SQL注入防护")
    void expressionValidation_SqlKeywords(@ForAll("expressionsWithKeywords") String expression) {
        assertThatThrownBy(() -> sqlValidator.validateExpression(expression))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("SQL关键字");
    }

    /**
     * 属性 7: 数学表达式验证 - 有效表达式应该通过
     * 
     * 验证需求: 5.2, 5.3, 5.4
     * 
     * 对于任意有效的数学表达式,SQLValidator应该接受它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 7: 数学表达式验证 - 有效表达式")
    void expressionValidation_ValidExpressions(@ForAll("validExpressions") String expression) {
        // 不应该抛出异常
        sqlValidator.validateExpression(expression);
    }

    /**
     * 属性 6: 聚合函数验证 - 只允许白名单函数
     * 
     * 验证需求: 6.1
     * 
     * 对于任意不在白名单中的聚合函数,SQLValidator应该拒绝它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 6: 聚合函数白名单验证")
    void aggregationValidation_OnlyAllowedFunctions(@ForAll("invalidAggregations") String aggregation) {
        assertThatThrownBy(() -> sqlValidator.validateAggregation(aggregation))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("不支持的聚合函数");
    }

    /**
     * 属性 7: 指标引用验证
     * 
     * 验证需求: 3.2, 5.4
     * 
     * 对于任意引用不存在指标的配置,SQLValidator应该拒绝它
     */
    @Property(tries = 20)
    @Label("Feature: enhanced-data-config, Property 7: 指标引用验证")
    void metricReferenceValidation(@ForAll("invalidMetricReferences") MetricReferenceTestCase testCase) {
        assertThatThrownBy(() -> 
            sqlValidator.validateMetricReferences(testCase.referencedMetrics, testCase.availableMetrics))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("不存在的指标");
    }

    // ==================== Arbitrary Providers ====================

    /**
     * 生成包含SQL关键字的恶意输入
     */
    @Provide
    Arbitrary<String> maliciousInputsWithKeywords() {
        List<String> sqlKeywords = Arrays.asList(
            "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", 
            "TRUNCATE", "EXEC", "EXECUTE", "UNION", "DECLARE", "CAST", "CONVERT"
        );
        
        return Arbitraries.of(sqlKeywords)
            .flatMap(keyword -> Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(prefix -> prefix + " " + keyword + " something"));
    }

    /**
     * 生成包含SQL注释的恶意输入
     */
    @Provide
    Arbitrary<String> maliciousInputsWithComments() {
        return Arbitraries.of(
            "status = 'active' -- comment",
            "status = 'active' /* comment */",
            "status = 'active' */ malicious",
            "field = 'value' --",
            "/* comment */ field = 'value'"
        );
    }

    /**
     * 生成包含SQL终止符的恶意输入
     */
    @Provide
    Arbitrary<String> maliciousInputsWithTerminators() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(5)
            .ofMaxLength(30)
            .map(s -> s + "; DROP TABLE users");
    }

    /**
     * 生成无效的字段名
     */
    @Provide
    Arbitrary<String> invalidFieldNames() {
        return Arbitraries.oneOf(
            // 以数字开头
            Arbitraries.strings().numeric().ofMinLength(1).ofMaxLength(10)
                .map(s -> s + "field"),
            // 包含特殊字符
            Arbitraries.of("field-name", "field.name", "field@name", "field name", "field$name"),
            // SQL关键字
            Arbitraries.of("SELECT", "INSERT", "UPDATE", "DELETE", "DROP"),
            // 空字符串
            Arbitraries.just(""),
            // 只有空格
            Arbitraries.just("   ")
        );
    }

    /**
     * 生成有效的条件表达式
     */
    @Provide
    Arbitrary<String> validConditions() {
        Arbitrary<String> fieldName = Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(3)
            .ofMaxLength(15)
            .map(s -> s + "_field");
        
        Arbitrary<String> value = Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(10);
        
        return Combinators.combine(fieldName, value)
            .as((field, val) -> field + " = '" + val + "'");
    }

    /**
     * 生成括号不匹配的条件表达式
     */
    @Provide
    Arbitrary<String> conditionsWithUnmatchedParentheses() {
        return Arbitraries.of(
            "(status = 'active'",
            "status = 'active')",
            "((status = 'active')",
            "(status = 'active'))",
            "status = 'active' AND (type = 'A'"
        );
    }

    /**
     * 生成包含SQL关键字的数学表达式
     */
    @Provide
    Arbitrary<String> expressionsWithKeywords() {
        return Arbitraries.of(
            "[metric1] + SELECT",
            "[metric1] * DELETE",
            "DROP + [metric2]",
            "[metric1] / UNION"
        );
    }

    /**
     * 生成有效的数学表达式
     */
    @Provide
    Arbitrary<String> validExpressions() {
        return Arbitraries.of(
            "[metric1] + [metric2]",
            "[metric1] - [metric2]",
            "[metric1] * [metric2]",
            "[metric1] / [metric2]",
            "([metric1] + [metric2]) / [metric3]",
            "[metric1] * 100",
            "([metric1] - [metric2]) * 0.5"
        );
    }

    /**
     * 生成无效的聚合函数
     */
    @Provide
    Arbitrary<String> invalidAggregations() {
        return Arbitraries.of(
            "MEDIAN", "MODE", "STDDEV", "VARIANCE", 
            "CONCAT", "SUBSTRING", "UPPER", "LOWER",
            "EXEC", "EXECUTE", "SCRIPT"
        );
    }

    /**
     * 生成无效的指标引用测试用例
     */
    @Provide
    Arbitrary<MetricReferenceTestCase> invalidMetricReferences() {
        Arbitrary<Set<String>> availableMetrics = Arbitraries.of(
            new HashSet<>(Arrays.asList("metric1", "metric2", "metric3"))
        );
        
        Arbitrary<Set<String>> referencedMetrics = Arbitraries.of(
            new HashSet<>(Arrays.asList("metric1", "nonexistent")),
            new HashSet<>(Arrays.asList("invalid_metric")),
            new HashSet<>(Arrays.asList("metric1", "metric2", "unknown"))
        );
        
        return Combinators.combine(referencedMetrics, availableMetrics)
            .as(MetricReferenceTestCase::new);
    }

    /**
     * 测试用例类: 指标引用验证
     */
    static class MetricReferenceTestCase {
        final Set<String> referencedMetrics;
        final Set<String> availableMetrics;

        MetricReferenceTestCase(Set<String> referencedMetrics, Set<String> availableMetrics) {
            this.referencedMetrics = referencedMetrics;
            this.availableMetrics = availableMetrics;
        }
    }
}
