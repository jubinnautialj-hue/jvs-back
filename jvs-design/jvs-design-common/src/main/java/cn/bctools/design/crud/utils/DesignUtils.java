package cn.bctools.design.crud.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.FormSelectItemDto;
import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.form.FormDataHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.FormSettingHtml;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.item.TabItemHtml;
import cn.bctools.design.data.fields.dto.page.*;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 设计数据处理类
 *
 * @Author: GuoZi
 */
public class DesignUtils {

    public static final Pattern COMPILE = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    public static final DataFieldHandler DATA_FIELD_HANDLER = SpringContextUtil.getBean(DataFieldHandler.class);

    public static PageDesignHtml parsePage(String json) {
        return JSON.parseObject(json, PageDesignHtml.class);
    }

    public static FormDesignHtml parseForm(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json, JSONConfig.create().setIgnoreNullValue(true));
        return BeanCopyUtil.copy(FormDesignHtml.class, jsonObject);
    }

    public static <T> T parseField(Object obj, Class<T> cls) {
        return BeanCopyUtil.copy(cls, obj);
    }

    /**
     * 校验列表页按钮名称
     *
     * @param page 列表页设计数据
     */
    public static void checkPageButton(PageDesignHtml page) {
        List<ButtonDesignHtml> buttons = page.getButtons();
        Set<String> buttonNames = buttons.stream().map(ButtonDesignHtml::getName).collect(Collectors.toSet());
        if (buttons.size() != buttonNames.size()) {
            throw new BusinessException("按钮名称重复");
        }
        StringBuffer str = new StringBuffer();
        buttons.stream()
                .filter(e -> e.getType().equals(ButtonTypeEnum.btn_page))
                // 筛选出有配置按钮的
                .filter(e -> ObjectNull.isNotNull(e.getPageBottomBtns()))
                .forEach(e -> {
                    for (PageBottonBtnsHtml pageBottomBtn : e.getPageBottomBtns()) {
                        if (ObjectNull.isNull(pageBottomBtn.getName())) {
                            str.append("按钮").append(e.getName()).append("按钮设置未配置完成\n");
                        }
                    }
                    for (PageQueryHtml pageBottomBtn : e.getPageQuery()) {
                        if (ObjectNull.isNull(pageBottomBtn.getFieldKey(), pageBottomBtn.getValue(), pageBottomBtn.getEnabledQueryTypes())) {
                            str.append("按钮").append(e.getName()).append("数据过滤未配置完成\n");
                        }
                    }
                    for (PageSearchHtml pageBottomBtn : e.getPageSearch()) {
                        if (ObjectNull.isNull(pageBottomBtn.getEnabledQueryTypes())) {
                            str.append("按钮").append(e.getName()).append("查询条件未配置完成\n");
                        }
                    }
                });
        if (ObjectNull.isNotNull(str.toString())) {
            throw new BusinessException(str.toString());
        }
    }

    /**
     * 校验表单按钮名称
     *
     * @param form 表单设计数据
     */
    public static void checkFormButton(FormDesignHtml form) {
        List<FormDataHtml> formData = form.getFormdata();
        if (ObjectUtils.isEmpty(formData)) {
            return;
        }
        FormDataHtml formDataHtml = formData.get(0);
        FormSettingHtml formSetting = formDataHtml.getFormsetting();
        List<ButtonDesignHtml> btnSetting = formSetting.getBtnSetting();
        if (ObjectUtils.isEmpty(btnSetting)) {
            return;
        }
        Set<String> uniqueButtonNames = new HashSet<>();
        for (ButtonDesignHtml button : btnSetting) {
            String name = button.getName();
            if (StringUtils.isBlank(name)) {
                throw new BusinessException("按钮名称不能为空");
            }
            if (!uniqueButtonNames.add(name)) {
                throw new BusinessException("按钮名称不能重复" + name);
            }
        }
    }

    /**
     * 校验字段
     *
     * @param fields 字段集合
     */
    public static void checkField(List<DataFieldPo> fields) {
        if (ObjectNull.isNull(fields)) {
            return;
        }
        fields.forEach(field -> {
            if (!COMPILE.matcher(field.getFieldKey()).matches()) {
                throw new BusinessException(field.getFieldName() + "字段名不符合规范");
            }
        });
    }

    /**
     * 获取列表页字段
     *
     * @param page        列表页设计数据
     * @param dataModelId 数据模型id
     * @param designId    设计id
     * @return 字段数据集合
     */
    public static List<DataFieldPo> getFields(PageDesignHtml page,
                                              @Nullable String dataModelId,
                                              @Nullable String designId) {
        if (Objects.isNull(page)) {
            return Collections.emptyList();
        }
        DataPageDesignHtml dataPage = page.getDataPage();
        if (Objects.isNull(dataPage)) {
            return Collections.emptyList();
        }
        List<DataTableFieldDesignHtml> fields = dataPage.getAutoTableFields();
        if (ObjectUtils.isEmpty(fields)) {
            return Collections.emptyList();
        }
        //检查是否配置有甘特图， 并字段没有选择上
        if (page.getGantt()) {
            if (ObjectNull.isNull(page.getGanttForm().getPlainStart())) {
                throw new BusinessException("甘特图计划开始时间未选择字段");
            }
            if (ObjectNull.isNull(page.getGanttForm().getPlainEnd())) {
                throw new BusinessException("甘特图计划结束时间未选择字段");
            }
            if (ObjectNull.isNull(page.getGanttForm().getReallyStart())) {
                throw new BusinessException("甘特图实际开始时间未选择字段");
            }
            if (ObjectNull.isNull(page.getGanttForm().getReallyEnd())) {
                throw new BusinessException("甘特图实际结束时间未选择字段");
            }
        }
        Set<String> filedset = new HashSet<>();
        List<DataFieldPo> collect = fields.stream().map(e -> {
            if (filedset.contains(e.getAliasColumnName())) {
                throw new BusinessException("字段名称重复请删除后保存", e.getShowChinese(), e.getAliasColumnName());
            }
            filedset.add(e.getAliasColumnName());
            DataFieldPo fieldDto = new DataFieldPo();
            fieldDto.setDesignId(designId);
            fieldDto.setModelId(dataModelId);
            fieldDto.setFieldKey(e.getAliasColumnName());
            fieldDto.setFieldName(e.getShowChinese());
            fieldDto.setIsExport(e.getIsExport());
            fieldDto.setDesignType(DesignType.page);
            // 列表页字段均为文本类型
            fieldDto.setFieldType(DataFieldType.input);
            return fieldDto;
        }).collect(Collectors.toList());
        collect.forEach(e -> {
            if (!COMPILE.matcher(e.getFieldKey()).matches()) {
                throw new BusinessException(e.getFieldName() + "字段名不符合规范");
            }
        });
        return collect;
    }

    /**
     * 获取表单字段
     *
     * @param forms       表单设计数据
     * @param dataModelId 数据模型id
     * @param designId    设计id
     * @return 字段数据集合
     */
    public static List<DataFieldPo> getFields(List<Map<String, Object>> forms,
                                              @Nullable String dataModelId,
                                              @Nullable String designId) {
        Set<String> filedset = new HashSet();
        List<DataFieldPo> collect = new ArrayList<>();
        forms.forEach(e -> {
            FieldPublicHtml baseDto = parseField(e, FieldPublicHtml.class);
            if ("id".equals(baseDto.getProp())) {
                if (!DataFieldType.input.equals(baseDto.getType())) {
                    throw new BusinessException("id只能为字符类型");
                }
            }
            //检查字段名只能以字母开始字母和数字结合
            if (!COMPILE.matcher(baseDto.getProp()).matches()) {
                throw new BusinessException(baseDto.getName() + "字段名不符合规范");
            }
            if (filedset.contains(baseDto.getProp())) {
                throw new BusinessException(baseDto.getName() + "字段名(" + baseDto.getProp() + ")称重复,请删除后保存");
            }
            filedset.add(baseDto.getProp());
            //将开启数据脱离字段也添加上去
            if (DataFieldType.tab.equals(baseDto.getType())) {
                TabItemHtml tabItemHtml = parseField(e, TabItemHtml.class);
                tabItemHtml.setDesignJson(e);
                if (tabItemHtml.getDetachData()) {
                    for (FormValueHtml dicDatum : tabItemHtml.getDicData()) {
                        if (ObjectNull.isNotNull(dicDatum.getProp())) {
                            if (!COMPILE.matcher(dicDatum.getProp()).matches()) {
                                throw new BusinessException(baseDto.getName() + "字段名不符合规范");
                            }
                            if (filedset.contains(dicDatum.getProp())) {
                                throw new BusinessException(dicDatum.getLabel() + "字段名(" + dicDatum.getProp() + ")称重复,请删除后保存");
                            }
                            filedset.add(dicDatum.getProp());
                        } else if (tabItemHtml.getColumn().containsKey(dicDatum.getName())) {
                            //没匹配的，获取所有的字段，也需要继续添加上去
                            List<FieldBasicsHtml> fieldBasicsHtmls = tabItemHtml.getColumn().get(dicDatum.getName());
                            for (int i = 0; i < fieldBasicsHtmls.size(); i++) {
                                FieldBasicsHtml fieldBasicsHtml = fieldBasicsHtmls.get(i);
                                if (!COMPILE.matcher(fieldBasicsHtml.getProp()).matches()) {
                                    throw new BusinessException(fieldBasicsHtml.getName() + "字段名不符合规范");
                                }
                                if (filedset.contains(fieldBasicsHtml.getProp())) {
                                    throw new BusinessException(fieldBasicsHtml.getLabel() + "字段名(" + fieldBasicsHtml.getProp() + ")称重复,请删除后保存");
                                }
                                filedset.add(fieldBasicsHtml.getProp());
                                Object eval = JSONPath.eval(tabItemHtml.getDesignJson(), "column." + dicDatum.getName() + "[" + i + "]");
                                DataFieldPo dataFieldPo = getDataFieldPo(dataModelId, designId, (Map<String, Object>) eval, fieldBasicsHtml);
                                collect.add(dataFieldPo);
                            }
                        }
                    }
                }
            }
            DataFieldPo fieldDto = getDataFieldPo(dataModelId, designId, e, baseDto);
            collect.add(fieldDto);
        });

        //判断是否是有工作流设计节点如果有一个增加默认工作流设计字段
        List<DataFieldPo> fieldPoList = collect.stream()
                .filter(e -> e.getFieldType().equals(DataFieldType.flowNode))
                .flatMap(e -> {
                    // 工作流设计id
                    DataFieldPo flowId = BeanCopyUtil.copy(e, DataFieldPo.class)
                            .setFieldKey(e.getFieldKey() + "_flowId")
                            .setFieldName(e.getFieldName() + "_流程id")
                            .setFieldType(DataFieldType.input);
                    // 动态添加的节点设计
                    DataFieldPo flowNewNode = BeanCopyUtil.copy(e, DataFieldPo.class)
                            .setFieldKey(e.getFieldKey() + "_dynamicNode")
                            .setFieldName(e.getFieldName() + "_动态节点")
                            .setFieldType(DataFieldType.input);
                    return Stream.of(flowId, flowNewNode);
                })
                .collect(Collectors.toList());
        collect.addAll(fieldPoList);
        return collect;
    }

    @NotNull
    private static DataFieldPo getDataFieldPo(@org.jetbrains.annotations.Nullable String dataModelId, @org.jetbrains.annotations.Nullable String designId, Map<String, Object> e, FieldPublicHtml baseDto) {
        //根据这个确定类型
        List<DataQueryType> enabledQueryTypes = DATA_FIELD_HANDLER.getEnabledQueryTypes(baseDto.getType(), e);
        DataFieldPo fieldDto = new DataFieldPo();
        fieldDto.setDesignId(designId);
        fieldDto.setModelId(dataModelId);
        fieldDto.setFieldKey(baseDto.getProp());
        fieldDto.setFieldName(baseDto.getLabel());
        fieldDto.setIsExport(baseDto.getType().isEnableExport());
        fieldDto.setDesignType(DesignType.form);
        // 列表页字段均为文本类型
        fieldDto.setFieldType(baseDto.getType());
        if (ObjectNull.isNotNull(enabledQueryTypes)) {
            fieldDto.setEnabledQueryTypes(JSON.parseArray(JSON.toJSONString(enabledQueryTypes)));
        }
        fieldDto.setDesignJson(e);
        return fieldDto;
    }

    /**
     * 获取列表页中, 表单的基本信息
     *
     * @param pageList 列表页集合
     * @return 表单集合
     */
    public static List<FormSelectItemDto> getFormsFromPage(List<CrudPage> pageList) {
        if (ObjectUtils.isEmpty(pageList)) {
            return Collections.emptyList();
        }
        List<FormSelectItemDto> result = new ArrayList<>();
        for (CrudPage page : pageList) {
            String pageName = page.getName();
            String viewJson = page.getViewJson();
            String dataModelId = page.getDataModelId();
            if (StringUtils.isBlank(viewJson)) {
                continue;
            }
            PageDesignHtml pageDesignHtml = DesignUtils.parsePage(viewJson);
            List<ButtonDesignHtml> buttons = pageDesignHtml.getButtons();
            if (ObjectUtils.isEmpty(buttons)) {
                continue;
            }
            for (ButtonDesignHtml button : buttons) {
                ButtonTypeEnum type = button.getType();
                if (ButtonTypeEnum.btn_add.equals(type) || ButtonTypeEnum.btn_modify.equals(type) || ButtonTypeEnum.btn_detail.equals(type) || ButtonTypeEnum.btn_form.equals(type)) {
                    result.add(new FormSelectItemDto()
                            .setId(button.getFormId())
                            .setDataModelId(dataModelId)
                            .setName(String.format("%s - %s", pageName, button.getName())));
                }
            }
        }
        return result;
    }

    public static List<DataFieldPo> getFields(FormDesignHtml form, String dataModelId, String designId) {
        if (Objects.isNull(form)) {
            //如果所有都是输入框
            return new ArrayList<>();
        }
        List<FormDataHtml> formData = form.getFormdata();
        if (ObjectUtils.isEmpty(formData)) {
            return new ArrayList<>();
        }
        FormDataHtml formDataHtml = formData.get(0);
        List forms = formDataHtml.getForms();
        return getFields(forms, dataModelId, designId);
    }

    /**
     * 过滤移动端显示数据
     */
    public static void filterMobile(FormDesignHtml form) {
        if (Objects.isNull(form)) {
            //如果所有都是输入框
            return;
        }
        List<FormDataHtml> formData = form.getFormdata();
        if (ObjectUtils.isEmpty(formData)) {
            return;
        }
        FormDataHtml formDataHtml = formData.get(0);
        List<Map<String, Object>> collect = formDataHtml.getForms()
                .stream()
                .filter(e -> {
                    FieldPublicHtml baseDto = parseField(e, FieldPublicHtml.class);
                    if (ObjectNull.isNull(baseDto)) {
                        return false;
                    }
                    return baseDto.getType().getMobile();
                }).filter(Objects::nonNull).collect(Collectors.toList());
        formData.get(0).setForms(collect);
    }
}
