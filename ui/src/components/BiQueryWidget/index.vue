<template>
  <div class="query-widget" :style="widgetStyle">
    <div class="query-conditions" :class="layoutClass">
      <!-- 时间条件 -->
      <div
        v-for="condition in visibleConditions"
        :key="condition.id"
        class="condition-item"
        :class="{ 'is-required': condition.isRequired === '1' }"
      >
        <label class="condition-label">
          {{ condition.conditionName }}
          <span v-if="condition.isRequired === '1'" class="required-mark">*</span>
        </label>

        <!-- 时间选择器 -->
        <el-date-picker
          v-if="condition.conditionType === 'time'"
          v-model="conditionValues[condition.id]"
          :type="getTimePickerType(condition)"
          :format="getTimeFormat(condition)"
          :value-format="getTimeFormat(condition)"
          :placeholder="'请选择' + condition.conditionName"
          :clearable="condition.isRequired !== '1'"
          @change="handleConditionChange(condition)"
          class="condition-input"
        />

        <!-- 下拉选择器(单选) -->
        <el-select
          v-else-if="condition.conditionType === 'dropdown' && !isMultiple(condition)"
          v-model="conditionValues[condition.id]"
          :placeholder="'请选择' + condition.conditionName"
          :clearable="condition.isRequired !== '1'"
          :loading="loadingOptions[condition.id]"
          @change="handleConditionChange(condition)"
          class="condition-input"
        >
          <el-option
            v-for="option in conditionOptions[condition.id]"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <!-- 下拉选择器(多选) -->
        <el-select
          v-else-if="condition.conditionType === 'dropdown' && isMultiple(condition)"
          v-model="conditionValues[condition.id]