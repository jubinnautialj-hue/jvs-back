package cn.bctools.design.data.service;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.data.dto.ExportFieldDto;
import cn.bctools.design.data.entity.DataChangePo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.QueryListDto;
import cn.bctools.design.data.fields.dto.QueryOrderDto;
import cn.bctools.design.data.fields.dto.page.ModelDisplayHtml;
import cn.bctools.design.data.fields.dto.page.ModelDisplayLinkageFieldHtml;
import cn.bctools.design.data.fields.enums.AggregateEnumType;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.MapDifference;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.function.Function;

/**
 * 动态数据
 *
 * @Author: GuoZi
 */
public interface DynamicDataService {

    /**
     * The constant MONGO_ID.
     */
    String MONGO_ID = "_id";
    /**
     * The constant MONGO_NAME.
     */
    String MONGO_NAME = "jvs_data";

    /**
     * 根据id获取数据
     *
     * @param modelId 数据模型id
     * @param dataId  数据id
     * @return 数据对象 by id
     */
    DynamicDataPo getById(String modelId, String dataId);

    /**
     * 根据id获取所有数据
     *
     * @param modelId the model id
     * @param ids     the ids
     * @return by ids
     */
    List<Map> getByIds(String modelId, List<String> ids);

    /**
     * Gets by ids.
     *
     * @param modelId the model id
     * @param ids     the ids
     * @param field   the field
     * @return the by ids
     */
    List<Map> getByIds(String modelId, List<String> ids, Set<String> field);

    /**
     * 检查数据字段类型是否一致
     * 如果数据有错误，将报异常
     *
     * @param appId   the app id
     * @param modelId the model id
     * @param map     the map
     * @throws BusinessException the business exception
     */
    void checkDataFieldType(String appId, String modelId, Map<String, Object> map) throws BusinessException;

    /**
     * Check data field type.
     *
     * @param appId       应用 id
     * @param modelId     模型 id
     * @param map         数据
     * @param emptyEnable 是否允许为空 true 为允许
     * @throws BusinessException the business exception
     */
    void checkDataFieldType(String appId, String modelId, Map<String, Object> map, Boolean emptyEnable) throws BusinessException;

    /**
     * Check data field type.
     *
     * @param appId   the app id
     * @param modelId the model id
     * @param list    the list
     */
    default void checkDataFieldType(String appId, String modelId, List<Map<String, Object>> list) {
        list.forEach(e -> checkDataFieldType(appId, modelId, e));
    }

    /**
     * 保存数据(会发送回调)
     * <p>
     * 1. 数据操作: 使用Map结构的Json字符串存储
     * 2. 数据版本: 新增时默认数据版本为1
     *
     * @param appId   应用ID
     * @param modelId 数据模型id
     * @param data    数据
     * @return 保存后的数据id rule execute dto
     */
    RuleExecuteDto save(String appId, String modelId, Map<String, Object> data);

    /**
     * 保存数据(返回数据版本)
     *
     * @param appId   应用id
     * @param modelId 模型id
     * @param data    数据
     * @return 数据版本号 string
     */
    String saveReturnVersion(String appId, String modelId, Map<String, Object> data);

    /**
     * 批量保存数据
     *
     * @param dataList    数据集合
     * @param dataModelId 模型id
     * @return 保存成功与否 boolean
     */
    boolean saveBatch(List<DynamicDataPo> dataList, String dataModelId);

    /**
     * 批量保存数据
     *
     * @param appId    应用id
     * @param modelId  数据模型id
     * @param dataList 数据集合
     * @return 保存成功与否 rule execute dto
     */
    RuleExecuteDto saveBatch(String appId, String modelId, List<Map<String, Object>> dataList);

    /**
     * 修改数据(会发送回调)
     * <p>
     * 1. 数据操作: 对原数据的Map做putAll操作, 而非数据覆盖
     *
     * @param appId   应用id
     * @param modelId 模型id
     * @param dataId  数据id
     * @param data    数据
     * @return 数据版本号 rule execute dto
     */
    RuleExecuteDto update(String appId, String modelId, String dataId, Map<String, Object> data);

    /**
     * 修改数据(返回数据版本)
     * <p>
     * 数据版本: 每次修改时递增
     *
     * @param appId   应用id
     * @param modelId 模型id
     * @param dataId  数据id
     * @param data    数据
     * @return 数据版本号 string
     */
    String updateReturnVersion(String appId, String modelId, String dataId, Map<String, Object> data);

    /**
     * 批量修改数据
     *
     * @param modelId 模型id
     * @param data    数据
     * @param setData 变更数据值
     * @return long
     */
    long updateMulti(String modelId, Map<String, Object> data, Map<String, Object> setData);

    /**
     * 批量修改数据
     *
     * @param modelId            模型id
     * @param queryConditionDtos 查询条件
     * @param setData            变更数据值
     * @return long
     */
    long updateMulti(String modelId, List<QueryConditionDto> queryConditionDtos, Map<String, Object> setData);

    /**
     * 批量修改数据
     *
     * @param modelId 模型id
     * @param setData 数据集合
     */
    void updateBatchById(String modelId, List<Map<String, Object>> setData);

    /**
     * 修改数据
     * <p>
     * 单纯的入库操作
     *
     * @param modelId 模型id
     * @param dataId  数据id
     * @param data    变更数据值
     */
    void onlyUpdate(String modelId, String dataId, Map<String, Object> data);

    /**
     * 批量删除
     *
     * @param dataModelId 数据模型
     * @param filterData  the filter data
     */
    void removeMulti(String dataModelId, Map<String, Object> filterData);

    /**
     * 根据条件删除数据
     *
     * @param dataModelId           数据模型
     * @param queryConditionDtoList the query condition dto list
     * @return 返回删除的数据 list
     */
    List<Object> removeMulti(String dataModelId, List<QueryConditionDto> queryConditionDtoList);

    /**
     * 删除数据(会发送回调)
     *
     * @param modelId 模型id
     * @param dataId  数据id
     * @return 数据版本号 rule execute dto
     */
    RuleExecuteDto remove(String modelId, String dataId);

    /**
     * 分页查询
     *
     * @param appId                           应用id
     * @param page                            分页数据
     * @param modelId                         数据模型id
     * @param combiningFieldFormulaContentMap the combining field formula content map
     * @param conditions                      动态查询条件
     * @param sorts                           the sorts
     * @param fieldKeyList                    查询的字段集(为空时查询所有字段)
     * @param addButtonInfo                   是否返回可用按钮标识
     * @param echo                            是否进行回显
     * @param andOr                           使用and拼接还是or拼接查询条件
     * @param fieldBasicsHtmls                所有的字段名
     * @param stringSet                       the string set
     * @return 查询结果 page
     */
    Page<Map<String, Object>> queryPage(String appId, Page<DynamicDataPo> page, String modelId, Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap, List<List<QueryConditionDto>> conditions, List<QueryOrderDto> sorts, List<String> fieldKeyList, boolean addButtonInfo,
                                        boolean echo, Boolean andOr, List<FieldBasicsHtml> fieldBasicsHtmls, Set<String> stringSet);

    /**
     * Query page page.
     *
     * @param appId                           the app id
     * @param page                            the page
     * @param modelId                         the model id
     * @param combiningFieldFormulaContentMap the combining field formula content map
     * @param conditions                      the conditions
     * @param sorts                           the sorts
     * @param fieldKeyList                    the field key list
     * @param addButtonInfo                   the add button info
     * @param echo                            the echo
     * @param andOr                           the and or
     * @param fieldBasicsHtmls                the field basics htmls
     * @param stringSet                       the string set
     * @param encryptionData                  the encryption data
     * @return the page
     */
    Page<Map<String, Object>> queryPage(String appId, Page<DynamicDataPo> page, String modelId, Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap, List<List<QueryConditionDto>> conditions, List<QueryOrderDto> sorts, List<String> fieldKeyList, boolean addButtonInfo,
                                        boolean echo, Boolean andOr, List<FieldBasicsHtml> fieldBasicsHtmls, Set<String> stringSet, Boolean encryptionData);

    /**
     * Query page page.
     *
     * @param appId                           the app id
     * @param page                            the page
     * @param modelId                         the model id
     * @param combiningFieldFormulaContentMap the combining field formula content map
     * @param conditions                      the conditions
     * @param sorts                           the sorts
     * @param fieldKeyList                    the field key list
     * @param addButtonInfo                   the add button info
     * @param echo                            the echo
     * @param andOr                           the and or
     * @param fieldBasicsHtmls                the field basics htmls
     * @return the page
     */
    default Page<Map<String, Object>> queryPage(String appId, Page<DynamicDataPo> page, String modelId, Map<String, FunctionBusinessPo> combiningFieldFormulaContentMap, List<List<QueryConditionDto>> conditions, List<QueryOrderDto> sorts
            , List<String> fieldKeyList, boolean addButtonInfo, boolean echo, Boolean andOr, List<FieldBasicsHtml> fieldBasicsHtmls) {
        return queryPage(appId, page, modelId, combiningFieldFormulaContentMap, conditions, sorts, fieldKeyList, addButtonInfo, echo, andOr, fieldBasicsHtmls, new HashSet<>());
    }

    /**
     * 查询所有数据
     * <p>
     * 数据量可能很大, 请谨慎调用
     *
     * @param modelIds 数据模型id集合
     * @return 查询结果 list
     */
    List<DynamicDataPo> queryList(List<String> modelIds);

    /**
     * 查询所有数据
     *
     * @param modelId      数据模型id
     * @param criteria     查询条件
     * @param fieldKeyList 查询的字段集
     * @return 查询结果 list
     */
    List<Map<String, Object>> queryList(String modelId, Criteria criteria, List<String> fieldKeyList);

    /**
     * Query list list.
     *
     * @param modelId      the model id
     * @param criteria     the criteria
     * @param fieldKeyList the field key list
     * @param limit        the limit
     * @return the list
     */
    List<Map<String, Object>> queryList(String modelId, Criteria criteria, List<String> fieldKeyList, Integer limit);

    /**
     * 根据条件查询对象
     *
     * @param modelId      数据模型
     * @param criteria     查询条件
     * @param fieldKeyList 查询字段
     * @param sorts        排序规则
     * @return list
     */
    List<Map<String, Object>> queryList(String modelId, Criteria criteria, List<String> fieldKeyList, Sort sorts);

    /**
     * Query list list.
     *
     * @param modelId      the model id
     * @param criteria     the criteria
     * @param fieldKeyList the field key list
     * @param sorts        the sorts
     * @param limit        the limit
     * @return the list
     */
    List<Map<String, Object>> queryList(String modelId, Criteria criteria, List<String> fieldKeyList, Sort sorts, Integer limit);

    /**
     * 查询所有数据
     *
     * @param modelId  数据模型id
     * @param fieldKey 字段key(可为空)
     * @return 查询结果 list
     */
    List<Map<String, Object>> queryList(String modelId, List<String> fieldKey);

    /**
     * Query list list.
     *
     * @param modelId  the model id
     * @param fieldKey the field key
     * @return the list
     */
    List<Map<String, Object>> queryList(String modelId, String... fieldKey);

    /**
     * 查询所有数据
     *
     * @param appId               应用id
     * @param modelId             数据模型id
     * @param criteria            查询条件
     * @param sort                排序
     * @param excludeFieldKeyList 要排除的字段集
     * @param fieldKeyList        查询的字段集
     * @return 查询结果 list
     */
    List<Map> queryList(String appId, String modelId, Criteria criteria, Sort sort, List<String> excludeFieldKeyList, List<String> fieldKeyList);


    /**
     * 查询单条数据
     * <p>
     * 默认不返回可用按钮标识
     *
     * @param appId   应用id  todo 当前 appid可以不填写，后续跨应用使用时会需要此字段
     * @param modelId 数据模型id
     * @param dataId  数据id
     * @return 数据结果 map
     */
    Map<String, Object> querySingle(String appId, String modelId, String dataId);

    /**
     * 查询单条数据，表单详情或编辑表单默认接口回显使用
     *
     * @param appId         应用id
     * @param modelId       数据模型id
     * @param dataId        数据id
     * @param addButtonInfo 是否返回可用按钮标识
     * @return 数据结果 map
     */
    Map<String, Object> querySingle(String appId, String modelId, String dataId, boolean addButtonInfo);


    /**
     * 查询单个字段值
     *
     * @param appId   应用id
     * @param modelId 数据模型id
     * @param dataId  数据id
     * @param fieldId 字段id
     * @return 字段值 object
     */
    Object queryField(String appId, String modelId, String dataId, String fieldId);

    /**
     * 转换单个动态数据
     *
     * @param data 动态数据
     * @return map数据 map
     */
    Map<String, Object> paresMap(DynamicDataPo data);

    /**
     * 数据对象转Map结构数据
     *
     * @param data      数据对象
     * @param fieldKeys 字段名称集合
     * @return Map数据 map
     */
    Map<String, Object> paresMap(DynamicDataPo data, Collection<String> fieldKeys);

    /**
     * Map数据转数据类
     *
     * @param data Map数据
     * @return 数据类 dynamic data po
     */
    DynamicDataPo parseBean(Map<String, Object> data);

    /**
     * Echo map.
     *
     * @param data     the data
     * @param fields   the fields
     * @param override the override
     * @return the map
     */
    Map<String, Object> echo(Map<String, Object> data, Collection<FieldBasicsHtml> fields, boolean override);

    /**
     * 校验模型的唯一性
     *
     * @param data    检查的数据
     * @param modelId 数据模型的id
     */
    void checkDataModel(Map<String, Object> data, String modelId);


    /**
     * 刷新公式唯一数据,判断库里面是否有相同的数据
     *
     * @param modelId     the model id
     * @param dataModelPo the data model po
     */
    void refreshData(String modelId, DataModelPo dataModelPo);

    /**
     * 清空这几个字段中的所有数据
     *
     * @param fields      字段名
     * @param dataModelId 数据模型id
     */
    void deleteFields(List<String> fields, String dataModelId);

    /**
     * 数据对象转Map结构数据
     * <p>
     * 并处理回显内容
     * 此操作，如果存在表单或选项卡数据，需要进行递归解析
     * 默认都要进行脱敏处理
     *
     * @param appId    应用 id
     * @param data     数据对象
     * @param modelId  设计id
     * @param designId the design id
     * @param override 是否覆盖原有的_1字段，如果是打印或导出时，需要进行覆盖,的逻辑引擎中使用也需要覆盖
     * @return Map数据 map
     */
    Map<String, Object> paresMapWithEcho(String appId, Map<String, Object> data, String modelId, String designId, boolean override);


    /**
     * Echo map.
     *
     * @param data     the data
     * @param fieldMap the field map
     * @param override the override
     * @return the map
     */
    Map<String, Object> echo(Map<String, Object> data, Map<String, FieldBasicsHtml> fieldMap, boolean override);

    /**
     * Echo map.
     *
     * @param data     the data
     * @param fieldMap the field map
     * @param override the override
     * @param function the function
     * @return the map
     */
    Map<String, Object> echo(Map<String, Object> data, Map<String, FieldBasicsHtml> fieldMap, boolean override, Function<ExportFieldDto, Object> function);

    /**
     * 数据对象转Map结构数据
     * <p>
     * 并处理回显内容
     * 此操作，如果存在表单或选项卡数据，需要进行递归解析
     * 默认都要进行脱敏处理
     *
     * @param data     行级数据
     * @param fields   所有的字段
     * @param override 是否覆盖
     * @return list
     */
    List<Map> echo(List<Map> data, Collection<FieldBasicsHtml> fields, boolean override);

    /**
     * 转换数据
     *
     * @param appId   the app id
     * @param data    the data
     * @param modelId the model id
     * @return map
     */
    Map<String, Object> paresMapWithEcho(String appId, DynamicDataPo data, String modelId);

    /**
     * 替换显示字段的值
     *
     * @param sourceFieldId   用以替换的显示字段id
     * @param replaceFieldKey 查询结果的字段
     * @param data            数据
     */
    void replaceSourceFieldData(String sourceFieldId, String replaceFieldKey, List<Map<String, Object>> data);

    /**
     * 扩展业务数据之外的数据,返回用户头像信息，用于工作流的表单信息
     *
     * @param data 业务数据
     */
    void expandOtherData(Map<String, Object> data);

    /**
     * 查询所有数据
     *
     * @param dataModelId  数据模型id
     * @param queryPageDto 查询条件
     * @return list
     */
    List<Map<String, Object>> postQueryList(String dataModelId, QueryListDto queryPageDto);

    /**
     * 根据模型查询所有数据
     *
     * @param dataModelId 模型id
     * @param fieldList   字段名  末认会加Id
     * @param list        查询条件
     * @return list
     */
    List<Map<String, Object>> queryList(String dataModelId, List<String> fieldList, QueryConditionDto... list);

    /**
     * 指定删除规则
     *
     * @param modelId the model id
     * @param dataId  the data id
     * @return map
     * @throws BusinessException the business exception
     */
    Map<String, Object> onlyRemove(String modelId, String dataId) throws BusinessException;

    /**
     * 保存数据
     *
     * @param appId       应用ID
     * @param dataModelId 模型id
     * @param body        the body
     * @return string
     */
    String saveTransactional(String appId, String dataModelId, Map<String, Object> body);

    /**
     * 查询模型数据信息
     *
     * @param appId        the app id
     * @param dataModelId  the data model id
     * @param queryPageDto the query page dto
     * @return list
     */
    List<Map<String, Object>> postQueryList(String appId, String dataModelId, QueryListDto queryPageDto);

    /**
     * Post query list list.
     *
     * @param appId        the app id
     * @param dataModelId  the data model id
     * @param queryPageDto the query page dto
     * @param override     the override
     * @return the list
     */
    List<Map<String, Object>> postQueryList(String appId, String dataModelId, QueryListDto queryPageDto, Boolean override);

    /**
     * 根据模型字段， 转换并替换原来的值
     *
     * @param appId       应用id
     * @param dataModelId 模型id
     * @param data        数据
     */
    void replaceSourceFieldDataMap(String appId, String dataModelId, Map<String, Object> data);

    /**
     * 根据模型字段， 转换并替换原来的值
     *
     * @param appId       应用id
     * @param dataModelId 模型id
     * @param fieldList   字段名
     * @param data        数据
     */
    void replaceSourceFieldData(String appId, String dataModelId, List<String> fieldList, List<Map<String, Object>> data);

    /**
     * Replace source field data.
     *
     * @param appId       the app id
     * @param dataModelId the data model id
     * @param fieldList   the field list
     * @param data        the data
     * @param override    the override
     */
    void replaceSourceFieldData(String appId, String dataModelId, List<String> fieldList, List<Map<String, Object>> data, Boolean override);

    /**
     * Replace source field data.
     *
     * @param appId       the app id
     * @param dataModelId the data model id
     * @param fieldList   the field list
     * @param data        the data
     */
    void replaceSourceFieldData(String appId, String dataModelId, List<String> fieldList, Map<String, Object> data);


    /**
     * 数据项状态变更
     * <p>
     * 将不满足条件的数据项标记为禁用
     *
     * @param dataList          数据集
     * @param disableConditions 禁用条件
     */
    void enableDataItem(List<Map<String, Object>> dataList, List<List<QueryConditionDto>> disableConditions);

    /**
     * 逻辑引擎保存数据模型数据
     *
     * @param appId       the app id
     * @param dataModelId the data model id
     * @param body        the body
     * @return string
     */
    String saveTransactionalRule(String appId, String dataModelId, Map<String, Object> body);

    /**
     * 保存数据
     *
     * @param modelId 数据模型id
     * @param data    数据内容
     * @return 新增后的数据id string
     */
    String onlySave(String modelId, Map<String, Object> data);

    /**
     * 聚合mongo指定设计地址
     *
     * @param criteria       组件的查询条件
     * @param dataModelId    模型id
     * @param type           类型，分组，求和，平均
     * @param group          是否分组
     * @param aggregateField 聚合字段
     * @return list
     */
    List aggregate(Criteria criteria, String dataModelId, AggregateEnumType type, String aggregateField, Fields group);

    /**
     * Gets string object map.
     *
     * @param init      the init
     * @param body      the body
     * @param params    the params
     * @param fieldsMap the fields map
     * @param formPo    the form po
     * @return the string object map
     */
    Map<String, Object> getStringObjectMap(Boolean init, ExecDto body, Map<String, Object> params, Map<String, FieldBasicsHtml> fieldsMap, FormPo formPo);

    /**
     * 清空指定模型数据
     *
     * @param modelId 模型id
     */
    void removeAll(String modelId);

    /**
     * 显示设置-关联模型数据回显
     *
     * @param appId                应用id
     * @param dataList             数据集合
     * @param fieldModelDisplayMap Map<列表字段key，关联字段>
     */
    void echoModelDisplay(String appId, List<Map<String, Object>> dataList, Map<String, ModelDisplayHtml> fieldModelDisplayMap);

    /**
     * 显示设置-关联模型数据回显
     *
     * @param data             待返回的数据
     * @param linkageData      关联数据（从此数据集中获取数据，作为data的回显数据）
     * @param fieldBasicsHtmls 关联数据模型的字段
     * @param linkageFieldKeys 关联模型数据回显字段配置
     */
    void echoModelDisplay(Map<String, Object> data, Map<String, Object> linkageData, Collection<FieldBasicsHtml> fieldBasicsHtmls, List<ModelDisplayLinkageFieldHtml> linkageFieldKeys);

    /**
     * 修改数据前对脱敏数据进行过滤清除掉
     * 根据数据和数据库数据进行对比，将其脱敏的数据去掉。
     * 用户展示的数据已经被脱敏后显示为:{"name":"张**"}
     * 此时用户提交数据时也会将脱敏后的数据提交到后台，需要将其脱敏数据进行对比后判断是否修改。
     * 1、系统默认修改时
     * 2、工作流提交修改时
     * 3、工作流重启或其它场景时
     *
     * @param modelId the model id  模型 id
     * @param data    the data  用户操作的数据
     * @param oldData the old data  数据库历史的老数据
     */
    void clearSensitiveBody(String modelId, Map<String, Object> data, Map<String, Object> oldData);


    /**
     * 脱敏数据
     *
     * @param data
     * @param byId
     */
    void encryptionData(Map<String, Object> data, DataModelPo byId);

    /**
     * 获取脱敏字段
     *
     * @param byId
     * @return
     */
    List<String> encryptionData(DataModelPo byId);

    List<String> encryptionData(String byId);

    List<DataChangePo> saveDataChange(String jvsAppId, String designId, Map<String, Object> oldData, MapDifference<String, Object> difference, Map<String, Object> data);
}

