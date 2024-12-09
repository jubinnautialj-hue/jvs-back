package cn.bctools.design.menu.util;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.fields.dto.form.FormDataHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.FormSettingHtml;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import cn.bctools.design.data.fields.dto.page.PageDesignHtml;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.dto.PermissionIdentificationDto;
import cn.bctools.design.project.dto.ButtonSettingDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 设计资源工具
 */
public class DesignPermissionUtil {
    private DesignPermissionUtil() {

    }

    /**
     * 解析设计得到设计资源
     *
     * @param designType 设计类型
     * @param viewJson   列表json
     * @return 设计资源
     */
    public static PermissionIdentificationDto parseDesign(DesignType designType, String viewJson) {
        if (DesignType.page.equals(designType)) {
            return parsePageDesign(viewJson);
        }
        if (DesignType.form.equals(designType)) {
            return parseFormDesign(viewJson);
        }
        return null;
    }

    /**
     * 解析设计得到设计资源
     *
     * @param appMenu 菜单
     * @return 设计资源
     */
    public static PermissionIdentificationDto parseDesign(AppMenu appMenu) {
        PermissionIdentificationDto permission = Optional.ofNullable(appMenu.getPermission()).orElseGet(PermissionIdentificationDto::new);
        if (DesignType.URL.equals(appMenu.getDesignType())) {
            return parseUrlDesign(permission);
        }
        return permission;
    }

    /**
     * 解析设计得到设计资源
     *
     * @param viewJson 列表json
     * @return 设计资源
     */
    private static PermissionIdentificationDto parsePageDesign(String viewJson) {
        if (ObjectNull.isNull(viewJson)) {
            return null;
        }
        PageDesignHtml pageDesignHtml = DesignUtils.parsePage(viewJson);
        List<String> operation = Optional.ofNullable(pageDesignHtml.getButtons()).orElseGet(ArrayList::new).stream().map(ButtonSettingDto::getName).collect(Collectors.toList());
        List<String> treeOperation = Optional.ofNullable(pageDesignHtml.getLeftTreeButton()).orElseGet(ArrayList::new).stream().map(ButtonSettingDto::getName).collect(Collectors.toList());
        return new PermissionIdentificationDto()
                .setOperation(operation)
                .setTreeOperation(treeOperation);
    }

    /**
     * 解析设计得到设计资源
     *
     * @param viewJson 表单json
     * @return 设计资源
     */
    private static PermissionIdentificationDto parseFormDesign(String viewJson) {
        if (ObjectNull.isNull(viewJson)) {
            return null;
        }
        FormDesignHtml formDesignHtml = Optional.ofNullable(DesignUtils.parseForm(viewJson)).orElseGet(FormDesignHtml::new);
        FormDataHtml formDataHtml = Optional.ofNullable(formDesignHtml.getFormdata()).map(formDesign -> formDesign.get(0)).orElseGet(FormDataHtml::new);
        List<ButtonDesignHtml> btnSetting = Optional.ofNullable(formDataHtml.getFormsetting()).map(FormSettingHtml::getBtnSetting).orElseGet(ArrayList::new);
        List<String> operation = btnSetting.stream().map(ButtonSettingDto::getName).collect(Collectors.toList());
        return new PermissionIdentificationDto().setOperation(operation);
    }

    /**
     * 获取自定义页面的资源标识
     *
     * @return 自定义页面的资源标识
     */
    private static PermissionIdentificationDto parseUrlDesign(PermissionIdentificationDto permission) {
        List<Map<String, String>> urlOperation = permission.getUrlOperation();
        if (ObjectNull.isNotNull(urlOperation)) {
            return permission;
        }
        // 未配置自定义页面的资源标识，默认返回标识
        Map<String, String> defUrlOperation = new HashMap<>(1);
        defUrlOperation.put("def_view", "查看");
        permission.setUrlOperation(Collections.singletonList(defUrlOperation));
        return permission;
    }
}
