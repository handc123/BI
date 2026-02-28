# 需求文档 - 智能画布布局系统

## 简介

智能画布布局系统为 IRAS Smart BI 仪表板设计器提供自动化的组件布局能力。系统在固定尺寸的画布区域内支持用户自由添加可视化组件(图表、卡片等),组件以每行最多两个的方式自动排列,并具备智能吸附对齐能力,确保布局整齐、间距一致,同时保留一定的自由拖拽调整空间。

## 术语表

- **Canvas(画布)**: 固定尺寸的设计区域,用于放置和排列可视化组件
- **Component(组件)**: 可视化元素,如图表、卡片、查询条件等
- **Grid_System(网格系统)**: 用于组件对齐和定位的虚拟网格结构
- **Snap_Alignment(吸附对齐)**: 拖拽组件时自动对齐到网格或其他组件的功能
- **Two_Column_Layout(双列布局)**: 每行最多显示两个组件的布局模式
- **Auto_Arrangement(自动排列)**: 系统自动计算和调整组件位置的功能
- **Drag_Operation(拖拽操作)**: 用户通过鼠标拖动组件改变其位置的交互
- **Visual_Feedback(视觉反馈)**: 拖拽过程中显示的对齐线、高亮区域等提示信息
- **Component_Spacing(组件间距)**: 组件之间的固定距离
- **Designer_Canvas**: 现有的画布组件实现 (ui/src/components/DesignerCanvas/index.vue)
- **Grid_Utils**: 网格计算工具模块 (ui/src/utils/gridUtils.js)
- **Alignment_Utils**: 对齐计算工具模块 (ui/src/utils/alignmentUtils.js)

## 需求

### 需求 1: 双列自动布局

**用户故事:** 作为仪表板设计者,我希望添加的组件能自动按双列布局排列,这样我可以快速创建整齐的仪表板而无需手动调整每个组件的位置。

#### 验收标准

1. WHEN 用户添加新组件到画布 THEN THE Layout_System SHALL 自动将组件放置在下一个可用的双列位置
2. WHEN 画布中已有奇数个组件 THEN THE Layout_System SHALL 将新组件放置在当前行的第二列
3. WHEN 画布中已有偶数个组件 THEN THE Layout_System SHALL 将新组件放置在新行的第一列
4. WHEN 组件被删除 THEN THE Layout_System SHALL 重新计算并调整后续组件的位置以填补空缺
5. THE Layout_System SHALL 保持所有组件的宽度一致(每个组件占画布宽度的约50%减去间距)
6. THE Layout_System SHALL 在组件之间保持固定的水平和垂直间距

### 需求 2: 智能吸附对齐

**用户故事:** 作为仪表板设计者,我希望在拖拽组件时能自动吸附到网格和其他组件,这样我可以轻松创建对齐整齐的布局。

#### 验收标准

1. WHEN 用户拖拽组件 THEN THE Snap_System SHALL 在组件接近网格线时自动吸附到网格
2. WHEN 拖拽的组件接近其他组件的边缘 THEN THE Snap_System SHALL 自动对齐到相邻组件的边缘
3. WHEN 组件吸附到对齐位置 THEN THE Snap_System SHALL 显示视觉对齐线指示对齐关系
4. THE Snap_System SHALL 支持水平对齐(左对齐、右对齐、中心对齐)
5. THE Snap_System SHALL 支持垂直对齐(顶部对齐、底部对齐、中心对齐)
6. WHERE 用户按住特定键(如 Shift) WHILE 拖拽组件 THEN THE Snap_System SHALL 禁用吸附功能以允许自由定位
7. THE Snap_System SHALL 设置吸附阈值(如 10 像素),只有在此范围内才触发吸附

### 需求 3: 一致的组件间距

**用户故事:** 作为仪表板设计者,我希望所有组件之间保持一致的间距,这样仪表板看起来更专业和整洁。

#### 验收标准

1. THE Spacing_System SHALL 在相邻组件之间保持固定的水平间距(如 16 像素)
2. THE Spacing_System SHALL 在上下组件之间保持固定的垂直间距(如 16 像素)
3. THE Spacing_System SHALL 在画布边缘和组件之间保持固定的边距(如 16 像素)
4. WHEN 组件自动排列时 THEN THE Spacing_System SHALL 应用标准间距规则
5. WHEN 用户手动调整组件位置时 THEN THE Spacing_System SHALL 通过吸附功能引导用户保持标准间距
6. WHERE 配置允许 THEN THE Spacing_System SHALL 支持自定义间距值

### 需求 4: 拖拽视觉反馈

**用户故事:** 作为仪表板设计者,我希望在拖拽组件时看到清晰的视觉提示,这样我可以准确了解组件将被放置的位置和对齐关系。

#### 验收标准

1. WHEN 用户开始拖拽组件 THEN THE Feedback_System SHALL 显示组件的半透明预览
2. WHEN 组件接近对齐位置 THEN THE Feedback_System SHALL 显示对齐线(虚线或实线)
3. WHEN 组件将吸附到网格 THEN THE Feedback_System SHALL 高亮显示目标网格区域
4. WHEN 组件与其他组件对齐 THEN THE Feedback_System SHALL 显示连接对齐线指示对齐关系
5. THE Feedback_System SHALL 使用不同颜色区分不同类型的对齐(网格对齐 vs 组件对齐)
6. WHEN 拖拽结束 THEN THE Feedback_System SHALL 立即清除所有视觉提示
7. THE Feedback_System SHALL 确保视觉反馈不影响拖拽性能(流畅的 60fps)

### 需求 5: 响应式画布行为

**用户故事:** 作为仪表板设计者,我希望当画布尺寸改变时布局能自动调整,这样我可以在不同屏幕尺寸下设计仪表板。

#### 验收标准

1. WHEN 画布宽度改变 THEN THE Layout_System SHALL 重新计算组件宽度以保持双列布局
2. WHEN 画布尺寸改变 THEN THE Layout_System SHALL 保持组件的相对位置和顺序
3. THE Layout_System SHALL 保持组件间距与画布尺寸的比例关系
4. WHEN 画布变窄导致无法容纳双列 THEN THE Layout_System SHALL 自动切换到单列布局
5. WHEN 画布恢复到足够宽度 THEN THE Layout_System SHALL 恢复双列布局
6. THE Layout_System SHALL 在尺寸变化时保持动画过渡效果以提供流畅的用户体验

### 需求 6: 不同尺寸组件处理

**用户故事:** 作为仪表板设计者,我希望系统能处理不同高度的组件,这样我可以创建更灵活和丰富的仪表板布局。

#### 验收标准

1. THE Layout_System SHALL 支持组件具有不同的高度
2. WHEN 组件高度不同 THEN THE Layout_System SHALL 将新组件放置在下一行而不是尝试填充高度差异
3. THE Layout_System SHALL 允许用户调整组件的高度
4. WHEN 用户调整组件高度 THEN THE Layout_System SHALL 不自动调整其他组件的位置
5. THE Snap_System SHALL 在垂直方向上支持对齐到不同高度组件的顶部、底部和中心
6. WHERE 用户启用高级布局模式 THEN THE Layout_System SHALL 支持组件跨列(占据整行)

### 需求 7: 手动调整自由度

**用户故事:** 作为仪表板设计者,我希望在自动布局的基础上能手动微调组件位置,这样我可以创建符合特定需求的自定义布局。

#### 验收标准

1. THE Layout_System SHALL 允许用户通过拖拽自由移动组件到任意位置
2. WHEN 用户手动移动组件 THEN THE Layout_System SHALL 不自动重排其他组件
3. THE Layout_System SHALL 提供"重置为自动布局"功能以恢复标准双列排列
4. WHEN 用户手动调整布局后添加新组件 THEN THE Layout_System SHALL 将新组件放置在画布底部
5. THE Layout_System SHALL 保存手动调整的位置信息到仪表板配置
6. WHEN 加载已保存的仪表板 THEN THE Layout_System SHALL 恢复所有组件的精确位置
7. THE Layout_System SHALL 提供"自动排列"按钮以重新应用双列自动布局

### 需求 8: 与现有系统集成

**用户故事:** 作为系统架构师,我希望智能布局系统能无缝集成到现有的仪表板设计器中,这样可以保持系统的一致性和可维护性。

#### 验收标准

1. THE Layout_System SHALL 集成到现有的 Designer_Canvas 组件中
2. THE Layout_System SHALL 使用 Vuex store (components.js) 管理组件状态
3. THE Layout_System SHALL 复用现有的 Grid_Utils 和 Alignment_Utils 工具模块
4. THE Layout_System SHALL 与现有的撤销/重做功能(history.js)兼容
5. THE Layout_System SHALL 保持与现有组件配置面板的交互
6. THE Layout_System SHALL 支持现有的组件类型(图表、查询条件、卡片等)
7. WHEN 布局改变 THEN THE Layout_System SHALL 触发适当的 Vuex mutations 以更新状态
8. THE Layout_System SHALL 保持与现有拖拽库(如 Vue-Draggable)的兼容性

### 需求 9: 性能优化

**用户故事:** 作为仪表板设计者,我希望布局系统在处理大量组件时仍然保持流畅,这样我可以创建复杂的仪表板而不会遇到性能问题。

#### 验收标准

1. THE Layout_System SHALL 在画布包含最多 50 个组件时保持流畅的拖拽性能(60fps)
2. THE Layout_System SHALL 使用防抖(debounce)优化频繁的布局计算
3. THE Layout_System SHALL 使用虚拟化技术处理大量对齐线的渲染
4. WHEN 执行批量操作(如删除多个组件) THEN THE Layout_System SHALL 批量更新布局而不是逐个处理
5. THE Layout_System SHALL 缓存网格计算结果以避免重复计算
6. THE Layout_System SHALL 使用 requestAnimationFrame 优化动画和视觉反馈
7. WHEN 画布不可见时 THEN THE Layout_System SHALL 暂停不必要的计算和渲染

### 需求 10: 配置和持久化

**用户故事:** 作为仪表板设计者,我希望布局配置能被保存和恢复,这样我可以在不同会话中继续编辑仪表板。

#### 验收标准

1. THE Layout_System SHALL 将组件位置信息序列化为 JSON 格式
2. THE Layout_System SHALL 保存布局模式(自动双列 vs 自由布局)到仪表板配置
3. THE Layout_System SHALL 保存网格设置(网格大小、吸附阈值)到用户偏好设置
4. WHEN 保存仪表板 THEN THE Layout_System SHALL 包含所有组件的位置、尺寸和层级信息
5. WHEN 加载仪表板 THEN THE Layout_System SHALL 准确恢复所有组件的布局状态
6. THE Layout_System SHALL 支持导出布局配置为独立文件
7. THE Layout_System SHALL 支持从配置文件导入布局设置
