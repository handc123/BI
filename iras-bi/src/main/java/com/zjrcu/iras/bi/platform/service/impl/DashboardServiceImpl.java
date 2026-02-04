package com.zjrcu.iras.bi.platform.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zjrcu.iras.bi.platform.domain.Dashboard;
import com.zjrcu.iras.bi.platform.domain.DashboardComponent;
import com.zjrcu.iras.bi.platform.domain.QueryCondition;
import com.zjrcu.iras.bi.platform.domain.ConditionMapping;
import com.zjrcu.iras.bi.platform.domain.dto.DashboardConfig;
import com.zjrcu.iras.bi.platform.mapper.DashboardMapper;
import com.zjrcu.iras.bi.platform.mapper.ComponentMapper;
import com.zjrcu.iras.bi.platform.mapper.QueryConditionMapper;
import com.zjrcu.iras.bi.platform.mapper.ConditionMappingMapper;
import com.zjrcu.iras.bi.platform.service.IDashboardService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.StringUtils;
import com.zjrcu.iras.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表板Service业务层处理
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Service
public class DashboardServiceImpl implements IDashboardService {
    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private ComponentMapper componentMapper;

    @Autowired
    private QueryConditionMapper queryConditionMapper;

    @Autowired
    private ConditionMappingMapper conditionMappingMapper;

    /**
     * 查询仪表板
     * 
     * @param id 仪表板主键
     * @return 仪表板
     */
    @Override
    public Dashboard selectDashboardById(Long id) {
        return dashboardMapper.selectDashboardById(id);
    }

    /**
     * 查询仪表板列表
     * 
     * @param dashboard 仪表板
     * @return 仪表板
     */
    @Override
    public List<Dashboard> selectDashboardList(Dashboard dashboard) {
        return dashboardMapper.selectDashboardList(dashboard);
    }

    /**
     * 新增仪表板
     * 
     * @param dashboard 仪表板
     * @return 结果
     */
    @Override
    public int insertDashboard(Dashboard dashboard) {
        // 验证仪表板名称
        if (StringUtils.isEmpty(dashboard.getDashboardName())) {
            throw new ServiceException("仪表板名称不能为空");
        }

        // 检查名称重复
        Dashboard existing = dashboardMapper.selectDashboardByName(dashboard.getDashboardName());
        if (existing != null) {
            throw new ServiceException("仪表板名称已存在");
        }

        // 验证JSON配置
        validateJsonConfig(dashboard.getCanvasConfig(), "画布配置");
        validateJsonConfig(dashboard.getGlobalStyle(), "全局样式配置");

        // 设置默认值
        if (StringUtils.isEmpty(dashboard.getTheme())) {
            dashboard.setTheme("light");
        }
        if (StringUtils.isEmpty(dashboard.getStatus())) {
            dashboard.setStatus("0"); // 默认草稿状态
        }
        if (dashboard.getPublishedVersion() == null) {
            dashboard.setPublishedVersion(0);
        }
        
        // 设置创建人
        if (StringUtils.isEmpty(dashboard.getCreateBy())) {
            try {
                dashboard.setCreateBy(SecurityUtils.getUsername());
            } catch (Exception e) {
                // 如果获取当前用户失败，使用默认值
                dashboard.setCreateBy("system");
            }
        }

        return dashboardMapper.insertDashboard(dashboard);
    }

    /**
     * 修改仪表板
     * 
     * @param dashboard 仪表板
     * @return 结果
     */
    @Override
    public int updateDashboard(Dashboard dashboard) {
        // 验证仪表板存在
        Dashboard existing = dashboardMapper.selectDashboardById(dashboard.getId());
        if (existing == null) {
            throw new ServiceException("仪表板不存在");
        }

        // 如果修改名称,检查名称重复
        if (StringUtils.isNotEmpty(dashboard.getDashboardName()) 
            && !dashboard.getDashboardName().equals(existing.getDashboardName())) {
            Dashboard duplicate = dashboardMapper.selectDashboardByName(dashboard.getDashboardName());
            if (duplicate != null && !duplicate.getId().equals(dashboard.getId())) {
                throw new ServiceException("仪表板名称已存在");
            }
        }

        // 验证JSON配置
        validateJsonConfig(dashboard.getCanvasConfig(), "画布配置");
        validateJsonConfig(dashboard.getGlobalStyle(), "全局样式配置");

        return dashboardMapper.updateDashboard(dashboard);
    }

    /**
     * 批量删除仪表板
     * 
     * @param ids 需要删除的仪表板主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDashboardByIds(Long[] ids) {
        // 删除关联的组件、查询条件和条件映射会通过数据库外键级联删除
        return dashboardMapper.deleteDashboardByIds(ids);
    }

    /**
     * 删除仪表板信息
     * 
     * @param id 仪表板主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDashboardById(Long id) {
        // 删除关联的组件、查询条件和条件映射会通过数据库外键级联删除
        return dashboardMapper.deleteDashboardById(id);
    }

    /**
     * 获取仪表板完整配置
     * 
     * @param id 仪表板主键
     * @return 仪表板配置
     */
    @Override
    public DashboardConfig getDashboardConfig(Long id) {
        Dashboard dashboard = dashboardMapper.selectDashboardById(id);
        if (dashboard == null) {
            throw new ServiceException("仪表板不存在");
        }

        DashboardConfig config = new DashboardConfig();
        config.setDashboard(dashboard);
        config.setComponents(componentMapper.selectComponentByDashboardId(id));
        config.setQueryConditions(queryConditionMapper.selectConditionByDashboardId(id));
        config.setConditionMappings(conditionMappingMapper.selectMappingByDashboardId(id));

        return config;
    }

    /**
     * 保存仪表板完整配置
     * 
     * @param dashboardId 仪表板主键
     * @param config 仪表板配置
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveDashboardConfig(Long dashboardId, DashboardConfig config) {
        // 验证仪表板存在
        Dashboard dashboard = dashboardMapper.selectDashboardById(dashboardId);
        if (dashboard == null) {
            throw new ServiceException("仪表板不存在");
        }

        try {
            // 更新仪表板基本信息
            if (config.getDashboard() != null) {
                config.getDashboard().setId(dashboardId);
                dashboardMapper.updateDashboard(config.getDashboard());
            }

            // 删除旧组件
            componentMapper.deleteComponentByDashboardId(dashboardId);

            // 插入新组件
            Map<String, Long> compMap = new HashMap<>();
            if (config.getComponents() != null) {
                for (DashboardComponent component : config.getComponents()) {
                    String tempId = component.getTempId();
                    component.setDashboardId(dashboardId);
                    componentMapper.insertComponent(component);
                    if (StringUtils.isNotEmpty(tempId)) {
                        compMap.put(tempId, component.getId());
                    }
                }
            }

            // 删除旧查询条件
            queryConditionMapper.deleteConditionByDashboardId(dashboardId);

            // 插入新查询条件
            Map<String, Long> condMap = new HashMap<>();
            if (config.getQueryConditions() != null) {
                for (QueryCondition condition : config.getQueryConditions()) {
                    String tempId = condition.getTempId();
                    condition.setDashboardId(dashboardId);
                    queryConditionMapper.insertCondition(condition);
                    if (StringUtils.isNotEmpty(tempId)) {
                        condMap.put(tempId, condition.getId());
                    }
                }
            }

            // 删除旧条件映射
            conditionMappingMapper.deleteMappingByDashboardId(dashboardId);

            // 插入新条件映射
            if (config.getConditionMappings() != null) {
                for (ConditionMapping mapping : config.getConditionMappings()) {
                    // 强制根据 tempId 转换，忽略前端传来的原始 Long ID (防止外键冲突)
                    Long realCompId = StringUtils.isNotEmpty(mapping.getTempComponentId()) 
                        ? compMap.get(mapping.getTempComponentId()) 
                        : null;
                    Long realCondId = StringUtils.isNotEmpty(mapping.getTempConditionId()) 
                        ? condMap.get(mapping.getTempConditionId()) 
                        : null;
                    
                    mapping.setComponentId(realCompId);
                    mapping.setConditionId(realCondId);
                    
                    // 只有当两个核心关联ID都成功转换后才插入
                    if (mapping.getComponentId() != null && mapping.getConditionId() != null) {
                        mapping.setId(null); // 确保是新插入
                        conditionMappingMapper.insertMapping(mapping);
                    } else {
                        // 打印警告日志或静默跳过
                        System.err.println("跳过映射保存: 关联ID转换失败. tempCompId=" + 
                            mapping.getTempComponentId() + ", tempCondId=" + mapping.getTempConditionId());
                    }
                }
            }

            return 1;
        } catch (Exception e) {
            throw new ServiceException("保存配置失败: " + e.getMessage());
        }
    }

    /**
     * 发布仪表板
     * 
     * @param id 仪表板主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishDashboard(Long id) {
        Dashboard dashboard = dashboardMapper.selectDashboardById(id);
        if (dashboard == null) {
            throw new ServiceException("仪表板不存在");
        }

        // 验证仪表板配置完整性
        if (StringUtils.isEmpty(dashboard.getDashboardName())) {
            throw new ServiceException("仪表板名称不能为空,无法发布");
        }

        // 检查是否有组件
        List<DashboardComponent> components = componentMapper.selectComponentByDashboardId(id);
        if (components == null || components.isEmpty()) {
            throw new ServiceException("仪表板没有组件,无法发布");
        }

        // 增加版本号并更新状态为已发布
        Integer newVersion = dashboard.getPublishedVersion() + 1;
        
        // 更新状态为已发布(1)
        dashboard.setStatus("1");
        dashboard.setPublishedVersion(newVersion);
        
        int result = dashboardMapper.publishDashboard(id, newVersion);
        
        if (result > 0) {
            // 发布成功后,可以在这里添加发布通知、审计日志等逻辑
            // TODO: 添加审计日志记录
        }
        
        return result;
    }

    /**
     * 取消发布仪表板(恢复为草稿状态)
     * 
     * @param id 仪表板主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unpublishDashboard(Long id) {
        Dashboard dashboard = dashboardMapper.selectDashboardById(id);
        if (dashboard == null) {
            throw new ServiceException("仪表板不存在");
        }

        if (!"1".equals(dashboard.getStatus())) {
            throw new ServiceException("仪表板未发布,无需取消发布");
        }

        // 更新状态为草稿(0)
        dashboard.setStatus("0");
        return dashboardMapper.updateDashboard(dashboard);
    }

    /**
     * 编辑已发布的仪表板 - 创建草稿副本
     * 
     * @param id 仪表板主键
     * @return 草稿副本ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDraftFromPublished(Long id) {
        Dashboard published = dashboardMapper.selectDashboardById(id);
        if (published == null) {
            throw new ServiceException("仪表板不存在");
        }

        if (!"1".equals(published.getStatus())) {
            throw new ServiceException("仪表板未发布,无需创建草稿副本");
        }

        // 创建草稿副本
        Dashboard draft = new Dashboard();
        draft.setDashboardName(published.getDashboardName() + " (草稿)");
        draft.setDashboardDesc(published.getDashboardDesc());
        draft.setTheme(published.getTheme());
        draft.setCanvasConfig(published.getCanvasConfig());
        draft.setGlobalStyle(published.getGlobalStyle());
        draft.setStatus("0"); // 草稿状态
        draft.setPublishedVersion(0);
        draft.setRemark("从已发布版本 " + published.getPublishedVersion() + " 创建的草稿");

        // 插入草稿仪表板
        dashboardMapper.insertDashboard(draft);
        Long draftId = draft.getId();

        // 复制组件
        List<DashboardComponent> components = componentMapper.selectComponentByDashboardId(id);
        if (components != null && !components.isEmpty()) {
            for (DashboardComponent component : components) {
                DashboardComponent draftComponent = new DashboardComponent();
                draftComponent.setDashboardId(draftId);
                draftComponent.setComponentType(component.getComponentType());
                draftComponent.setComponentName(component.getComponentName());
                draftComponent.setPositionX(component.getPositionX());
                draftComponent.setPositionY(component.getPositionY());
                draftComponent.setWidth(component.getWidth());
                draftComponent.setHeight(component.getHeight());
                draftComponent.setZIndex(component.getZIndex());
                draftComponent.setDataConfig(component.getDataConfig());
                draftComponent.setStyleConfig(component.getStyleConfig());
                draftComponent.setAdvancedConfig(component.getAdvancedConfig());
                componentMapper.insertComponent(draftComponent);
            }
        }

        // 复制查询条件
        List<QueryCondition> conditions = queryConditionMapper.selectConditionByDashboardId(id);
        if (conditions != null && !conditions.isEmpty()) {
            for (QueryCondition condition : conditions) {
                QueryCondition draftCondition = new QueryCondition();
                draftCondition.setDashboardId(draftId);
                draftCondition.setConditionName(condition.getConditionName());
                draftCondition.setConditionType(condition.getConditionType());
                draftCondition.setDisplayOrder(condition.getDisplayOrder());
                draftCondition.setIsRequired(condition.getIsRequired());
                draftCondition.setIsVisible(condition.getIsVisible());
                draftCondition.setDefaultValue(condition.getDefaultValue());
                draftCondition.setConfig(condition.getConfig());
                draftCondition.setParentConditionId(condition.getParentConditionId());
                queryConditionMapper.insertCondition(draftCondition);
            }
        }

        // 复制条件映射
        List<ConditionMapping> mappings = conditionMappingMapper.selectMappingByDashboardId(id);
        if (mappings != null && !mappings.isEmpty()) {
            for (ConditionMapping mapping : mappings) {
                ConditionMapping draftMapping = new ConditionMapping();
                draftMapping.setConditionId(mapping.getConditionId());
                draftMapping.setComponentId(mapping.getComponentId());
                draftMapping.setFieldName(mapping.getFieldName());
                draftMapping.setMappingType(mapping.getMappingType());
                conditionMappingMapper.insertMapping(draftMapping);
            }
        }

        return draftId;
    }

    /**
     * 验证JSON配置格式
     * 
     * @param jsonConfig JSON配置字符串
     * @param configName 配置名称
     */
    private void validateJsonConfig(String jsonConfig, String configName) {
        if (StringUtils.isNotEmpty(jsonConfig)) {
            try {
                JSONObject.parseObject(jsonConfig);
            } catch (Exception e) {
                throw new ServiceException(configName + "格式错误: " + e.getMessage());
            }
        }
    }
}
