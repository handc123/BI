package com.zjrcu.iras.bi.platform.mapper;

import com.zjrcu.iras.bi.platform.domain.ComponentTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 组件模板Mapper接口
 * 
 * @author zjrcu
 * @date 2026-01-20
 */
@Mapper
public interface ComponentTemplateMapper {
    /**
     * 查询组件模板
     * 
     * @param id 组件模板主键
     * @return 组件模板
     */
    public ComponentTemplate selectTemplateById(Long id);

    /**
     * 查询组件模板列表
     * 
     * @param template 组件模板
     * @return 组件模板集合
     */
    public List<ComponentTemplate> selectTemplateList(ComponentTemplate template);

    /**
     * 新增组件模板
     * 
     * @param template 组件模板
     * @return 结果
     */
    public int insertTemplate(ComponentTemplate template);

    /**
     * 修改组件模板
     * 
     * @param template 组件模板
     * @return 结果
     */
    public int updateTemplate(ComponentTemplate template);

    /**
     * 删除组件模板
     * 
     * @param id 组件模板主键
     * @return 结果
     */
    public int deleteTemplateById(Long id);

    /**
     * 批量删除组件模板
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTemplateByIds(Long[] ids);
}
