package cn.bctools.design.data.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.crud.entity.IndexFields;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.data.util.DataModelUtil;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.notice.handler.DataNoticeHandler;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.web.excel.ArrayListConvert;
import cn.bctools.web.excel.LocalDateTimeConvert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 动态数据管理
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "[data]动态数据")
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/dynamic/data")
public class DynamicDataDesignController {

    FormService formService;
    CrudPageService pageService;
    DataFieldService dataFieldService;
    DynamicDataService dynamicDataService;
    DataModelService dataModelService;
    Map<String, IDataFieldHandler> fieldHandlerMap;
    DesignHandler designHandler;
    ArrayListConvert arrayListConvert;
    LocalDateTimeConvert localDateTimeConvert;
    DataNoticeHandler dataNoticeHandler;
    DataModelHandler dataModelHandler;
    IdentificationService identificationService;

    @Log
    @ApiOperation("修改数据模型")
    @PostMapping("/{modelId}")
    @Transactional(rollbackFor = Exception.class)
    public R<String> putDataModel(@PathVariable String appId, @RequestBody DataModelPo dataModelPo, @PathVariable String modelId) {
        DataModelPo byId = dataModelService.getById(modelId);
        if (ObjectNull.isNull(byId)) {
            return R.failed("没有找到数据模型");
        }
        if (!byId.getAppId().equals(appId)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        dataModelPo.setId(modelId);
        dynamicDataService.refreshData(modelId, dataModelPo);
        List<IndexFields> indexFields = byId.getIndexFields();
        dataModelPo.setIndexFields(indexFields);
        dataModelService.updateById(dataModelPo);
        return R.ok();
    }

    @Log
    @ApiOperation("修改数据模型名称")
    @PutMapping("/{modelName}/{modelId}")
    public R<String> putDataModelName(@PathVariable String appId, @PathVariable("modelId") String modelId, @PathVariable String modelName) {
        DataModelPo byId = dataModelService.getOne(Wrappers.query(new DataModelPo().setId(modelId).setAppId(appId)));
        if (ObjectNull.isNotNull(byId)) {
            byId.setName(modelName);
            dataModelService.updateById(byId);
            // 修改自定义标识冗余的设计名称
            identificationService.updateDesignName(modelId, modelName);
            return R.ok();
        }
        return R.failed("错误模型");
    }

    @Log
    @ApiOperation("修改数据模型数据集名称")
    @PutMapping("/collection_name/{modelId}")
    public R<String> putDataModelCollectionName(@PathVariable String appId, @PathVariable("modelId") String modelId, String collectionName) {
        if (ObjectNull.isNull(collectionName)) {
            return R.ok();
        }
        DataModelPo dataModel = dataModelService.getById(modelId);
        if (ObjectNull.isNull(dataModel) || Boolean.FALSE.equals(appId.equals(dataModel.getAppId()))) {
            throw new BusinessException("模型不存在");
        }
        if (collectionName.equals(dataModel.getCollectionName())) {
            return R.ok();
        }
        // 校验数据集名称全局唯一
        String tenantId = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();
        DataModelPo existsDataModel = dataModelService.getOne(Wrappers.<DataModelPo>lambdaQuery()
                .eq(DataModelPo::getBelongMode, ModeUtils.getMode())
                .and(and -> and.or(wrapper -> wrapper.eq(DataModelPo::getCollectionName, collectionName).or().eq(DataModelPo::getId, collectionName))));
        if (ObjectNull.isNotNull(existsDataModel)) {
            return R.failed("该数据集名已存在");
        }
        if (dataModelHandler.collectionExists(collectionName)) {
            return R.failed("该数据集名已存在");
        }
        TenantContextHolder.setTenantId(tenantId);
        // 得到现在的数据集名
        String oldCollectionName = Optional.ofNullable(dataModel.getCollectionName()).orElse(dataModel.getId());
        String oldLogCollectionName = DataModelUtil.buildLogCollectionName(oldCollectionName);
        String oldDelCollectionName = DataModelUtil.buildRemoveCollectionName(oldCollectionName);
        dataModel.setCollectionName(collectionName);
        dataModelService.updateById(dataModel);
        // 修改模型相关数据集名
        dataModelHandler.renameCollection(oldCollectionName, collectionName);
        dataModelHandler.renameCollection(oldLogCollectionName, oldLogCollectionName.replace(oldCollectionName, collectionName));
        dataModelHandler.renameCollection(oldDelCollectionName, oldDelCollectionName.replace(oldCollectionName, collectionName));
        log.info("将模型[{}]变更为[{}]", oldCollectionName, collectionName);
        return R.ok();
    }

    @Deprecated
    @Log
    @ApiOperation("删除模型")
    @DeleteMapping("/{modelId}")
    public R<String> putDataModelName(@PathVariable String appId, @PathVariable("modelId") String modelId) {
        dataModelService.remove(Wrappers.query(new DataModelPo().setAppId(appId).setId(modelId)));
        return R.ok();
    }

}
