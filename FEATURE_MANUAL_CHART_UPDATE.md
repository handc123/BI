# Feature: Manual Chart Update Button

## Overview
Added a "更新图表" (Update Chart) button in the DataConfig panel that allows users to manually trigger chart rendering after configuring dimensions and metrics.

## Changes Made

### 1. Added Update Button in DataConfig.vue

#### Button Location
- Position: Bottom of the DataConfig form
- Above a separator line for visual distinction
- Full width button for easy clicking

#### Button Features
```vue
<el-button
  type="primary"
  icon="el-icon-refresh"
  :loading="updating"
  :disabled="!canUpdate"
  @click="handleUpdateChart"
  style="width: 100%"
>
  更新图表
</el-button>
```

**Button States**:
- **Enabled**: When dataset is selected AND at least one metric is configured
- **Disabled**: When dataset is not selected OR no metrics configured
- **Loading**: Shows spinner while updating

#### Helper Text
When button is disabled, shows hint:
```
ℹ️ 请先配置数据集和至少一个指标
```

### 2. Computed Property: canUpdate

```javascript
canUpdate() {
  return this.dataConfig.datasetId && 
         this.dataConfig.metrics && 
         this.dataConfig.metrics.length > 0 &&
         this.dataConfig.metrics.some(m => m.field && m.aggregation)
}
```

**Validation Logic**:
- ✅ Dataset must be selected
- ✅ At least one metric must exist
- ✅ Metric must have both field and aggregation selected

### 3. Update Handler: handleUpdateChart()

```javascript
handleUpdateChart() {
  // 1. Validate configuration
  if (!this.validateConfig()) {
    this.$message.error('配置不完整，请检查错误提示')
    return
  }
  
  // 2. Set loading state
  this.updating = true
  
  // 3. Emit change event to trigger chart update
  this.$emit('change', this.dataConfig)
  
  // 4. Show success message
  this.$message.success('图表配置已更新')
  
  // 5. Reset loading state
  setTimeout(() => {
    this.updating = false
  }, 500)
}
```

### 4. Removed Auto-Update Watch

**Before** (Auto-update on every change):
```javascript
watch: {
  dataConfig: {
    handler(val) {
      this.validateConfig()
      this.$emit('change', val)  // ❌ Auto-emits on every change
    },
    deep: true
  }
}
```

**After** (Manual update only):
```javascript
watch: {
  // Removed dataConfig watch
  // User must click "更新图表" button to trigger update
}
```

## User Flow

### Step 1: Add Chart Component
1. Click "组件库" button
2. Select chart type
3. Chart appears on canvas with placeholder

### Step 2: Configure Dataset
1. Click on chart component
2. ConfigPanel opens on right
3. Click "数据配置" tab
4. Select dataset from dropdown
5. Fields load automatically

### Step 3: Add Dimensions (Optional)
1. Click "添加维度" button
2. Select dimension field
3. Choose axis (X-axis or Series)
4. **Note**: Button remains disabled (needs metrics)

### Step 4: Add Metrics (Required)
1. Click "添加指标" button
2. Select metric field
3. Choose aggregation (sum, avg, max, min, count, count_distinct)
4. **Note**: Button becomes enabled ✅

### Step 5: Click Update Button
1. Click "更新图表" button
2. Button shows loading spinner
3. Configuration is validated
4. Change event is emitted
5. Chart renders with data
6. Success message appears

## Data Flow

```
User clicks "更新图表"
  ↓
handleUpdateChart() validates config
  ↓
Emits @change event with dataConfig
  ↓
ConfigPanel receives change
  ↓
ConfigPanel emits config-change to designer
  ↓
designer.vue calls updateComponent()
  ↓
Vuex store updates component
  ↓
ChartWidget receives updated config
  ↓
ChartWidget watch triggers
  ↓
fetchData() calls API
  ↓
Chart renders ✨
```

## Benefits

### 1. User Control
- Users decide when to update the chart
- Prevents unnecessary API calls while configuring
- Clear feedback when configuration is incomplete

### 2. Performance
- No API calls until user is ready
- Reduces server load
- Faster configuration experience

### 3. Better UX
- Clear button state (enabled/disabled)
- Loading indicator during update
- Success/error messages
- Helper text when disabled

## Validation

### Button Enabled When:
- ✅ Dataset is selected
- ✅ At least one metric is configured
- ✅ Metric has field selected
- ✅ Metric has aggregation selected

### Button Disabled When:
- ❌ No dataset selected
- ❌ No metrics configured
- ❌ Metric missing field
- ❌ Metric missing aggregation

### Validation Errors Shown:
- "请选择数据集" - No dataset
- "请至少配置一个指标" - No metrics
- "维度X缺少字段" - Dimension missing field
- "维度X缺少轴配置" - Dimension missing axis
- "指标X缺少字段" - Metric missing field
- "指标X缺少聚合方法" - Metric missing aggregation

## Styling

### Button Section
```css
.update-chart-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e6e6e6;
}
```

### Helper Text
```css
.update-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
```

## Testing

### Test Case 1: Button Disabled State
1. Add chart component
2. Select dataset
3. **Expected**: Button is disabled
4. **Helper text**: "请先配置数据集和至少一个指标"

### Test Case 2: Button Enabled State
1. Add chart component
2. Select dataset
3. Add metric with field and aggregation
4. **Expected**: Button is enabled
5. **No helper text**

### Test Case 3: Successful Update
1. Configure dataset and metrics
2. Click "更新图表" button
3. **Expected**:
   - Button shows loading spinner
   - Success message: "图表配置已更新"
   - Network request to `/bi/dashboard/query/execute`
   - Chart renders with data

### Test Case 4: Validation Error
1. Add metric without selecting field
2. Click "更新图表" button
3. **Expected**:
   - Error message: "配置不完整，请检查错误提示"
   - Validation errors shown in alert
   - No API call

### Test Case 5: Multiple Updates
1. Configure and click "更新图表"
2. Change metric aggregation
3. Click "更新图表" again
4. **Expected**: Chart updates with new data

## Files Modified

1. ✅ `ui/src/components/ConfigPanel/DataConfig.vue`
   - Added update button
   - Added canUpdate computed property
   - Added handleUpdateChart method
   - Removed auto-update watch
   - Added styling for button section

2. ✅ `FEATURE_MANUAL_CHART_UPDATE.md` - This file

## Next Steps

1. **Test the button** - Click and verify API call
2. **Check validation** - Try clicking without metrics
3. **Verify chart updates** - Ensure data renders correctly
4. **Test error cases** - Invalid configuration, API errors

## Troubleshooting

### Button Always Disabled
**Check**:
- Is dataset selected?
- Is at least one metric added?
- Does metric have field selected?
- Does metric have aggregation selected?

### Button Click Does Nothing
**Check**:
- Browser console for errors
- Network tab for API call
- Vuex DevTools for state updates
- ChartWidget props in Vue DevTools

### Chart Doesn't Update
**Check**:
- Did API call succeed?
- Is response data valid?
- Is ChartWidget watch triggering?
- Are there errors in console?

## Summary

Added a manual "更新图表" button that:
- ✅ Gives users control over when to update
- ✅ Validates configuration before updating
- ✅ Shows clear enabled/disabled states
- ✅ Provides helpful feedback messages
- ✅ Prevents unnecessary API calls
- ✅ Improves overall user experience

**Now users must click the button to render the chart after configuring dimensions and metrics!**
