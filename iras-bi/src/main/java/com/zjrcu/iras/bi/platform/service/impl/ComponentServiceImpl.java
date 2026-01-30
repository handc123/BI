package com.zjrcu.iras.bi.platform.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zjrcu.iras.bi.platform.domain.DashboardComponent;
import com.zjrcu.iras.bi.platform.domain.dto.ComponentPosition;
import com.zjrcu.iras.bi.platform.mapper.ComponentMapper;
import com.zjrcu.iras.bi.platform.service.IComponentService;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 仪表板组件Service业务层处理
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Service
public class ComponentServiceImpl implements IComponentService {
    @Autowired
    private ComponentMapper componentMapper;

    /**
     * 查询组件
     * 
     * @param id 组件主键
     * @return 组件
     */
    @Override
    public DashboardComponent selectComponentById(Long id) {
        return componentMapper.selectComponentById(id);
    }

    /**
     * 查询组件列表
     * 
     * @param component 组件
     * @return 组件
     */
    @Override
    public List<DashboardComponent> selectComponentList(DashboardComponent component) {
        return componentMapper.selectComponentList(component);
    }

    /**
     * 新增组件
     * 
     * @param component 组件
     * @return 结果
     */
    @Override
    public int insertComponent(DashboardComponent component) {
        // 验证必填字段
        if (component.getDashboardId() == null) {
            throw new ServiceException("仪表板ID不能为空");
        }
        if (StringUtils.isEmpty(component.getComponentType())) {
            throw new ServiceException("组件类型不能为空");
        }
        if (component.getPositionX() == null || component.getPositionY() == null) {
            throw new ServiceException("组件位置不能为空");
        }
        if (component.getWidth() == null || component.getHeight() == null) {
            throw new ServiceException("组件尺寸不能为空");
        }

        // 验证JSON配置
        validateJsonConfig(component.getDataConfig(), "数据配置");
        validateJsonConfig(component.getStyleConfig(), "样式配置");
        validateJsonConfig(component.getAdvancedConfig(), "高级配置");

        // 设置默认值
        if (component.getZIndex() == null) {
            component.setZIndex(0);
        }

        return componentMapper.insertComponent(component);
    }

    /**
     * 修改组件
     * 
     * @param component 组件
     * @return 结果
     */
    @Override
    public int updateComponent(DashboardComponent component) {
        // 验证组件存在
        DashboardComponent existing = componentMapper.selectComponentById(component.getId());
        if (existing == null) {
            throw new ServiceException("组件不存在");
        }

        // 验证JSON配置
        validateJsonConfig(component.getDataConfig(), "数据配置");
        validateJsonConfig(component.getStyleConfig(), "样式配置");
        validateJsonConfig(component.getAdvancedConfig(), "高级配置");

        return componentMapper.updateComponent(component);
    }

    /**
     * 批量删除组件
     * 
     * @param ids 需要删除的组件主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteComponentByIds(Long[] ids) {
        // 删除关联的条件映射会通过数据库外键级联删除
        return componentMapper.deleteComponentByIds(ids);
    }

    /**
     * 删除组件信息
     * 
     * @param id 组件主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteComponentById(Long id) {
        // 删除关联的条件映射会通过数据库外键级联删除
        return componentMapper.deleteComponentById(id);
    }

    /**
     * 批量更新组件位置
     * 
     * @param positions 组件位置列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdatePosition(List<ComponentPosition> positions) {
        int count = 0;
        for (ComponentPosition position : positions) {
            count += componentMapper.updateComponentPosition(position);
        }
        return count;
    }

    /**
     * 复制组件
     * 
     * @param id 组件主键
     * @return 新组件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DashboardComponent copyComponent(Long id) {
        DashboardComponent original = componentMapper.selectComponentById(id);
        if (original == null) {
            throw new ServiceException("组件不存在");
        }

        // 创建副本
        DashboardComponent copy = new DashboardComponent();
        copy.setDashboardId(original.getDashboardId());
        copy.setComponentType(original.getComponentType());
        copy.setComponentName(original.getComponentName() + " - 副本");
        // 位置偏移
        copy.setPositionX(original.getPositionX() + 20);
        copy.setPositionY(original.getPositionY() + 20);
        copy.setWidth(original.getWidth());
        copy.setHeight(original.getHeight());
        copy.setZIndex(original.getZIndex() + 1);
        copy.setDataConfig(original.getDataConfig());
        copy.setStyleConfig(original.getStyleConfig());
        copy.setAdvancedConfig(original.getAdvancedConfig());

        componentMapper.insertComponent(copy);

        return copy;
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
