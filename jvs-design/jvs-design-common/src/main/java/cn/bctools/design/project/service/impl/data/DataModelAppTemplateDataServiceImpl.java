package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.util.DataModelUtil;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.design.util.ModeUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——数据模型
 */
@Service
@AllArgsConstructor
public class DataModelAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<DataModelPo> {

    private final DataModelService dataModelService;
    private final DataModelHandler dataModelHandler;

    @Override
    public List<DataModelPo> list(String jvsAppId, List<String> ids) {
        List<DataModelPo> dataModelList = dataModelService.list(Wrappers.query(new DataModelPo().setAppId(jvsAppId)));
        List<String> collect = dataModelList.stream()
                .peek(e -> e.setCreateTime(null))
                .map(DataModelPo::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return dataModelList;
    }


    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(dataModelService, existsIds, targetVersionTemplateBo.getDataModelPoList(), DataModelPo::getId);
        List<DataModelPo> newDataModelPos = templateBo.getDataModelPoList();
        // 新增或修改
        if (ObjectNull.isNull(newDataModelPos)) {
            return;
        }
        // 校验模型别名是否已存在， 若已存在，则提示那些别名已存在，并抛异常

        // 得到已存在的模型id
        List<String> ids = newDataModelPos.stream().map(DataModelPo::getId).collect(Collectors.toList());
        List<DataModelPo> existsDataModels = dataModelService.list(Wrappers.<DataModelPo>lambdaQuery().in(DataModelPo::getId, ids).select(DataModelPo::getId, DataModelPo::getCollectionName));
        List<String> dbExistsIds = existsDataModels.stream().map(DataModelPo::getId).collect(Collectors.toList());

        // 获取所有模型，用来校验数据集名称是否重名（数据集名称必须全局唯一，否则无法创建数据集）
        // 要修改或要创建的数据集名称若已存在，则抛异常
        // 模型要分模式存储，数据集要按模式分库存储
        AppVersionTypeEnum targetVersionType = targetAppVersion.getVersionType();
        List<DataModelPo> allModelList = getModeAllModel(targetVersionType);

        // 待修改的数据集别名集合
        List<UpdateCollectionName> updateCollectionNames = new ArrayList<>();
        // 待新增的模型集合
        List<DataModelPo> createModels = new ArrayList<>();
        // 待修改的模型集合
        List<DataModelPo> updateModels = new ArrayList<>();
        // 根据目标版本，设置模式上下文，以便将数据集保存到版本对应的数据库下
        SwitchModeDto currentMode = ModeUtils.getSwitchMode();
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(targetVersionType));
        newDataModelPos.forEach(model -> {
            model.setTenantId(null).setCreateTime(null);
            model.setBelongMode(targetVersionType);
            // 若目标版本为开发，则重置表编码标识
            if (AppVersionTypeEnum.DEV.equals(targetVersionType)) {
                model.setTableCode(IdWorker.get32UUID());
            }
            setAppVersion(model, DataModelPo::setAppVersion, targetAppVersion);
            // 校验重命名的模型数据集别名是否已存在,别名已存在时，创建新别名
            if(checkDuplicationCollectionNames(model, allModelList)) {
                String copyCollectionName = model.getCollectionName() + "_copy_" + new BigInteger(String.valueOf(IdWorker.getId())).toString(36);
                model.setCollectionName(copyCollectionName);
            }
            if (dbExistsIds.contains(model.getId())) {
                updateModels.add(model);
                // 得到待修改的模型别名集合
                buildUpdateCollectionNames(model, existsDataModels, updateCollectionNames);
            } else {
                createModels.add(model);
            }
        });

        if (ObjectNull.isNotNull(updateModels)) {
            // 修改数据
            dataModelService.updateBatchById(updateModels);
            // 修改模型数据集别名
            updateCollectionNames.forEach(u -> dataModelHandler.renameCollection(u.getOldCollectionName(), u.getNewCollectionName()));
        }

        if (ObjectNull.isNotNull(createModels)) {
            dataModelService.createBatch(createModels);
        }
        // 恢复模式上下文
        ModeUtils.setSwitchModel(currentMode);
    }


    /**
     * 得到指定模式全量数据模型
     *
     * @param targetMode 指定模式
     * @return 指定模式全量数据模型
     */
    private List<DataModelPo> getModeAllModel(AppVersionTypeEnum targetMode) {
        // 获取所有模型，用来校验数据集名称是否重名（数据集名称必须全局唯一，否则无法创建数据集）
        String tenantId = TenantContextHolder.getTenantId();
        TenantContextHolder.clear();
        JvsApp threadLocalApp = CurrentAppUtils.getApp();
        CurrentAppUtils.clear();
        List<DataModelPo> modelList = dataModelService.list(Wrappers.<DataModelPo>lambdaQuery()
                .select(DataModelPo::getId, DataModelPo::getCollectionName)
                .eq(DataModelPo::getBelongMode, targetMode));
        TenantContextHolder.setTenantId(tenantId);
        CurrentAppUtils.setApp(threadLocalApp);
        return modelList;
    }

    /**
     * 校验数据集别名，得到已存在的数据集别名集合
     *
     * @param dataModel                  待入库的模型
     * @param allModelList               已入库全量模型数据
     * @return true-别名已存在，false-别名不存在
     */
    private boolean checkDuplicationCollectionNames(DataModelPo dataModel, List<DataModelPo> allModelList) {
        String collectionName = dataModel.getCollectionName();
        if (ObjectNull.isNull(collectionName)) {
            return false;
        }
        // - 数据集名已存在:
        //      不是当前模型的的数据集名，则表示与其它模型的数据集别名重名了，记录重名的数据集名
        //      是当前模型的数据集名，则表示没有变更，不需要重命名；
        // - 数据集名不存在：
        //      检查数据存储库中是否存在同名的数据集别名
        Optional<DataModelPo> duplicationOptional = allModelList.stream()
                .filter(m -> collectionName.equals(m.getCollectionName()) || collectionName.equals(m.getId()))
                .findFirst();
        if (duplicationOptional.isPresent()) {
            if (dataModel.getId().equals(duplicationOptional.get().getId())) {
                return false;
            }
            return true;
        }

        if (dataModelHandler.collectionExists(collectionName)) {
            return true;
        }
        return false;
    }

    /**
     * 得到待变更的数据集名集合
     *
     * @param dataModel             待入库的模型
     * @param existsDataModels      已入库的模型
     * @param updateCollectionNames 待变更的数据集名集合
     */
    private void buildUpdateCollectionNames(DataModelPo dataModel, List<DataModelPo> existsDataModels, List<UpdateCollectionName> updateCollectionNames) {
        String collectionName = dataModel.getCollectionName();
        if (ObjectNull.isNull(collectionName)) {
            return;
        }
        Optional<DataModelPo> modelOptional = existsDataModels.stream().filter(m -> dataModel.getId().equals(m.getId())).findFirst();
        if (modelOptional.isPresent()) {
            DataModelPo model = modelOptional.get();
            if (collectionName.equals(model.getCollectionName())) {
                return;
            }
            String oldCollectionName = Optional.ofNullable(model.getCollectionName()).orElseGet(model::getId);
            String oldLogCollectionName = DataModelUtil.buildLogCollectionName(oldCollectionName);
            String oldDelCollectionName = DataModelUtil.buildRemoveCollectionName(oldCollectionName);
            updateCollectionNames.add(new UpdateCollectionName().setOldCollectionName(oldCollectionName).setNewCollectionName(collectionName));
            updateCollectionNames.add(new UpdateCollectionName().setOldCollectionName(oldLogCollectionName).setNewCollectionName(oldLogCollectionName.replace(oldCollectionName, collectionName)));
            updateCollectionNames.add(new UpdateCollectionName().setOldCollectionName(oldDelCollectionName).setNewCollectionName(oldDelCollectionName.replace(oldCollectionName, collectionName)));
        }
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        dataModelService.update(Wrappers.<DataModelPo>lambdaUpdate().eq(DataModelPo::getAppId, appId).set(DataModelPo::getAppVersion, appVersion));
    }


    /**
     * 待修改的数据集
     */
    @Data
    @Accessors(chain = true)
    private static class UpdateCollectionName {
        // 旧数据集名
        private String oldCollectionName;
        // 新数据集名
        private String newCollectionName;
    }

}
