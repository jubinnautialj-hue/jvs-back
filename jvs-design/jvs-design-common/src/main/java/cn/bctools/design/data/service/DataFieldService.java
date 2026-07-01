package cn.bctools.design.data.service;

import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.impl.DataFieldServiceImpl;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 数据字段
 *
 * @Author: GuoZi
 */
public interface DataFieldService extends IService<DataFieldPo> {
    /**
     * 关联数据id默认字段（数据结构：{关联标识：[数据id集合]}）
     */
    String FIELD_RELATION_TAG = "relationTag";

    /**
     * 保存数据字段信息
     *
     * @param appId   应用id
     * @param fields  字段信息集合
     * @param modelId 数据模型id
     * @return 保存是否成功 boolean
     */
    boolean saveFields(String appId, List<DataFieldPo> fields, String modelId);

    /**
     * 修改数据字段信息
     *
     * @param appId      应用id
     * @param designId   设计id
     * @param designType 设计类型
     * @param modelId    数据模型id
     * @param fields     字段信息集合
     * @return 操作结果 boolean
     */
    boolean updateFields(String appId, String designId, DesignType designType, String modelId, List<DataFieldPo> fields);

    /**
     * 根据数据模型id获取单个字段信息
     * <p>
     * 字段显示名称以最后一次保存的为准
     *
     * @param appId         应用id
     * @param modelId       数据模型id
     * @param fieldKey      字段key
     * @param getDesignJson 是否获取字段设计数据
     * @return 字段信息 field
     */
    FieldBasicsHtml getField(String appId, String modelId, String fieldKey, boolean getDesignJson);

    /**
     * 根据数据模型id获取字段信息集合
     * <p>
     * 字段显示名称以最后一次保存的为准
     *
     * @param appId            应用id
     * @param modelId          数据模型id
     * @param getDesignJson    是否获取字段设计数据
     * @param addDefaultFields 添加系统默认字段
     * @return 字段信息集合(已对key去重) fields
     */
    List<FieldBasicsHtml> getFields(String appId, String modelId, boolean getDesignJson, boolean addDefaultFields);

    /**
     * 根据数据模型id获取字段信息集合
     * <p>
     * 字段显示名称以最后一次保存的为准
     *
     * @param appId            应用id
     * @param modelId          数据模型id
     * @param designId         设计id  如果需要转换此设计 Id不应该传递，
     * @param getDesignJson    是否获取字段设计数据
     * @param addDefaultFields 添加系统默认字段
     * @return 字段信息集合(已对key去重) fields
     */
    List<FieldBasicsHtml> getFields(String appId, String modelId, String designId, boolean getDesignJson, boolean addDefaultFields);

    /**
     * Gets default fields.
     *
     * @param modelId the model id
     * @return the default fields
     */
    Map<String, DataFieldPo> getDefaultFields(String modelId);

    /**
     * 获取前端不需要显示，但后端需要处理的字段信息集合
     *
     * @return 字段信息集合(已对key去重) do not show fields
     */
    List<DataFieldPo> getDoNotShowFields();

    /**
     * 根据数据模型id获取字段key集合
     *
     * @param appId   应用ID
     * @param modelId 数据模型id
     * @return 字段key集合(已去重) field keys
     */
    List<String> getFieldKeys(String appId, String modelId);

    /**
     * 获取自增序号字段
     *
     * @param appId   应用ID
     * @param modelId 数据模型id
     * @return 字段key集合(已去重) increased id fields
     */
    List<FieldBasicsHtml> getIncreasedIdFields(String appId, String modelId);

    /**
     * 根据应用ID 和模型ID查询字段
     *
     * @param appId   应用id
     * @param modelId 模型id
     * @return all field
     */
    List<FieldBasicsHtml> getAllField(String appId, String modelId);

    /**
     * Gets all field.
     *
     * @param appId            the app id
     * @param modelId          the model id
     * @param getDesignJson    the get design json
     * @param addDefaultFields the add default fields
     * @return the all field
     */
    List<FieldBasicsHtml> getAllField(String appId, String modelId, boolean getDesignJson, boolean addDefaultFields);

    /**
     * 根据应用ID 和模型ID查询字段
     *
     * @param appId     应用id
     * @param modelId   模型id
     * @param predicate 排除字段
     * @return all field
     */
    List<FieldBasicsHtml> getAllField(String appId, String modelId, Predicate<FieldBasicsHtml> predicate);

    /**
     * Gets all field.
     *
     * @param appId            the app id
     * @param modelId          the model id
     * @param getDesignJson    the get design json
     * @param addDefaultFields the add default fields
     * @param predicate        the predicate
     * @return the all field
     */
    List<FieldBasicsHtml> getAllField(String appId, String modelId, boolean getDesignJson, boolean addDefaultFields, Predicate<FieldBasicsHtml> predicate);

    /**
     * 根据应用ID 和模型ID查询字段
     *
     * @param appId     应用id
     * @param modelId   模型id
     * @param predicate 排除字段
     * @return all field default
     */
    List<FieldBasicsHtml> getAllFieldDefault(String appId, String modelId, Predicate<FieldBasicsHtml> predicate);

    /**
     * 获取所有系统字段
     *
     * @return default all fields
     */
    List<DataFieldPo> getDefaultAllFields();

    /**
     * 获取模型的字段
     *
     * @param appId            应用id
     * @param modelId          模型id
     * @param designId         设计id
     * @param fieldKeys        可选的哪些字段名
     * @param fieldType        类型类型
     * @param getDesignJson    是否需要具体的设计结构json
     * @param addDefaultFields 是否返回默认的数据字段,包含创建人,修改人等
     * @return fields
     */
    List<FieldBasicsHtml> getFields(String appId, String modelId, String designId, List<String> fieldKeys, DataFieldType fieldType, boolean getDesignJson, boolean addDefaultFields);

    /**
     * 根据条件进行字段筛选
     *
     * @param fieldCacheKey the field cache key
     * @return fields cache
     */
    List<FieldBasicsHtml> getFieldsCache(DataFieldServiceImpl.FieldCacheKey fieldCacheKey);

    /**
     * 根据数据模型和数据，和规则，进行内容替换
     *
     * @param appId   应用Id
     * @param modelId 模型id
     * @param data    模型数据
     * @param content 操作类型
     * @return 数据替换的结果 string
     */
    String templateReplacement(String appId, String modelId, Map<String, Object> data, String content);

    /**
     * Gets design id fields.
     *
     * @param appId            the app id
     * @param modelId          the model id
     * @param designId         the design id
     * @param getDesignJson    the get design json
     * @param addDefaultFields the add default fields
     * @return the design id fields
     */
    List<FieldBasicsHtml> getDesignIdFields(String appId, String modelId, String designId, boolean getDesignJson, boolean addDefaultFields);

    /**
     * 获取数据模型字段
     *
     * @param appId            应用id
     * @param modelId          数据模型id
     * @return 数据模型字段
     */
    List<FieldBasicsHtml> getModelFields(String appId, String modelId);
}
