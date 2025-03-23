package cn.bctools.design.crud.service.impl;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.crud.dto.AutoCreateCrudDesignDto;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.entity.enums.DataRoleTypeEnum;
import cn.bctools.design.crud.service.AutoCreateCrudDesignService;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.enums.FieldTypeEnum;
import cn.bctools.design.data.fields.dto.form.FormDataHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.FormSettingHtml;
import cn.bctools.design.data.fields.dto.page.*;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.fields.enums.FormTypeEnum;
import cn.bctools.design.data.service.DataFieldService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Service
@AllArgsConstructor
public class AutoCreateCrudDesignServiceImpl implements AutoCreateCrudDesignService {

    private final CrudPageService pageService;
    private final FormService formService;
    private final DataFieldService fieldService;
    private final Map<String, IDataFieldHandler> fieldHandlerMap;

    /**
     * 列表按钮表单基础信息
     */
    private static final String PATE_DESIGN_BUTTON_FORM_TEMPLATE = "{\n" +
            "        \"formdata\":[\n" +
            "          {\n" +
            "            \"forms\":[\n" +
            "            ],\n" +
            "            \"formsetting\":{\n" +
            "              \"labelposition\":\"top\",\n" +
            "              \"labelwidth\":80,\n" +
            "              \"formsize\":\"mini\",\n" +
            "              \"btnSetting\":[\n" +
            "              ],\n" +
            "              \"fullscreen\":false\n" +
            "            },\n" +
            "            \"autoTableFields\":[\n" +
            "            ]\n" +
            "          }],\n" +
            "        \"formType\":\"normalForm\"\n" +
            "      }";


    @Override
    public void generate(AutoCreateCrudDesignDto autoDto) {
        // 自动生成列表设计
        PageDesignHtml pageDesignHtml = buildPageDesignHtml(autoDto);
        CrudPage crudPage = new CrudPage()
                .setIsDeploy(true)
                .setDataModelId(autoDto.getModelId())
                .setType(autoDto.getMenuId())
                .setName(autoDto.getDesignName())
                .setJvsAppId(autoDto.getAppId());
        crudPage.setId(IdWorker.getIdStr(crudPage));
        crudPage.setViewJson(JSONObject.toJSONString(pageDesignHtml));
        //创建列表
        crudPage = pageService.create(crudPage);
        pageService.initButton(autoDto.getAppId(), crudPage.getDataModelId(), pageDesignHtml);
        pageService.updateDesign(crudPage.getId(), pageDesignHtml);

        //根据列表信息创建对应的表单并更新
        // 变更新增表单设计
        updateFormDesign(ButtonTypeEnum.btn_add, getPageFormId(ButtonTypeEnum.btn_add, pageDesignHtml), autoDto);
        // 变更修改表单设计
        updateFormDesign(ButtonTypeEnum.btn_modify, getPageFormId(ButtonTypeEnum.btn_modify, pageDesignHtml), autoDto);
        // 变更详情表单设计
        updateFormDesign(ButtonTypeEnum.btn_detail, getPageFormId(ButtonTypeEnum.btn_detail, pageDesignHtml), autoDto);
    }

    /**
     * 自动生成列表基础设计
     *
     * @param autoDto 自动生成设计参数
     * @return 列表基础设计
     */
    private PageDesignHtml buildPageDesignHtml(AutoCreateCrudDesignDto autoDto) {
        // 初始化按钮
        List<ButtonDesignHtml> buttons = listTemplatePageButton();
        List<DataTableFieldDesignHtml> autoTableFields = autoDto.getFields().stream()
                .map(e -> new DataTableFieldDesignHtml()
                        .setAliasColumnName(e.getFieldKey())
                        .setColumnName(e.getFieldKey())
                        .setDbJavaType(FieldTypeEnum.field_text)
                        .setShow(true)
                        .setShowChinese(e.getFieldName())
                        .setSupportShow(true)
                        .setSupportQuery(true)
                        .setSupportSettings(true)
                        .setSupportSort(false)
                        .setSupportStatistics(false))
                .collect(Collectors.toList());
        List<String> field = autoTableFields.stream().map(DataTableFieldDesignHtml::getFieldName).collect(Collectors.toList());
        DataPageDesignHtml datapage = new DataPageDesignHtml();
        datapage.setAutoTableFields(autoTableFields);
        datapage.setQueryJson("{\"size\":20,\"current\":1}");
        datapage.setDataPageJson("{\"records\":" + JSONObject.toJSONString(field) + ",\"current\":1,\"total\":0}");
        ArrayList<DesignRole> role = new ArrayList<>();
        role.add(new DesignRole());
        return new PageDesignHtml().setStepDataPermission(false).setSorts(new ArrayList<>()).setRole(role).setRoleType(true)
                .setParameters(new ArrayList<>())
                .setPageTableTitle(new ArrayList<>())
                .setName(autoDto.getDesignName())
                .setMenuWidth(150)
                .setMenuFixed(false)
                .setLeftTreeButton(new ArrayList<>())
                .setIcon("icon-haxi")
                .setDisplayType(LayoutEnum.table)
                .setDataRoleType(DataRoleTypeEnum.data_model)
                .setDataRole(new ArrayList<>())
                .setDataPage(datapage)
                .setButtons(buttons);
    }

    /**
     * 获取列表默认按钮结构设计模板
     *
     * @return 列表按钮集合
     */
    private List<ButtonDesignHtml> listTemplatePageButton() {
        List<ButtonDesignHtml> buttons = new ArrayList<>();
        {
            {
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setFormType("normalForm");
                buttonDesignHtml.setIsDefault(true);
                buttonDesignHtml.setMobileEnable(true);
                buttonDesignHtml.setName("新增");
                buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                buttonDesignHtml.setPosition("top");
                buttonDesignHtml.setType(ButtonTypeEnum.btn_add);
                buttonDesignHtml.setForm(JSONObject.parseObject(PATE_DESIGN_BUTTON_FORM_TEMPLATE, HashMap.class));
                buttons.add(buttonDesignHtml);
            }
            {
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setFormType("normalForm");
                buttonDesignHtml.setIsDefault(true);
                buttonDesignHtml.setMobileEnable(true);
                buttonDesignHtml.setName("修改");
                buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                buttonDesignHtml.setPosition("line");
                buttonDesignHtml.setType(ButtonTypeEnum.btn_modify);
                buttonDesignHtml.setForm(JSONObject.parseObject(PATE_DESIGN_BUTTON_FORM_TEMPLATE, HashMap.class));
                buttons.add(buttonDesignHtml);
            }
            {
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setFormType("detailForm");
                buttonDesignHtml.setIsDefault(true);
                buttonDesignHtml.setMobileEnable(true);
                buttonDesignHtml.setName("详情");
                buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                buttonDesignHtml.setPosition("line");
                buttonDesignHtml.setType(ButtonTypeEnum.btn_detail);
                buttonDesignHtml.setForm(JSONObject.parseObject(PATE_DESIGN_BUTTON_FORM_TEMPLATE, HashMap.class));
                buttons.add(buttonDesignHtml);
            }
            {
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setIsDefault(true);
                buttonDesignHtml.setMobileEnable(true);
                buttonDesignHtml.setName("导入");
                buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                buttonDesignHtml.setPosition("top");
                buttonDesignHtml.setType(ButtonTypeEnum.btn_import);
                buttons.add(buttonDesignHtml);
            }
            {
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setIsDefault(true);
                buttonDesignHtml.setMobileEnable(true);
                buttonDesignHtml.setName("导出");
                buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                buttonDesignHtml.setPosition("top");
                buttonDesignHtml.setExportFields(new ArrayList<>());
                buttonDesignHtml.setType(ButtonTypeEnum.btn_export);
                buttons.add(buttonDesignHtml);
            }
            {
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setIsDefault(true);
                buttonDesignHtml.setMobileEnable(true);
                buttonDesignHtml.setName("下载模板");
                buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                buttonDesignHtml.setPosition("top");
                buttonDesignHtml.setExportFields(new ArrayList<>());
                buttonDesignHtml.setType(ButtonTypeEnum.btn_download_template);
                buttons.add(buttonDesignHtml);
            }
            {
                ButtonDesignHtml buttonDesignHtml = new ButtonDesignHtml();
                buttonDesignHtml.setEnable(true);
                buttonDesignHtml.setIsDefault(true);
                buttonDesignHtml.setMobileEnable(true);
                buttonDesignHtml.setName("删除");
                buttonDesignHtml.setPermissionFlag(IdWorker.get32UUID());
                buttonDesignHtml.setPosition("line");
                buttonDesignHtml.setExportFields(new ArrayList<>());
                buttonDesignHtml.setType(ButtonTypeEnum.btn_delete);
                buttons.add(buttonDesignHtml);
            }
        }
        return buttons;
    }

    /**
     * 获取列表中指定按钮类型对应的表单id
     * <p>
     * 默认只有新增、修改、详情按钮有表单id
     *
     * @param pageButtonType 列表按钮类型
     * @param pageDesignHtml 列表设计
     * @return 表单id
     */
    private String getPageFormId(ButtonTypeEnum pageButtonType, PageDesignHtml pageDesignHtml) {
        return pageDesignHtml.getButtons().stream().filter(btn -> pageButtonType.equals(btn.getType())).findAny().get().getFormId();
    }

    /**
     * 修改列表对应的表单设计
     *
     * @param pageButtonType 列表按钮类型
     * @param formId         表单id
     * @param autoDto        自动生成设计参数
     */
    private void updateFormDesign(ButtonTypeEnum pageButtonType, String formId, AutoCreateCrudDesignDto autoDto) {
        //新增的表单
        FormPo formPo = formService.getById(formId);
        FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
        //根据字段和类型，进行添加了初始化字段
        List<Map<String, Object>> formsList = autoDto.getFields().stream().map(e -> {
            return fieldHandlerMap.get(e.getType()).generate(e.getFieldName(), e.getFieldKey(), e.getDicData());
        }).map(e -> (Map<String, Object>) e).collect(Collectors.toList());
        List<FormDataHtml> formdata = formDesignHtml.getFormdata();
        if (ObjectNull.isNull(formdata)) {
            formdata = new ArrayList<>();
            FormDataHtml e = new FormDataHtml();
            formdata.add(e);
            formDesignHtml.setFormdata(formdata);
        }
        List<String> fields = autoDto.getFields().stream().map(AutoCreateCrudDesignDto.Field::getFieldKey).collect(Collectors.toList());

        if (ButtonTypeEnum.btn_add.equals(pageButtonType)) {
            List<DataFieldPo> datafields = formsList.stream().map(e ->
                            BeanCopyUtil.copy(e, DataFieldPo.class)
                                    .setFieldKey(e.get("prop").toString())
                                    .setFieldName(e.get("label").toString())
                                    .setFieldType(DataFieldType.valueOf(e.get("type").toString()))
                                    .setDesignJson(e))
                    .collect(Collectors.toList());
            fieldService.updateFields(autoDto.getAppId(), formPo.getId(), DesignType.form, formPo.getDataModelId(), datafields);
        }
        if (ButtonTypeEnum.btn_detail.equals(pageButtonType)) {
            List<DataFieldPo> datafields = formsList.stream().map(e -> {
                        e.put("disabled", true);
                        return BeanCopyUtil.copy(e, DataFieldPo.class)
                                .setFieldKey(e.get("prop").toString())
                                .setFieldName(e.get("label").toString())
                                .setFieldType(DataFieldType.valueOf(e.get("type").toString()))
                                .setDesignJson(e);
                    })
                    .collect(Collectors.toList());
            fieldService.updateFields(autoDto.getAppId(), formPo.getId(), DesignType.form, formPo.getDataModelId(), datafields);
        }

        formDesignHtml.getFormdata().get(0).setForms(formsList);
        List<ButtonDesignHtml> btnSetting = ButtonTypeEnum.btn_add.equals(pageButtonType) || ButtonTypeEnum.btn_modify.equals(pageButtonType) ? listTemplateFormButton(formId) : new ArrayList<>();
        formDesignHtml.getFormdata().get(0)
                .setFormJson(JSONObject.toJSONString(fields))
                .setFormsetting(new FormSettingHtml()
                        .setLabelposition("top")
                        .setLabelwidth(80)
                        .setFormsize("mini")
                        .setFullscreen(false)
                        .setSubmitBtn(true)
                        .setEmptyBtn(false)
                        .setCancal(false)
                        .setBtnSetting(btnSetting)
                        .setPopupWidth(50)
                        .setPopupType("drawer"));

        formDesignHtml.setFormType(FormTypeEnum.normalForm);
        formPo.setViewJson(JSONObject.toJSONString(formDesignHtml));
        formService.updateById(formPo);
    }


    /**
     * 获取表单默认按钮结构设计模板
     *
     * @return 表单按钮集合
     */
    private List<ButtonDesignHtml> listTemplateFormButton(String formId) {
        ArrayList<ButtonDesignHtml> btnSetting = new ArrayList<>();
        ButtonDesignHtml submitButtonDesignHtml = new ButtonDesignHtml();
        submitButtonDesignHtml.setEnable(true);
        submitButtonDesignHtml.setName("提交");
        submitButtonDesignHtml.setButtonType("submit");
        submitButtonDesignHtml.setFlag(true);
        submitButtonDesignHtml.setPermissionFlag(formId + "-" + submitButtonDesignHtml.getButtonType() + "-" + IdGenerator.getIdStr(36));
        btnSetting.add(submitButtonDesignHtml);

        ButtonDesignHtml emptyButtonDesignHtml = new ButtonDesignHtml();
        emptyButtonDesignHtml.setEnable(true);
        emptyButtonDesignHtml.setName("重置");
        emptyButtonDesignHtml.setButtonType("empty");
        emptyButtonDesignHtml.setFlag(true);
        emptyButtonDesignHtml.setPermissionFlag(formId + "-" + emptyButtonDesignHtml.getButtonType() + "-" + IdGenerator.getIdStr(36));
        btnSetting.add(emptyButtonDesignHtml);

        ButtonDesignHtml cancelButtonDesignHtml = new ButtonDesignHtml();
        cancelButtonDesignHtml.setEnable(true);
        cancelButtonDesignHtml.setName("取消");
        cancelButtonDesignHtml.setButtonType("cancel");
        cancelButtonDesignHtml.setFlag(true);
        cancelButtonDesignHtml.setPermissionFlag(formId + "-" + cancelButtonDesignHtml.getButtonType() + "-" + IdGenerator.getIdStr(36));
        btnSetting.add(cancelButtonDesignHtml);
        return btnSetting;
    }

}
