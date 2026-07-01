package cn.bctools.design.crud.service;

import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.dto.FieldHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.template.dto.FormTemplateDto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 表单配置项
 *
 * @author auto
 */
public interface FormService extends IService<FormPo> {

    /**
     * 根据id获取设计数据, 并做空值校验
     *
     * @param formId 表单id
     * @return 表单设计数据 form po
     */
    FormPo get(String formId);

    /**
     * 新增表单
     * <p>
     * 会添加应用分类, 会尝试初始化数据模型
     *
     * @param formPo 表单
     * @return 新增后的表单数据 form po
     */
    FormPo create(FormPo formPo);


    /**
     * 根据id删除表单
     * <p>
     * 不能删除已发布的表单
     *
     * @param appId  the app id
     * @param formId 表单id
     */
    void delete(String appId, String formId);

    /**
     * 发布表单
     *
     * @param appId the app id
     * @param id    设计id
     * @return 表单 form po
     */
    FormPo deploy(String appId, String id);

    /**
     * 卸载表单
     *
     * @param appId the app id
     * @param id    设计id
     * @return 表单 form po
     */
    FormPo unload(String appId, String id);

    /**
     * 获取表单模板数据
     *
     * @param ids 表单id集合
     * @return 模板数据集合 template
     */
    List<FormTemplateDto> getTemplate(List<String> ids);

    /**
     * 设置字段大小
     *
     * @param po           the po
     * @param formDataHtml the form data html
     * @param fields       the fields
     */
    void associationSettingsFields(FormPo po, FormDesignHtml formDataHtml, List<DataFieldPo> fields);

    /**
     * 获取表单设计详情
     *
     * @param po
     * @param appId 应用id
     * @param id    表单id
     * @return 表单设计详情 form detail
     */
    FormPo getFormDetail(FormPo po, String appId, String id);

    /**
     * 获取这个设计的整体字段关系结构
     *
     * @param formDesignHtml 表单结构
     * @param list           空对象l
     * @param cls            根据不同的 cls决定是否需要 json结构
     * @return
     */
    void structure(FormDesignHtml formDesignHtml, List<FieldHtml> list, Class<? extends FieldHtml> cls);


    /**
     * Create form po.
     * <p>
     * 新增表单
     *
     * @param dataModelId 数据模型id
     * @param jvsAppId    应用id
     * @param buttonName  按钮名称
     * @return 新增后的表单数据 form po
     * @return the form po
     */
    FormPo create(String dataModelId, String jvsAppId, String buttonName);
}

