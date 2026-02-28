package com.zjrcu.iras.bi.metric.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据脱敏工具类
 * 用于对敏感数据进行脱敏处理
 *
 * @author iras
 * @date 2025-02-26
 */
public class DataMaskingUtil {

    /**
     * 脱敏规则映射
     * key: 字段名关键词（不区分大小写）
     * value: 脱敏函数
     */
    private static final Map<String, Function<String, String>> MASKING_RULES = new HashMap<>();

    static {
        // 手机号脱敏：保留前3位和后4位
        MASKING_RULES.put("phone", DataMaskingUtil::maskPhone);
        MASKING_RULES.put("mobile", DataMaskingUtil::maskPhone);
        MASKING_RULES.put("telephone", DataMaskingUtil::maskPhone);

        // 邮箱脱敏：保留前2位和@后的域名
        MASKING_RULES.put("email", DataMaskingUtil::maskEmail);
        MASKING_RULES.put("mail", DataMaskingUtil::maskEmail);

        // 身份证号脱敏：保留前6位和后4位
        MASKING_RULES.put("id_card", DataMaskingUtil::maskIdCard);
        MASKING_RULES.put("idcard", DataMaskingUtil::maskIdCard);
        MASKING_RULES.put("identity", DataMaskingUtil::maskIdCard);

        // 银行卡号脱敏：保留前4位和后4位
        MASKING_RULES.put("bank", DataMaskingUtil::maskBankCard);
        MASKING_RULES.put("card", DataMaskingUtil::maskBankCard);
        MASKING_RULES.put("account", DataMaskingUtil::maskBankCard);

        // 密码脱敏：全部替换为*
        MASKING_RULES.put("password", DataMaskingUtil::maskAll);
        MASKING_RULES.put("passwd", DataMaskingUtil::maskAll);
        MASKING_RULES.put("pwd", DataMaskingUtil::maskAll);

        // 地址脱敏：保留前3个字和后3个字
        MASKING_RULES.put("address", DataMaskingUtil::maskAddress);
        MASKING_RULES.put("addr", DataMaskingUtil::maskAddress);

        // 姓名脱敏：保留姓，名只保留第一个字
        MASKING_RULES.put("name", DataMaskingUtil::maskName);
        MASKING_RULES.put("user", DataMaskingUtil::maskName);

        // IP地址脱敏：保留前两段
        MASKING_RULES.put("ip", DataMaskingUtil::maskIp);
    }

    /**
     * 手机号脱敏
     * 示例：13812345678 -> 138****5678
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone)) {
            return phone;
        }
        // 移除非数字字符
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() != 11) {
            return phone;
        }
        return cleaned.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 邮箱脱敏
     * 示例：zhangsan@example.com -> zh****@example.com
     */
    public static String maskEmail(String email) {
        if (isEmpty(email)) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return email;
        }
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        String maskedPrefix = prefix.substring(0, 2) + "****";
        return maskedPrefix + suffix;
    }

    /**
     * 身份证号脱敏
     * 示例：110101199001011234 -> 110101********1234
     */
    public static String maskIdCard(String idCard) {
        if (isEmpty(idCard)) {
            return idCard;
        }
        // 移除非数字字符
        String cleaned = idCard.replaceAll("[^0-9Xx]", "");
        if (cleaned.length() != 15 && cleaned.length() != 18) {
            return idCard;
        }
        if (cleaned.length() == 15) {
            return cleaned.replaceAll("(\\d{6})\\d{7}(\\d{2})", "$1********$2");
        } else {
            return cleaned.replaceAll("(\\d{6})\\d{8}(\\w{4})", "$1********$2");
        }
    }

    /**
     * 银行卡号脱敏
     * 示例：6222021234567890123 -> 6222***********0123
     */
    public static String maskBankCard(String cardNo) {
        if (isEmpty(cardNo)) {
            return cardNo;
        }
        // 移除非数字字符
        String cleaned = cardNo.replaceAll("[^0-9]", "");
        if (cleaned.length() < 8) {
            return cardNo;
        }
        int length = cleaned.length();
        String prefix = cleaned.substring(0, 4);
        String suffix = cleaned.substring(length - 4);
        String middle = "*".repeat(length - 8);
        return prefix + middle + suffix;
    }

    /**
     * 地址脱敏
     * 示例：北京市朝阳区某某街道123号 -> 北京市***街道***号
     */
    public static String maskAddress(String address) {
        if (isEmpty(address) || address.length() < 6) {
            return address;
        }
        int length = address.length();
        String prefix = address.substring(0, 3);
        String suffix = address.substring(length - 3);
        String middle = "*".repeat(Math.max(3, length - 6));
        return prefix + middle + suffix;
    }

    /**
     * 姓名脱敏
     * 示例：张三 -> 张*；王小明 -> 王*明
     */
    public static String maskName(String name) {
        if (isEmpty(name)) {
            return name;
        }
        int length = name.length();
        if (length == 2) {
            return name.charAt(0) + "*";
        } else if (length == 3) {
            return name.charAt(0) + "*" + name.charAt(2);
        } else {
            return name.charAt(0) + "*" + name.substring(length - 1);
        }
    }

    /**
     * IP地址脱敏
     * 示例：192.168.1.100 -> 192.168.*.*
     */
    public static String maskIp(String ip) {
        if (isEmpty(ip)) {
            return ip;
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }
        return parts[0] + "." + parts[1] + ".*.*";
    }

    /**
     * 全部脱敏（用于密码等）
     * 示例：123456 -> ******
     */
    public static String maskAll(String value) {
        if (isEmpty(value)) {
            return value;
        }
        return "*".repeat(value.length());
    }

    /**
     * 对单个值进行脱敏
     *
     * @param value 原始值
     * @param fieldName 字段名（用于匹配脱敏规则）
     * @return 脱敏后的值
     */
    public static Object maskValue(Object value, String fieldName) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof String)) {
            return value;
        }

        String strValue = (String) value;
        String lowerFieldName = fieldName.toLowerCase();

        // 查找匹配的脱敏规则
        for (Map.Entry<String, Function<String, String>> entry : MASKING_RULES.entrySet()) {
            if (lowerFieldName.contains(entry.getKey())) {
                return entry.getValue().apply(strValue);
            }
        }

        // 没有匹配的规则，返回原值
        return strValue;
    }

    /**
     * 对 Map 中的敏感数据进行脱敏
     *
     * @param data 原始数据
     * @return 脱敏后的数据
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> maskSensitiveData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        Map<String, Object> masked = new HashMap<>(data);

        for (String key : masked.keySet()) {
            Object value = masked.get(key);

            if (value == null) {
                continue;
            }

            // 处理字符串类型
            if (value instanceof String) {
                masked.put(key, maskValue(value, key));
            }
            // 处理嵌套的 Map
            else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                masked.put(key, maskSensitiveData(nestedMap));
            }
            // 处理 List
            else if (value instanceof List) {
                List<?> list = (List<?>) value;
                List<Object> newList = new java.util.ArrayList<>(list.size());
                for (Object item : list) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        newList.add(maskSensitiveData(itemMap));
                    } else if (item instanceof String) {
                        newList.add(maskValue(item, key));
                    } else {
                        newList.add(item);
                    }
                }
                masked.put(key, newList);
            }
        }

        return masked;
    }

    /**
     * 检查字符串是否为空
     */
    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 添加自定义脱敏规则
     *
     * @param keyword 字段名关键词
     * @param maskingFunction 脱敏函数
     */
    public static void addMaskingRule(String keyword, Function<String, String> maskingFunction) {
        if (keyword != null && maskingFunction != null) {
            MASKING_RULES.put(keyword.toLowerCase(), maskingFunction);
        }
    }

    /**
     * 移除脱敏规则
     *
     * @param keyword 字段名关键词
     */
    public static void removeMaskingRule(String keyword) {
        if (keyword != null) {
            MASKING_RULES.remove(keyword.toLowerCase());
        }
    }

    /**
     * 判断是否需要脱敏
     *
     * @param fieldName 字段名
     * @return 是否需要脱敏
     */
    public static boolean needsMasking(String fieldName) {
        if (isEmpty(fieldName)) {
            return false;
        }
        String lowerFieldName = fieldName.toLowerCase();
        return MASKING_RULES.keySet().stream()
                .anyMatch(lowerFieldName::contains);
    }

    /**
     * 批量脱敏（用于导出场景）
     *
     * @param dataList 数据列表
     * @param maskFields 需要脱敏的字段列表（为空时自动识别）
     * @return 脱敏后的数据列表
     */
    public static List<Map<String, Object>> batchMaskSensitiveData(
            List<Map<String, Object>> dataList, List<String> maskFields) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }

        return dataList.stream()
                .map(data -> {
                    if (maskFields != null && !maskFields.isEmpty()) {
                        // 只脱敏指定字段
                        Map<String, Object> masked = new HashMap<>(data);
                        for (String field : maskFields) {
                            Object value = masked.get(field);
                            if (value instanceof String) {
                                masked.put(field, maskValue(value, field));
                            }
                        }
                        return masked;
                    } else {
                        // 自动识别并脱敏
                        return maskSensitiveData(data);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
