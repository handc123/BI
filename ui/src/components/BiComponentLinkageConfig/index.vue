<template>
  <div class="component-linkage-config">
    <div v-if="linkages.length === 0" class="empty-state">
      <p>暂无联动配置</p>
      <el-button size="small" type="primary" @click="addLinkage">添加联动</el-button>
    </div>
    <div v-else class="linkage-list">
      <el-card
        v-for="(linkage, index) in linkages"
        :key="index"
        shadow="hover"
        class="linkage-item"
      >
        <div slot="header" class="linkage-header">
          <span>联动 {{ index + 1 }}</span>
          <el-button
            type="text"
            size="mini"
            icon="el-icon-delete"
            @click="removeLinkage(index)"
          />
        </div>
        
        <el-form :model="linkage" label-width="80px" size="small">
          <el-form-item label="源组件">
            <el-select
              v-model="linkage.source"
              placeholder="选择源组件"
              style="width: 100%"
              @change="handleSourceChange(linkage)"
            >
              <el-option
                v-for="comp in availableSourceComponents"
                :key="comp.i"
                :label="comp.name"
                :value="comp.i"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="目标组件">
            <el-select
              v-model="linkage.target"
              placeholder="选择目标组件"
              style="width: 100%"
              multiple
            >
              <el-option
                v-for="comp in getAvailableTargets(linkage.source)"
                :key="comp.i"
                :label="comp.name"
                :value="comp.i"
              />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">字段映射</el-divider>

          <div class="field-mapping">
            <div
              v-for="(mapping, mapIndex) in linkage.fieldMapping"
              :key="mapIndex"
              class="mapping-item"
            >
              <el-row :gutter="10">
                <el-col :span="10">
                  <el-input
                    v-model="mapping.sourceField"
                    placeholder="源字段"
                    size="small"
                  >
                    <template slot="prepend">源</template>
                  </el-input>
                </el-col>
                <el-col :span="2" style="text-align: center; line-height: 32px">
                  <i class="el-icon-right"></i>
                </el-col>
                <el-col :span="10">
                  <el-input
                    v-model="mapping.targetField"
                    placeholder="目标字段"
                    size="small"
                  >
                    <template slot="prepend">目标</template>
                  </el-input>
                </el-col>
                <el-col :span="2">
                  <el-button
                    type="text"
                    size="mini"
                    icon="el-icon-delete"
                    @click="removeMapping(linkage, mapIndex)"
                  />
                </el-col>
              </el-row>
            </div>
            <el-button
              size="small"
              type="text"
              icon="el-icon-plus"
              @click="addMapping(linkage)"
            >添加映射</el-button>
          </div>

          <el-form-item label="联动方式">
            <el-radio-group v-model="linkage.triggerType">
              <el-radio label="click">点击触发</el-radio>
              <el-radio label="select">选择触发</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="清除方式">
            <el-radio-group v-model="linkage.clearType">
              <el-radio label="manual">手动清除</el-radio>
              <el-radio label="auto">自动清除</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="启用状态">
            <el-switch v-model="linkage.enabled" />
          </el-form-item>
        </el-form>
      </el-card>

      <el-button
        size="small"
        type="dashed"
        icon="el-icon-plus"
        style="width: 100%; margin-top: 10px"
        @click="addLinkage"
      >添加新联动</el-button>
    </div>

    <!-- 联动预览 -->
    <el-divider content-position="left">联动关系图</el-divider>
    <div class="linkage-graph">
      <div v-if="linkages.length === 0" class="empty-graph">
        <i class="el-icon-share"></i>
        <p>暂无联动关系</p>
      </div>
      <div v-else class="graph-container">
        <div
          v-for="(linkage, index) in linkages"
          :key="index"
          class="graph-item"
        >
          <div class="graph-source">
            {{ getComponentName(linkage.source) }}
          </div>
          <div class="graph-arrow">
            <i class="el-icon-right"></i>
          </div>
          <div class="graph-targets">
            <div
              v-for="target in linkage.target"
              :key="target"
              class="graph-target"
            >
              {{ getComponentName(target) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BiComponentLinkageConfig',
  props: {
    componentId: {
      type: String,
      default: null
    },
    components: {
      type: Array,
      default: () => []
    },
    value: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      linkages: []
    }
  },
  computed: {
    availableSourceComponents() {
      // 可以作为源的组件（通常是图表类组件）
      return this.components.filter(comp => {
        return ['line', 'bar', 'pie', 'donut', 'table', 'map'].includes(comp.type)
      })
    }
  },
  watch: {
    value: {
      handler(val) {
        this.linkages = val || []
      },
      immediate: true,
      deep: true
    },
    linkages: {
      handler(val) {
        this.$emit('input', val)
      },
      deep: true
    }
  },
  methods: {
    addLinkage() {
      this.linkages.push({
        source: '',
        target: [],
        fieldMapping: [],
        triggerType: 'click',
        clearType: 'manual',
        enabled: true
      })
    },
    removeLinkage(index) {
      this.$confirm('确定要删除此联动配置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.linkages.splice(index, 1)
        this.$message.success('联动配置已删除')
      }).catch(() => {})
    },
    handleSourceChange(linkage) {
      // 清空目标组件和字段映射
      linkage.target = []
      linkage.fieldMapping = []
    },
    getAvailableTargets(sourceId) {
      // 排除源组件本身
      return this.components.filter(comp => comp.i !== sourceId)
    },
    addMapping(linkage) {
      if (!linkage.fieldMapping) {
        this.$set(linkage, 'fieldMapping', [])
      }
      linkage.fieldMapping.push({
        sourceField: '',
        targetField: ''
      })
    },
    removeMapping(linkage, index) {
      linkage.fieldMapping.splice(index, 1)
    },
    getComponentName(componentId) {
      const comp = this.components.find(c => c.i === componentId)
      return comp ? comp.name : '未知组件'
    }
  }
}
</script>

<style scoped>
.component-linkage-config {
  padding: 10px;
}

.empty-state {
  text-align: center;
  padding: 30px;
  color: #909399;
}

.linkage-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.linkage-item {
  margin-bottom: 10px;
}

.linkage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.field-mapping {
  margin-bottom: 15px;
}

.mapping-item {
  margin-bottom: 10px;
}

.linkage-graph {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.empty-graph {
  text-align: center;
  padding: 30px;
  color: #909399;
}

.empty-graph i {
  font-size: 48px;
  margin-bottom: 10px;
}

.graph-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.graph-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.graph-source {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border-radius: 4px;
  font-size: 14px;
  white-space: nowrap;
}

.graph-arrow {
  margin: 0 15px;
  font-size: 20px;
  color: #909399;
}

.graph-targets {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.graph-target {
  padding: 8px 16px;
  background: #67c23a;
  color: white;
  border-radius: 4px;
  font-size: 14px;
  white-space: nowrap;
}
</style>
