package cn.bctools.design.data.service;

import cn.bctools.design.crud.entity.IndexFields;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.DataModelDto;
import cn.bctools.design.data.fields.dto.DataModelPageReqDto;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DesignType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 数据模型
 *
 * @Author: GuoZi
 */
public interface DataModelService extends IService<DataModelPo> {

    /**
     * 查询数据模型信息
     *
     * @param modelId 模型id
     * @return 模型信息 model
     */
    DataModelPo getModel(String modelId);

    /**
     * 创建数据模型
     *
     * @param appId      应用id
     * @param designId   设计id
     * @param designType 设计类型
     * @param name       模型名称
     * @return 数据模型id string
     */
    String create(String appId, String designId, DesignType designType, String name);

    /**
     * 批量创建数据模型
     *
     * @param dataModelPoList the data model po list
     * @param dataModelPoList
     */
    void createBatch(List<DataModelPo> dataModelPoList);

    /**
     * 创建数据模型, 并保存字段数据
     * <p>
     * 会为字段数据自动填充数据模型id
     *
     * @param appId      应用id
     * @param designId   设计id
     * @param designType 设计类型
     * @param name       模型名称
     * @param fields     字段数据
     * @return 数据模型id string
     */
    String createWithField(String appId, String designId, DesignType designType, String name, List<DataFieldPo> fields);

    /**
     * 修改数据模型名称
     * <p>
     *
     * @param dataModelId 数据模型id
     * @param name        模型名称
     */
    void updateName(String dataModelId, String name);


    /**
     * 修改模型启用工作流状态
     *
     * @param modelId 模型id
     * @param flag    TRUE-启用，FALSE-不启用
     */
    void updateEnableWorkflow(String modelId, Boolean flag);

    /**
     * 模型列表查询
     *
     * @param page  the page
     * @param dto   the dto
     * @param appId the app id
     * @return page
     * @return
     */
    Page<DataModelDto> findPage(Page<DataModelPo> page, DataModelPageReqDto dto, String appId);

    /**
     * 数据模型添加索引
     *
     * @param modelId  the model id
     * @param fieldKey the field key
     * @param appId    the app id
     * @return
     */
    void indexField(@PathVariable String modelId, @PathVariable String appId, List<IndexFields> fields);

    /**
     * 获取模型下所有设计字段
     *
     * @param appId   the app id
     * @param modelId the model id
     * @return design field
     */
    List<FieldBasicsHtml> getDesignField(String appId, String modelId);

    /**
     * 获取租户模型数据大小
     *
     * @return mode size
     */
    Long getModeSize();

    /**
     * 修改是否启用模型字段
     *
     * @param modelId 模型id
     * @param enable true-启用模型字段，false-不启用模型字段
     */
    void updateEnableModelField(String modelId, Boolean enable);

    /**
     * 添加模型字段
     * <p>
     *    模型字段不能随着设计字段的变更而变更，所以只做增加不做删除和修改
     *
     * @param appId  应用id
     * @param modelId 模型id
     * @param fields 字段集合
     */
    void addModelField(String appId, String modelId, List<DataFieldPo> fields);
}

