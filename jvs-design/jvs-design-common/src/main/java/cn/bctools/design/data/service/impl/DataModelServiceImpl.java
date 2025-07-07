package cn.bctools.design.data.service.impl;

import cn.bctools.auth.api.api.AuthRoleServiceApi;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.IndexField;
import cn.bctools.design.crud.entity.IndexFields;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.DataModelDto;
import cn.bctools.design.data.fields.dto.DataModelPageReqDto;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.mapper.DataModelMapper;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataLogService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.util.DataModelUtil;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.dto.AppRoleDto;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据模型
 *
 * @Author: GuoZi
 */
@Slf4j
@Service
@AllArgsConstructor
@Design(DesignType.data)
@Transactional(rollbackFor = Exception.class)
public class DataModelServiceImpl extends ServiceImpl<DataModelMapper, DataModelPo> implements DataModelService, IJvsDesigner {

    DataModelHandler dataModelHandler;
    DataFieldService fieldService;
    JvsAppService appService;
    DataFieldService dataFieldService;
    AuthRoleServiceApi authRoleServiceApi;
    DataLogService dataLogService;
    RedisUtils redisUtils;
    IdentificationService identificationService;
    MapperMethodHandler mapperMethodHandler;

    public static final String UNNAMED_NAME = "未命名";
    public static final Integer MAX_INDEX_COUNT = 10;

    @Override
    public DataModelPo getModel(String modelId) {
        if (StringUtils.isBlank(modelId)) {
            throw new BusinessException("模型id为空");
        }
        DataModelPo modelPo = this.getById(modelId);
        if (Objects.isNull(modelPo)) {
            throw new BusinessException("模型不存在");
        }
        return modelPo;
    }

    @Override
    public boolean save(DataModelPo entity) {
        entity.setCreateTime(LocalDateTime.now());
        return super.save(entity);
    }

    @Override
    public boolean saveBatch(Collection<DataModelPo> entityList, int batchSize) {
        LocalDateTime now = LocalDateTime.now();
        entityList.forEach(e -> e.setCreateTime(now));
        return super.saveBatch(entityList, batchSize);
    }

    @Override
    public String create(String appId, String designId, DesignType designType, String name) {
        if (StringUtils.isBlank(name)) {
            name = UNNAMED_NAME;
        }
        DataModelPo modelPo = new DataModelPo();
        modelPo.setAppId(appId);
        modelPo.setName(name);
        modelPo.setDesignId(designId);
        modelPo.setDesignType(designType);
        if (DesignType.workflow.equals(designType)) {
            modelPo.setWorkflowId(designId);
            modelPo.setEnableWorkflow(true);
        }
        modelPo.setBelongMode(ModeUtils.getMode());
        modelPo.setTableCode(IdWorker.getIdStr());
        this.save(modelPo);
        // 创建数据集默认索引
        dataModelHandler.createDefaultIndex(modelPo.getId());
        return modelPo.getId();
    }

    @Override
    public void createBatch(List<DataModelPo> dataModelPoList) {
        saveBatch(dataModelPoList);
        // 创建数据集默认索引
        dataModelPoList.forEach(model -> {
            String collectionName = Optional.ofNullable(model.getCollectionName()).orElseGet(model::getId);
            dataModelHandler.createDefaultIndex(collectionName);
        });
    }

    @Override
    public String createWithField(String appId, String designId, DesignType designType, String name, List<DataFieldPo> fields) {
        String modelId = this.create(appId, designId, designType, name);
        if (ObjectNull.isNotNull(fields)) {
            fieldService.saveFields(appId, fields, modelId);
        }
        return modelId;
    }

    @Override
    public void updateName(String dataModelId, String name) {
        if (StringUtils.isBlank(dataModelId)) {
            return;
        }
        if (StringUtils.isBlank(name)) {
            name = UNNAMED_NAME;
        }
        DataModelPo modelPo = getOne(Wrappers.<DataModelPo>lambdaQuery()
                .eq(DataModelPo::getId, dataModelId).select(DataModelPo::getName));
        if (ObjectNull.isNull(modelPo)) {
            return;
        }
        // 若模型名称包含“未命名”，则更改
        if (modelPo.getName().contains(UNNAMED_NAME)) {
            this.update(Wrappers.<DataModelPo>lambdaUpdate()
                    .set(DataModelPo::getName, name)
                    .eq(DataModelPo::getId, dataModelId));
            // 修改自定义标识冗余的设计名称
            identificationService.updateDesignName(dataModelId, name);
        }
    }
    @Override
    public void delete(String appId, String designId) {
        DataFieldPo fieldPo = fieldService.getOne(Wrappers.<DataFieldPo>lambdaQuery()
                .select(DataFieldPo::getModelId)
                .eq(DataFieldPo::getDesignId, designId)
                .last("limit 1"));
        if (Objects.isNull(fieldPo)) {
            // 该模型无字段
            return;
        }
        String modelId = fieldPo.getModelId();
        this.remove(Wrappers.<DataModelPo>lambdaQuery().eq(DataModelPo::getAppId, appId).eq(DataModelPo::getId, modelId));
        fieldService.remove(Wrappers.<DataFieldPo>lambdaQuery().eq(DataFieldPo::getDesignId, designId));
    }

    @Override
    public void beforeAppDeleted(String appId) {
        List<DataModelPo> dataModelPoList = list(Wrappers.<DataModelPo>lambdaQuery()
                .eq(DataModelPo::getAppId, appId)
                .select(DataModelPo::getId));
        List<String> modeIds = dataModelPoList.stream().map(DataModelPo::getId).collect(Collectors.toList());
        modeIds.forEach(modeId -> {
            dataModelHandler.dropCollection(modeId);
            dataModelHandler.dropCollection(DataModelUtil.buildLogCollectionName(modeId));
            dataModelHandler.dropCollection(DataModelUtil.buildRemoveCollectionName(modeId));
        });
        mapperMethodHandler.deletePhysical(this, Wrappers.<DataModelPo>lambdaQuery().eq(DataModelPo::getAppId, appId));
    }

    /**
     * 校验字段信息是否完整
     *
     * @param field 字段信息
     * @return 校验结果
     */
    private boolean emptyFieldDto(FieldPublicHtml field) {
        if (Objects.isNull(field)) {
            return true;
        }
        return StringUtils.isBlank(field.getFieldKey());
    }

    @Override
    public void updateEnableWorkflow(String modelId, Boolean flag) {
        update(Wrappers.<DataModelPo>lambdaUpdate()
                .eq(DataModelPo::getId, modelId)
                .set(DataModelPo::getEnableWorkflow, flag));
    }

    @Override
    public Page<DataModelDto> findPage(Page<DataModelPo> page, DataModelPageReqDto dto, String appId) {
        // 查询模型数据
        page(page, Wrappers.<DataModelPo>lambdaQuery()
                .select(DataModelPo::getId, DataModelPo::getAppId, DataModelPo::getName,  DataModelPo::getIndexFields, DataModelPo::getCreateTime, DataModelPo::getCollectionName, DataModelPo::getEnableModelField)
                .eq(ObjectNull.isNotNull(appId), DataModelPo::getAppId, appId)
                .eq(ObjectNull.isNotNull(dto.getEnableWorkflow()), DataModelPo::getEnableWorkflow, dto.getEnableWorkflow())
                .like(ObjectNull.isNotNull(dto.getId()), DataModelPo::getId, dto.getId())
                .like(ObjectNull.isNotNull(dto.getName()), DataModelPo::getName, dto.getName())
                .like(ObjectNull.isNotNull(dto.getCollectionName()), DataModelPo::getCollectionName, dto.getCollectionName())
                .orderByDesc(DataModelPo::getCreateTime));
        Page<DataModelDto> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<DataModelPo> models = page.getRecords();
        if (ObjectUtils.isEmpty(models)) {
            return resultPage;
        }
        List<DataModelDto> modelList = new ArrayList<>();
        for (DataModelPo model : models) {
            DataModelDto dataModelDto = BeanCopyUtil.copy(model, DataModelDto.class);
            List<FieldBasicsHtml> fields = null;
            // 启用了模型字段，查询模型配置的字段
            if (Boolean.TRUE.equals(model.getEnableModelField())) {
                fields = fieldService.getModelFields(appId, model.getId());
            } else {
                // 未启用模型字段，则查询模型相关设计的字段
                fields = fieldService.getFields(appId, model.getId(), false, false);
            }
            dataModelDto.setFieldList(fields);
            dataModelDto.setCollectionName(Optional.ofNullable(model.getCollectionName()).orElse(model.getId()));
            modelList.add(dataModelDto);
        }
        resultPage.setRecords(modelList);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void indexField(String modelId, String appId, List<IndexFields> fields) {
        JvsApp jvsApp = appService.getById(appId);
        AppRoleDto appRole = Optional.ofNullable(jvsApp.getRole()).orElseGet(AppRoleDto::new);
        boolean check = appService.checkRole(appRole.getAdminMember(), UserCurrentUtils.getCurrentUser()) ||
                appService.checkRole(appRole.getDevMember(), UserCurrentUtils.getCurrentUser());
        if (Boolean.FALSE.equals(check)) {
            //如果应用的创建人和管理员是自己直接返回
            throw new BusinessException("没有权限操作");
        }
        int size = fields.stream().map(IndexFields::getName).collect(Collectors.toSet()).size();
        if (size != fields.size()) {
            throw new BusinessException("索引名称存在重复，请检查");
        }
        //判断索引是否有存在重复字段
        DataModelPo dataModelPo = getById(modelId);
        if (dataModelPo.getAppId().equals(appId)) {
            try {
                //查询出历史的索引信息并进行全部删除
                if (ObjectNull.isNotNull(dataModelPo.getIndexFields())) {
                    try {
                        dataModelPo.getIndexFields().forEach(e -> dataModelHandler.getCollection(modelId).dropIndex(e.getName()));
                    } catch (Exception ignored) {
                    }
                }

                if (ObjectNull.isNotNull(fields)) {
                    List<IndexModel> collect = new ArrayList<>();
                    for (IndexFields e : fields) {
                        Iterator<IndexField> iterator = e.getFields().iterator();
                        IndexField next = iterator.next();
                        if (ObjectNull.isNull(next.getKey())) {
                            throw new BusinessException("索引信息不完善，请检查");
                        }
                        Document indexKeys = new Document(next.getKey(), next.getSort().getSort());
                        if (iterator.hasNext()) {
                            // 创建复合索引
                            IndexField nexted = iterator.next();
                            if (ObjectNull.isNull(nexted.getKey())) {
                                throw new BusinessException("索引信息不完善，请检查");
                            }
                            indexKeys.append(nexted.getKey(), nexted.getSort().getSort());
                        }
                        IndexOptions indexOptions = new IndexOptions().name(e.getName()).unique(e.getRepetitionAllowed());
                        collect.add(new IndexModel(indexKeys, indexOptions));
                    }
                    dataModelHandler.getCollection(modelId).createIndexes(collect);
                }
                dataModelPo.setIndexFields(fields);
                updateById(dataModelPo);
            } catch (Exception e) {
                throw new BusinessException("创建索引失败");
            }
        } else {
            throw new BusinessException("没有权限操作");
        }
    }


    @Override
    public List<FieldBasicsHtml> getDesignField(String appId, String modelId) {
        return fieldService.getFields(appId, modelId, false, false);
    }

    @Override
    public Long getModeSize() {
        Set<String> collect = appService.list(new LambdaQueryWrapper<JvsApp>().select(JvsApp::getId)).stream().map(e -> e.getId()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(collect)) {
            return 0L;
        }
        return list(new LambdaQueryWrapper<DataModelPo>()
                .in(DataModelPo::getAppId, collect)
                .select(DataModelPo::getSize)
        ).stream().filter(ObjectNull::isNotNull).mapToLong(DataModelPo::getSize).sum();
    }

    @Override
    public void updateEnableModelField(String modelId, Boolean enable) {
        update(Wrappers.<DataModelPo>lambdaUpdate()
                .set(DataModelPo::getEnableModelField, enable)
                .eq(DataModelPo::getId, modelId));
    }

    @Override
    public void addModelField(String appId, String modelId, List<DataFieldPo> fields) {
        if (ObjectNull.isNull(fields)) {
            return;
        }
        // 查询模型配置的字段
        List<String> modelFieldKeys = fieldService.getFields(appId, modelId, modelId, false, false)
                .stream()
                .map(FieldBasicsHtml::getFieldKey)
                .collect(Collectors.toList());
        // 过滤可以保存为模型字段的新字段
        List<DataFieldType> notModelFieldTypeList = DataFieldType.notModelFieldTypes();
        String appVersion = CurrentAppUtils.getAppVersion();
        List<DataFieldPo> addFields = fields.stream()
                .filter(field -> !notModelFieldTypeList.contains(field.getFieldType()))
                .filter(field -> !modelFieldKeys.contains(field.getFieldKey()))
                .map(field ->
                        new DataFieldPo()
                                .setDesignId(modelId)
                                .setModelId(modelId)
                                .setFieldKey(field.getFieldKey())
                                .setFieldName(field.getFieldName())
                                .setDesignType(DesignType.data)
                                .setFieldType(field.getFieldType())
                                .setJvsAppId(appId)
                                .setAppVersion(appVersion)
                )
                .collect(Collectors.toList());
        if (ObjectNull.isNull(addFields)) {
            return;
        }
        fieldService.saveBatch(addFields);
    }
}
