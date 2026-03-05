# Backend Restart Instructions

## Issue
Two fixes have been applied to support Chinese characters in SQL expressions:
1. **Character validation** updated to allow string literals with Unicode content
2. **Quote normalization** to convert smart quotes to standard SQL quotes

The code has been compiled successfully, but you need to restart the backend to load the new changes.

## Steps to Restart

### Option 1: Using the batch file (Windows)
```bash
# Stop the current backend (Ctrl+C if running in terminal)
# Then restart:
ry.bat
```

### Option 2: Using Maven directly
```bash
# Navigate to iras-admin directory
cd iras-admin

# Run the application
mvn spring-boot:run
```

### Option 3: Using your IDE
1. Stop the current running instance of `IrasApplication`
2. Run `IrasApplication.java` again from `iras-admin/src/main/java/com/zjrcu/iras/IrasApplication.java`

## Verification

After restarting, test the metric with Chinese characters again:

1. Create or edit a calculated field with: `SUM(CASE WHEN wjfl in '不良' THEN jkye ELSE 0 END) / SUM(jkye)`
2. The validation should pass (no "非法字符" error)
3. The query should execute successfully (no MySQL syntax error)
4. Check the logs to see the normalized SQL being generated

## What Was Fixed

### Fix 1: Character Validation (SQLValidator)
The validator now:
- Removes string literals before character validation
- Allows any Unicode characters inside quotes (including Chinese)
- Supports commas for IN clauses
- Focuses on SQL structure safety rather than character restrictions

### Fix 2: Quote Normalization (SQLDialectAdapter)
The adapter now automatically converts smart quotes to standard quotes:
- `'` (U+2018) → `'` (standard single quote)
- `'` (U+2019) → `'` (standard single quote)  
- `"` (U+201C) → `"` (standard double quote)
- `"` (U+201D) → `"` (standard double quote)
- Chinese quotation marks are also normalized

This happens transparently in:
- `generateConditionalRatio()` - for conditional ratio metrics
- `generateConditionalSum()` - for conditional sum metrics

## Expected Result

Before fixes:
```
Error 1: 数学表达式包含非法字符
Error 2: You have an error in your SQL syntax near ''不良'
```

After fixes:
```sql
SUM(CASE WHEN wjfl in '不良' THEN `jkye` ELSE 0 END) / NULLIF(SUM(`jkye`), 0) AS `npl_ratio`
-- Success: Validation passes and query executes correctly
```

## Troubleshooting

If you still see errors after restarting:

1. **Verify the restart**: Check the application startup logs for the timestamp
2. **Clear browser cache**: The frontend might be caching old error messages
3. **Check the expression**: Make sure it's using the calculated field format, not the metric config format
4. **Enable DEBUG logging**: Set `logging.level.com.zjrcu.iras.bi.platform=DEBUG` in `application.yml` to see the generated SQL
