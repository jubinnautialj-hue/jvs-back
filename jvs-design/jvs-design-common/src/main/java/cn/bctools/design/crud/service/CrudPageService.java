package cn.bctools.design.crud.service;

import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.data.fields.dto.FormSelectItemDto;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import cn.bctools.design.data.fields.dto.page.PageDesignHtml;
import cn.bctools.design.template.dto.PageTemplateDto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 列表的列表配置项
 *
 * @author auto
 */
public interface CrudPageService extends IService<CrudPage> {
    /**
     * 根据id获取设计数据, 并做空值校验
     *
     * @param pageId 列表页id
     * @return 列表页设计数据
     */
    CrudPage get(String pageId);

    /**
     * 更新设计
     *
     * @param id     设计id
     * @param design 设计
     * @return 设计
     */
    PageDesignHtml updateDesign(String id, PageDesignHtml design);

    /**
     * 新增列表页
     * <p>
     * 会添加应用分类, 会尝试初始化数据模型
     *
     * @param crudPage 列表页
     * @return 新增后的列表页数据
     */
    CrudPage create(CrudPage crudPage);

    /**
     * 创建
     *
     * @param dataModelId 根据数据模型ID创建列表页
     * @param name        名称
     * @param type        类型
     * @param jvsAppId    应用id
     * @return 设计
     */
    CrudPage create(String dataModelId, String name, String type, String jvsAppId);

    /**
     * 根据id删除列表页
     * <p>
     * 不能删除已发布的列表页
     *
     * @param appId
     * @param pageId 列表页id
     */
    void delete(String appId, String pageId);

    /**
     * 初始化列表页按钮表单
     *
     * @param appId       应用id
     * @param dataModelId 数据模型id
     * @param design      列表页设计数据
     */
    void initButton(String appId, String dataModelId, PageDesignHtml design);

    /**
     * 获取列表页模板数据
     *
     * @param ids 列表页id集合
     * @return 列表页数据集合
     */
    List<PageTemplateDto> getTemplate(List<String> ids);

    /**
     * 获取所有列表页生成的表单
     *
     * @param name     查询条件
     * @param jvsAppId
     * @return 表单基本数据
     */
    List<FormSelectItemDto> getAllForm(String name, String jvsAppId);

    /**
     * 获取系统默认按钮
     *
     * @return
     */
    List<ButtonDesignHtml> getSystemDefaultButtons();


    /**
     * 对设计进行自适应处理
     *
     * @param po
     * @param design
     */
    void convertDesign(CrudPage po, PageDesignHtml design);
}

