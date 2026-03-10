/**
 * 从指标表达式中提取可展示的口径条件（第一版）
 * 当前支持:
 * - IN (...) / NOT IN (...)
 * - =, !=, <>, >, >=, <, <=
 */
export function parseMetricConditions(expression) {
  if (!expression || typeof expression !== 'string') {
    return { ok: false, message: '表达式为空', conditions: [] }
  }

  const conditions = []
  const text = expression.replace(/\s+/g, ' ').trim()

  // 匹配 field [NOT] IN (...)
  const inRegex = /([a-zA-Z_][a-zA-Z0-9_]*)\s+(NOT\s+IN|IN)\s*\(([^)]+)\)/ig
  let inMatch = null
  while ((inMatch = inRegex.exec(text)) !== null) {
    const field = inMatch[1]
    const op = inMatch[2].toUpperCase().replace(/\s+/g, ' ')
    const raw = inMatch[3]
    const values = raw.split(',').map(v => String(v).trim().replace(/^'+|'+$/g, '')).filter(Boolean)
    conditions.push({
      source: 'formula',
      field,
      operator: op,
      value: values,
      display: `${field} ${op === 'IN' ? '属于' : '不属于'} (${values.join('、')})`
    })
  }

  // 匹配简单比较 field op value（排除 CASE/THEN/ELSE 等关键字）
  const cmpRegex = /([a-zA-Z_][a-zA-Z0-9_]*)\s*(=|!=|<>|>=|<=|>|<)\s*('(?:[^']|\\')*'|[0-9]+(?:\.[0-9]+)?)/ig
  let cmpMatch = null
  while ((cmpMatch = cmpRegex.exec(text)) !== null) {
    const field = cmpMatch[1]
    if (/^(CASE|WHEN|THEN|ELSE|END|SUM|AVG|COUNT|MIN|MAX)$/i.test(field)) {
      continue
    }
    const operator = cmpMatch[2]
    const value = String(cmpMatch[3]).replace(/^'+|'+$/g, '')
    conditions.push({
      source: 'formula',
      field,
      operator,
      value,
      display: `${field} ${operator} ${value}`
    })
  }

  if (conditions.length === 0) {
    return { ok: false, message: '未识别到可解析条件', conditions: [] }
  }

  return { ok: true, conditions }
}

