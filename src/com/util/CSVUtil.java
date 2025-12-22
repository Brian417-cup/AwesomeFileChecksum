package com.util;

public class CSVUtil {

    /**
     * 辅助方法：根据 CSV 规则格式化值。
     * 如果值中包含逗号、换行或双引号，则需要使用双引号包裹，并将内部的双引号转义为两个双引号。
     * * @param value 要格式化的字符串。
     *
     * @return 符合 CSV 格式的字符串。
     */
    public static String formatCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        // 检查值是否需要双引号包裹：如果包含逗号、双引号或换行符
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {

            // 1. 将内部的所有双引号转义为两个双引号 (" -> "")
            // 注意：Java 的 replace 会替换所有匹配项（类似 C# 的 Replace）
            String escapedValue = value.replace("\"", "\"\"");

            // 2. 使用双引号包裹整个值
            return "\"" + escapedValue + "\"";
        }

        return value;
    }
}
