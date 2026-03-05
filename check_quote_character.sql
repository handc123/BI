-- Script to check the actual character code in your expression
-- Run this to see what character is being used

SELECT 
    expression,
    HEX(SUBSTRING(expression, LOCATE('in', expression) + 3, 1)) as first_quote_hex,
    ASCII(SUBSTRING(expression, LOCATE('in', expression) + 3, 1)) as first_quote_ascii,
    CHAR_LENGTH(expression) as total_length
FROM 
    (SELECT 'SUM(CASE WHEN wjfl in '不良' THEN jkye ELSE 0 END) / SUM(jkye)' as expression) as test;

-- Also check from your actual database
-- Replace 'your_table' and 'your_column' with actual table/column names where the expression is stored
-- SELECT 
--     name,
--     expression,
--     HEX(SUBSTRING(expression, LOCATE('in', expression) + 3, 1)) as first_quote_hex,
--     ASCII(SUBSTRING(expression, LOCATE('in', expression) + 3, 1)) as first_quote_ascii
-- FROM your_table
-- WHERE name = 'npl_ratio';
