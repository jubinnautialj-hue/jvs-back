package cn.bctools.design.permission.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.enums.DataRoleTypeEnum;
import cn.bctools.design.crud.mapper.CrudPageMapper;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.permission.service.PermissionCompatibleService;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author zhuxiaokang
 * 兼容新旧版本权限功能
 */
@Slf4j
@Component
@AllArgsConstructor
public class PermissionCompatibleServiceImpl implements PermissionCompatibleService {

    private final JvsAppService appService;
    private final AppMenuService appMenuService;
    private final CrudPageMapper crudPageMapper;
    private final DataModelService dataModelService;
    private final DesignPermissionService designPermissionService;

    @Override
    public DesignRoleSettingDto getDesignPermission(String appId, String designId, String dataModelId) {
        JvsApp jvsApp = appService.getById(appId);
        if (jvsApp.getEnableVersionFeature()) {
            return designPermissionService.getDesignPermission(appId, designId, dataModelId);
        } else {
            AppMenu appMenu = appMenuService.getDesignMenu(designId, appId);
            DesignRoleSettingDto settingDto = new DesignRoleSettingDto();
            settingDto.setRole(appMenu.getRoles());
            settingDto.setJvsAppId(appId);
            settingDto.setJvsAppName(jvsApp.getName());
            settingDto.setModeId(dataModelId);
            settingDto.setJvsAppCreateById(jvsApp.getCreateById());
            DataModelPo dataModelPo = dataModelService.getById(dataModelId);
            if (ObjectNull.isNotNull(dataModelPo.getRole())) {
                settingDto.setDataModelRole(dataModelPo.getRoles());
                settingDto.setEnableWorkflow(dataModelPo.getEnableWorkflow());
            }
            return settingDto;
        }
    }

    @Override
    public List<DesignRole> getOperationPermissions(String appId, String designId) {
        if (appService.appEnableVersionFeature(appId)) {
            return designPermissionService.getOperationPermissions(designId);
        } else {
            AppMenu appMenu = appMenuService.getDesignMenu(designId, appId);
            return appMenu.getRoles();
        }
    }

    @Override
    public DesignRoleSettingDto getRoleSetting(String dataModelId, String pageDesignId) {
        DataModelPo dataModelPo = dataModelService.getModel(dataModelId);
        boolean enableVersionFeature = appService.appEnableVersionFeature(dataModelPo.getAppId());

        // 获取权限
        Function<DesignRoleSettingDto, DesignRoleSettingDto> designRoleFunction = designRoleSettingDto ->
                Optional.ofNullable(designRoleSettingDto).orElseGet(DesignRoleSettingDto::new)
                        .setEnableVersionFeature(enableVersionFeature);
        if (enableVersionFeature) {
            // 无列表设计id，直接查询模型数据权限
            if (StringUtils.isBlank(pageDesignId)) {
                return designRoleFunction.apply(designPermissionService.getDataModelPermission(dataModelId));
            }
            // 有列表设计则查询包括设计的自定义数据权限和模型的设计权限
            return designRoleFunction.apply(designPermissionService.getDataModelPermission(pageDesignId, dataModelId));
        } else {
            // 无列表设计id，直接返回模型数据权限
            if (StringUtils.isBlank(pageDesignId)) {
                return designRoleFunction.apply(dataModelScope(dataModelPo));
            }

            // 根据列表设计id查询列表设计
            CrudPage crudPage = crudPageMapper.selectOne(Wrappers.<CrudPage>lambdaQuery()
                    .eq(CrudPage::getId, pageDesignId)
                    .select(CrudPage::getStepDataPermission, CrudPage::getId, CrudPage::getDataRole, CrudPage::getDataRoleType, CrudPage::getJvsAppId));
            if (ObjectNull.isNull(crudPage)) {
                log.warn("获取列表的数据权限失败：不存在设计id为[{}]的列表", pageDesignId);
                throw new BusinessException("列表不存在");
            }
            // 得到应该使用的数据权限
            DesignRoleSettingDto designRole = new DesignRoleSettingDto()
                    .setStepDataPermission(crudPage.getStepDataPermission());
            // 是否跳过数据权限：为null或为false-跳过数据权限， 则不需要获取已设置的数据权限配置
            if (ObjectNull.isNull(crudPage.getStepDataPermission()) || Boolean.FALSE.equals(crudPage.getStepDataPermission())) {
                return designRoleFunction.apply(designRole);
            }

            // 开启数据权限的情况
            // 根据使用的数据权限类型获取相应的数据权限设置（null或datamodel-数据模型权限）
            if (ObjectNull.isNull(crudPage.getDataRoleType()) || DataRoleTypeEnum.data_model.equals(crudPage.getDataRoleType())) {
                return designRoleFunction.apply(dataModelScope(dataModelPo));
            }
            // 使用列表自定义数据权限
            DynamicDataUtils.setDto(designRole);
            designRole.setDataModelRole(crudPage.getDataRoles());
            return designRoleFunction.apply(designRole);
        }
    }

    private DesignRoleSettingDto dataModelScope(DataModelPo model) {
        DesignRoleSettingDto designRole = new DesignRoleSettingDto();
        DynamicDataUtils.setDto(designRole);
        designRole.setDataModelRole(model.getRoles());
        designRole.setEnableWorkflow(model.getEnableWorkflow());
        return designRole;
    }

}
