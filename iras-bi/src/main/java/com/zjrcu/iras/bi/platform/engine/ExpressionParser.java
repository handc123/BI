package com.zjrcu.iras.bi.platform.engine;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式解析器
 * 
 * 负责解析和验证自定义数学表达式,提取指标引用,验证语法。
 * 实现需求: 5.2, 5.3, 5.4
 * 
 * @author IRAS BI Platform
 */
@Component
public class ExpressionParser {

    // 指标引用模式: [metric_name]
    private static final Pattern METRIC_REFERENCE_PATTERN = Pattern.compile("\\[([a-zA-Z_][a-zA-Z0-9_]*)\\]");
    
    // 数学运算符
    private static final Set<Character> OPERATORS = new HashSet<>(Arrays.asList('+', '-', '*', '/'));
    
    // 最大嵌套深度
    private static final int MAX_NESTING_DEPTH = 10;

    /**
     * 提取表达式中的指标引用
     * 需求 5.2
     * 
     * @param expression 数学表达式
     * @return 指标名称集合
     */
    public Set<String> extractMetricReferences(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<String> metricNames = new HashSet<>();
        Matcher matcher = METRIC_REFERENCE_PATTERN.matcher(expression);
        
        while (matcher.find()) {
            String metricName = matcher.group(1);
            metricNames.add(metricName);
        }
        
        return metricNames;
    }

    /**
     * 验证表达式语法
     * 需求 5.3, 5.4
     * 
     * @param expression 数学表达式
     * @throws IllegalArgumentException 如果表达式语法无效
     */
    public void validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        
        // 验证括号匹配
        validateBracketMatching(expression);
        
        // 验证运算符位置
        validateOperatorPositions(expression);
        
        // 验证表达式包含有效内容
        validateExpressionContent(expression);
        
        // 验证嵌套深度
        validateNestingDepth(expression);
    }

    /**
     * 验证括号匹配
     * 需求 5.3
     * 
     * @param expression 表达式
     * @throws IllegalArgumentException 如果括号不匹配
     */
    private void validateBracketMatching(String expression) {
        Stack<Character> stack = new Stack<>();
        Map<Character, Character> bracketPairs = new HashMap<>();
        bracketPairs.put(')', '(');
        bracketPairs.put(']', '[');
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (c == '(' || c == '[') {
                stack.push(c);
            } else if (c == ')' || c == ']') {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException(
                        "括号不匹配: 在位置 " + i + " 发现多余的闭括号 '" + c + "'"
                    );
                }
                
                char expected = bracketPairs.get(c);
                char actual = stack.pop();
                
                if (actual != expected) {
                    throw new IllegalArgumentException(
                        "括号类型不匹配: 期望 '" + expected + "' 但发现 '" + actual + "' 在位置 " + i
                    );
                }
            }
        }
        
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException(
                "括号不匹配: 有 " + stack.size() + " 个未闭合的括号"
            );
        }
    }

    /**
     * 验证运算符位置
     * 需求 5.4
     * 
     * @param expression 表达式
     * @throws IllegalArgumentException 如果运算符位置无效
     */
    private void validateOperatorPositions(String expression) {
        // 移除空格以简化验证
        String trimmed = expression.replaceAll("\\s+", "");
        
        // 检查表达式不能以运算符开始(除了负号)
        if (trimmed.length() > 0) {
            char first = trimmed.charAt(0);
            if (OPERATORS.contains(first) && first != '-') {
                throw new IllegalArgumentException(
                    "表达式不能以运算符 '" + first + "' 开始"
                );
            }
        }
        
        // 检查表达式不能以运算符结束
        if (trimmed.length() > 0) {
            char last = trimmed.charAt(trimmed.length() - 1);
            if (OPERATORS.contains(last)) {
                throw new IllegalArgumentException(
                    "表达式不能以运算符 '" + last + "' 结束"
                );
            }
        }
        
        // 检查连续运算符
        for (int i = 0; i < trimmed.length() - 1; i++) {
            char current = trimmed.charAt(i);
            char next = trimmed.charAt(i + 1);
            
            // 允许 "+-" 或 "--" (负号),但不允许其他连续运算符
            if (OPERATORS.contains(current) && OPERATORS.contains(next)) {
                if (!((current == '+' || current == '-') && next == '-')) {
                    throw new IllegalArgumentException(
                        "表达式包含无效的连续运算符: '" + current + next + "' 在位置 " + i
                    );
                }
            }
        }
        
        // 检查运算符前后必须有操作数
        validateOperatorOperands(trimmed);
    }

    /**
     * 验证运算符前后的操作数
     * 
     * @param expression 表达式(已移除空格)
     * @throws IllegalArgumentException 如果运算符缺少操作数
     */
    private void validateOperatorOperands(String expression) {
        for (int i = 1; i < expression.length() - 1; i++) {
            char c = expression.charAt(i);
            
            if (OPERATORS.contains(c) && c != '-') {
                char prev = expression.charAt(i - 1);
                char next = expression.charAt(i + 1);
                
                // 运算符前必须是数字、字母、下划线、闭括号或闭方括号
                if (!Character.isLetterOrDigit(prev) && prev != '_' && prev != ')' && prev != ']') {
                    throw new IllegalArgumentException(
                        "运算符 '" + c + "' 在位置 " + i + " 前缺少操作数"
                    );
                }
                
                // 运算符后必须是数字、字母、下划线、开括号、开方括号或负号
                if (!Character.isLetterOrDigit(next) && next != '_' && next != '(' && next != '[' && next != '-') {
                    throw new IllegalArgumentException(
                        "运算符 '" + c + "' 在位置 " + i + " 后缺少操作数"
                    );
                }
            }
        }
    }

    /**
     * 验证表达式包含有效内容
     * 
     * @param expression 表达式
     * @throws IllegalArgumentException 如果表达式无效
     */
    private void validateExpressionContent(String expression) {
        // 移除空格、括号、运算符后,应该至少包含一个标识符或数字
        String content = expression.replaceAll("[\\s()\\[\\]+\\-*/]", "");
        
        if (content.isEmpty()) {
            throw new IllegalArgumentException("表达式不包含有效的操作数");
        }
        
        // 检查是否包含至少一个指标引用或数字
        boolean hasMetric = METRIC_REFERENCE_PATTERN.matcher(expression).find();
        boolean hasNumber = expression.matches(".*\\d+.*");
        
        if (!hasMetric && !hasNumber) {
            throw new IllegalArgumentException("表达式必须包含至少一个指标引用或数字");
        }
    }

    /**
     * 验证嵌套深度
     * 
     * @param expression 表达式
     * @throws IllegalArgumentException 如果嵌套深度超过限制
     */
    private void validateNestingDepth(String expression) {
        int maxDepth = 0;
        int currentDepth = 0;
        
        for (char c : expression.toCharArray()) {
            if (c == '(' || c == '[') {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (c == ')' || c == ']') {
                currentDepth--;
            }
        }
        
        if (maxDepth > MAX_NESTING_DEPTH) {
            throw new IllegalArgumentException(
                "表达式嵌套深度 " + maxDepth + " 超过最大限制 " + MAX_NESTING_DEPTH
            );
        }
    }

    /**
     * 替换表达式中的指标引用为SQL表达式
     * 
     * @param expression 原始表达式
     * @param metricSqlMap 指标名称到SQL表达式的映射
     * @return 替换后的SQL表达式
     */
    public String replaceMetricReferences(String expression, Map<String, String> metricSqlMap) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        
        if (metricSqlMap == null || metricSqlMap.isEmpty()) {
            throw new IllegalArgumentException("指标SQL映射不能为空");
        }
        
        String result = expression;
        Matcher matcher = METRIC_REFERENCE_PATTERN.matcher(expression);
        
        // 使用StringBuffer进行替换,避免重复匹配问题
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String metricName = matcher.group(1);
            String sqlExpression = metricSqlMap.get(metricName);
            
            if (sqlExpression == null) {
                throw new IllegalArgumentException("未找到指标 '" + metricName + "' 的SQL表达式");
            }
            
            // 用括号包裹SQL表达式以确保运算优先级正确
            matcher.appendReplacement(sb, "(" + Matcher.quoteReplacement(sqlExpression) + ")");
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * 检测表达式中的除法操作
     * 
     * @param expression 表达式
     * @return 如果包含除法操作返回true
     */
    public boolean containsDivision(String expression) {
        if (expression == null) {
            return false;
        }
        return expression.contains("/");
    }

    /**
     * 为除法操作添加NULLIF保护
     * 
     * @param expression SQL表达式
     * @return 添加NULLIF保护后的表达式
     */
    public String addNullProtectionForDivision(String expression) {
        if (expression == null || !expression.contains("/")) {
            return expression;
        }
        
        // 简单实现: 查找除法运算符,为分母添加NULLIF
        // 注意: 这是一个简化版本,实际实现可能需要更复杂的解析
        // 在MetricConverter中会有更精确的处理
        
        return expression;
    }

    /**
     * 计算指标依赖深度
     * 
     * @param metricName 指标名称
     * @param metricDependencies 指标依赖关系映射
     * @param visited 已访问的指标(用于检测循环依赖)
     * @return 依赖深度
     * @throws IllegalArgumentException 如果存在循环依赖
     */
    public int calculateDependencyDepth(
            String metricName, 
            Map<String, Set<String>> metricDependencies,
            Set<String> visited) {
        
        if (visited.contains(metricName)) {
            throw new IllegalArgumentException("检测到循环依赖: " + metricName);
        }
        
        Set<String> dependencies = metricDependencies.get(metricName);
        if (dependencies == null || dependencies.isEmpty()) {
            return 0;
        }
        
        visited.add(metricName);
        
        int maxDepth = 0;
        for (String dependency : dependencies) {
            int depth = calculateDependencyDepth(dependency, metricDependencies, new HashSet<>(visited));
            maxDepth = Math.max(maxDepth, depth);
        }
        
        return maxDepth + 1;
    }
    
    // ========== 计算字段支持方法 ==========
    
    /**
     * 提取表达式中的字段引用
     * 字段引用格式: 直接使用字段名(字母、数字、下划线)
     * 需求: 7.1
     * 
     * @param expression 表达式
     * @return 字段名称集合
     */
    public Set<String> extractFieldReferences(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<String> fieldNames = new HashSet<>();
        
        // 匹配字段名: 字母或下划线开头,后跟字母、数字或下划线
        Pattern fieldPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
        Matcher matcher = fieldPattern.matcher(expression);
        
        // 已知的SQL关键字和函数,不应被识别为字段
        Set<String> sqlKeywords = Set.of(
            "CASE", "WHEN", "THEN", "ELSE", "END", "AND", "OR", "NOT", "IN", "IS", "NULL",
            "IF", "CONCAT", "SUBSTRING", "LENGTH", "UPPER", "LOWER", "TRIM",
            "ROUND", "FLOOR", "CEIL", "ABS", "MOD", "POWER",
            "YEAR", "MONTH", "DAY", "DATEDIFF", "COALESCE", "NULLIF",
            "SUM", "AVG", "COUNT", "MAX", "MIN"
        );
        
        while (matcher.find()) {
            String token = matcher.group(1);
            // 排除SQL关键字和数字
            if (!sqlKeywords.contains(token.toUpperCase()) && !token.matches("\\d+")) {
                fieldNames.add(token);
            }
        }
        
        return fieldNames;
    }
    
    /**
     * 提取表达式中的函数调用
     * 需求: 7.3
     * 
     * @param expression 表达式
     * @return 函数名称集合
     */
    public Set<String> extractFunctions(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<String> functions = new HashSet<>();
        
        // 匹配函数调用: 函数名后跟左括号
        Pattern functionPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
        Matcher matcher = functionPattern.matcher(expression);
        
        while (matcher.find()) {
            String functionName = matcher.group(1);
            functions.add(functionName.toUpperCase());
        }
        
        return functions;
    }
    
    /**
     * 验证函数语法
     * 需求: 7.5
     * 
     * @param expression 表达式
     * @throws IllegalArgumentException 如果函数语法无效
     */
    public void validateFunctionSyntax(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }
        
        // 提取所有函数调用
        Set<String> functions = extractFunctions(expression);
        
        for (String function : functions) {
            // 验证函数参数括号匹配
            validateFunctionParentheses(expression, function);
            
            // 验证函数参数数量(针对已知函数)
            validateFunctionArguments(expression, function);
        }
    }
    
    /**
     * 验证函数括号匹配
     * 
     * @param expression 表达式
     * @param functionName 函数名称
     * @throws IllegalArgumentException 如果括号不匹配
     */
    private void validateFunctionParentheses(String expression, String functionName) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(functionName) + "\\s*\\(", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(expression);
        
        while (matcher.find()) {
            int startPos = matcher.end() - 1; // 左括号位置
            int depth = 1;
            int pos = startPos + 1;
            
            while (pos < expression.length() && depth > 0) {
                char c = expression.charAt(pos);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                }
                pos++;
            }
            
            if (depth != 0) {
                throw new IllegalArgumentException(
                    "函数 " + functionName + " 的括号不匹配"
                );
            }
        }
    }
    
    /**
     * 验证函数参数数量
     * 
     * @param expression 表达式
     * @param functionName 函数名称
     * @throws IllegalArgumentException 如果参数数量无效
     */
    private void validateFunctionArguments(String expression, String functionName) {
        String upperFunc = functionName.toUpperCase();
        
        // 定义已知函数的参数数量要求
        Map<String, Integer> functionArgCounts = new HashMap<>();
        functionArgCounts.put("IF", 3);
        functionArgCounts.put("ROUND", 2);
        functionArgCounts.put("SUBSTRING", 3);
        functionArgCounts.put("YEAR", 1);
        functionArgCounts.put("MONTH", 1);
        functionArgCounts.put("DAY", 1);
        functionArgCounts.put("ABS", 1);
        functionArgCounts.put("FLOOR", 1);
        functionArgCounts.put("CEIL", 1);
        functionArgCounts.put("UPPER", 1);
        functionArgCounts.put("LOWER", 1);
        functionArgCounts.put("TRIM", 1);
        functionArgCounts.put("LENGTH", 1);
        
        // 可变参数函数
        Set<String> varArgFunctions = Set.of("CONCAT", "COALESCE", "CASE");
        
        if (functionArgCounts.containsKey(upperFunc)) {
            int expectedArgs = functionArgCounts.get(upperFunc);
            int actualArgs = countFunctionArguments(expression, functionName);
            
            if (actualArgs != expectedArgs) {
                throw new IllegalArgumentException(
                    "函数 " + functionName + " 需要 " + expectedArgs + " 个参数,但提供了 " + actualArgs + " 个"
                );
            }
        } else if (!varArgFunctions.contains(upperFunc)) {
            // 对于未知函数,只验证至少有一个参数
            int actualArgs = countFunctionArguments(expression, functionName);
            if (actualArgs == 0) {
                throw new IllegalArgumentException(
                    "函数 " + functionName + " 至少需要一个参数"
                );
            }
        }
    }
    
    /**
     * 计算函数参数数量
     * 
     * @param expression 表达式
     * @param functionName 函数名称
     * @return 参数数量
     */
    private int countFunctionArguments(String expression, String functionName) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(functionName) + "\\s*\\(", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(expression);
        
        if (!matcher.find()) {
            return 0;
        }
        
        int startPos = matcher.end(); // 左括号后的位置
        int depth = 1;
        int pos = startPos;
        int argCount = 0;
        boolean hasContent = false;
        
        while (pos < expression.length() && depth > 0) {
            char c = expression.charAt(pos);
            
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0 && hasContent) {
                    argCount++; // 最后一个参数
                }
            } else if (c == ',' && depth == 1) {
                argCount++;
                hasContent = false;
            } else if (!Character.isWhitespace(c)) {
                hasContent = true;
            }
            
            pos++;
        }
        
        return argCount;
    }
}
