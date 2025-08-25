package cn.bctools.design.crud.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.mapper.FormMapper;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import cn.bctools.design.data.fields.dto.form.html.TableFormItemHtml;
import cn.bctools.design.data.fields.dto.form.item.FilterHtml;
import cn.bctools.design.data.fields.dto.form.item.TabItemHtml;
import cn.bctools.design.data.fields.dto.form.item.TypeHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.fields.impl.container.TabFieldHandler;
import cn.bctools.design.data.fields.impl.container.TableFormFieldHandler;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.menu.component.AppMenuHandler;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.permission.service.PermissionCompatibleService;
import cn.bctools.design.project.dto.ButtonSettingDto;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.template.dto.FormTemplateDto;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.mapper.FunctionBusinessMapper;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static cn.bctools.design.crud.utils.DesignUtils.parseField;
import static cn.bctools.design.data.fields.enums.DataFieldType.RESERVED_WORDS;

/**
 * 表单配置项
 *
 * @author auto
 */
@Slf4j
@Service
@AllArgsConstructor
@Design(DesignType.form)
public class FormServiceImpl extends ServiceImpl<FormMapper, FormPo> implements FormService, IJvsDesigner {

    JvsAppService appService;
    DataFieldService dataFieldService;
    DataModelService dataModelService;
    OssTemplate ossTemplate;
    AppMenuService appMenuService;
    TabFieldHandler tabFieldHandler;
    TableFormFieldHandler tableFormFieldHandler;
    AppMenuHandler appMenuHandler;
    FunctionBusinessMapper functionBusinessMapper;
    DynamicDataService dynamicDataService;
    FunctionBusinessMapper businessMapper;
    PermissionCompatibleService permissionCompatibleService;
    MapperMethodHandler mapperMethodHandler;

    @Override
    public FormPo get(String formId) {
        if (StringUtils.isBlank(formId)) {
            throw new BusinessException("表单id为空");
        }
        FormPo form = this.getById(formId);
        if (Objects.isNull(form)) {
            throw new BusinessException("表单不存在");
        }
        return form;
    }

    @Override
    public FormPo create(FormPo formPo) {
        if (ObjectNull.isNull(formPo.getName())) {
            formPo.setName("未命名表单");
        }
        // 添加应用类型
        String id = formPo.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr(formPo);
            formPo.setId(id);
        }
        String appId = formPo.getJvsAppId();
        // 应用类型   创建时根据已有类型新建设计, 不需要在此处添加
        // 初始化数据模型
        String dataModelId = formPo.getDataModelId();
        List<DataFieldPo> fields = new ArrayList<>();
        FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
        fields = DesignUtils.getFields(formDesignHtml, null, id);
        List<String> collect = fields.stream().filter(e -> RESERVED_WORDS.contains(e.getFieldKey())).map(DataFieldPo::getFieldKey).collect(Collectors.toList());
        if (ObjectNull.isNotNull(collect)) {
            throw new BusinessException("字段名不允许使用" + collect);
        }
        if (StringUtils.isBlank(dataModelId)) {
            dataModelId = dataModelService.createWithField(appId, id, DesignType.form, formPo.getName(), fields);
            formPo.setDataModelId(dataModelId);
        }
        formPo.setIsDeploy(true);
        this.save(formPo);
        associationSettingsFields(formPo, formDesignHtml, fields);
        //更新二维码进去
        updateById(formPo);
        appMenuHandler.addMenu(DesignType.form, formPo.getId(), formPo.getJvsAppId(), formPo.getDataModelId(), formPo.getName(), formPo.getType());
        return formPo;
    }

    /**
     * 检查出非必要的字段，并将其设置到设计中
     *
     * @param po
     * @param fields
     */
    @Override
    public void associationSettingsFields(FormPo po, FormDesignHtml formDesignHtml, List<DataFieldPo> fields) {
        //如果设计为空直接返回，创建列表的时候会自动创建一个新的表单
        if (ObjectNull.isNull(formDesignHtml.getFormdata())) {
            return;
        }
        Set<String> associationSettingsFields = new HashSet<>();
        //默认添加数据 id，确保左树结构的打开表单时能触发联联动回显
        associationSettingsFields.add("id");
        List<String> tablePath = new ArrayList<>();
        //判断
        //获取所有字段,做两次遍历， 然后将属性进行判断是否重复，如果重复，表示已经存在
        List<Map<String, Object>> forms = formDesignHtml.getFormdata().get(0).getForms();
        Map<String, String> fieldMap = forms.stream().collect(Collectors.toMap(e -> e.get("prop").toString(), JSON::toJSONString));
        fields.forEach(e -> {
            if (DataFieldType.CONTAINER.contains(e.getFieldType())) {
                //如果是选项卡，需要判断是否配置了 key
                if (e.getFieldType().equals(DataFieldType.tab)) {
                    TabItemHtml html = tabFieldHandler.toHtml(e.getDesignJson());
                    if (html.getDetachData()) {
                        //返回当前层级所有的key
                        for (FormValueHtml dicDatum : html.getDicData()) {
                            //未配置 key 时返回所有关联的字段
                            List<FieldBasicsHtml> fieldBasicsHtmls = html.getColumn().get(dicDatum.getName());
                            if (ObjectNull.isNull(fieldBasicsHtmls)) {
                                continue;
                            }
                            //脱离数据配置了 key后直接返回 key
                            if (ObjectNull.isNotNull(dicDatum.getProp())) {
                                associationSettingsFields.add(dicDatum.getProp());
                                //有配置的路径，
                                //记录表格路径
                                if (html.getColumn().containsKey(dicDatum.getProp())) {
                                    List<String> collect = html.getColumn().get(dicDatum.getProp())
                                            .stream()
                                            .filter(s -> DataFieldType.tableForm.equals(s.getType()))
                                            .map(s -> dicDatum.getProp() + StrUtil.DOT + s.getProp())
                                            .collect(Collectors.toList());
                                    tablePath.addAll(collect);
                                }
                            } else {
                                for (FieldBasicsHtml fieldBasicsHtml : fieldBasicsHtmls) {
                                    if (DataFieldType.CONTAINER.contains(fieldBasicsHtml.getType())) {
                                        associationSettingsFields.add(fieldBasicsHtml.getProp());
                                        //记录表格路径
                                        if (DataFieldType.tableForm.equals(fieldBasicsHtml.getType())) {
                                            if (ObjectNull.isNotNull(dicDatum.getProp())) {
                                                tablePath.add(dicDatum.getProp() + StrUtil.DOT + fieldBasicsHtml.getProp());
                                            } else {
                                                tablePath.add(fieldBasicsHtml.getProp());
                                            }

                                            if (ObjectNull.isNotNull(fieldBasicsHtml.getDataFilterGroupList())) {
                                                fieldBasicsHtml.getDataFilterGroupList()
                                                        .stream()
                                                        .flatMap(a -> a.stream()
                                                                .filter(as -> TypeHtml.prop.equals(as.getType()))
                                                                .map(as -> {
                                                                    if (as.getValue() instanceof List) {
                                                                        return (List) as.getValue();
                                                                    } else {
                                                                        return Collections.singletonList(as.getValue());
                                                                    }
                                                                })
                                                                .map(as -> as.stream().collect(Collectors.joining(".")))
                                                                .map(Object::toString))
                                                        .forEach(associationSettingsFields::add);
                                            }
                                        }
                                    } else {
                                        fieldMap.put(html.getProp(), JSON.toJSONString(fieldBasicsHtml));
                                    }
                                }
                            }
                        }
                    } else {
                        //记录表格路径
                        List<String> listStream = html.getColumn().keySet().stream()
                                .map(v -> html.getColumn().get(v)
                                        .stream()
                                        .filter(s -> DataFieldType.tableForm.equals(s.getType()))
                                        .map(s -> html.getProp() + StrUtil.DOT + v + StrUtil.DOT + s.getProp())
                                        .collect(Collectors.toList()))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
                        tablePath.addAll(listStream);

                        //获取表格中未开启数据脱离部分的表格
                        Map<String, List<FieldBasicsHtml>> column = html.getColumn();
                        for (String key : column.keySet()) {
                            //获取所有的表格字段
                            for (FieldBasicsHtml table : column.get(key)) {
                                if (!DataFieldType.tableForm.equals(table.getFieldType())) {
                                    TableFormItemHtml tableHtml = tableFormFieldHandler.toHtml(table.getDesignJson());
                                    if (ObjectNull.isNotNull(tableHtml)) {
                                        if (ObjectNull.isNotNull(tableHtml.getTableColumn())) {
                                            for (FieldBasicsHtml fieldBasicsHtml : tableHtml.getTableColumn()) {
                                                setHasRelationPropList(fieldBasicsHtml, html.getProp() + key + table.getProp());
                                            }
                                            //将数据重新设置到设计里
                                            table.setDesignJson(BeanToMapUtils.beanToMap(tableHtml));
                                        }
                                    }
                                }
                            }

                        }
                    }
                } else {
                    //todo 需要组件递归，并将有关联的公式字段，和全路径放到字段的设计中。
                    //表格的时候，需要解析表格里面的所有字段
                    TableFormItemHtml html = tableFormFieldHandler.toHtml(e.getDesignJson());
                    for (FieldBasicsHtml table : html.getTableColumn()) {
                        setHasRelationPropList(table, e.getFieldKey());
                    }
                    e.setDesignJson(BeanToMapUtils.beanToMap(html));
                    fieldMap.put(e.getFieldKey(), JSON.toJSONString(html));
                    //记录表格路径
                    tablePath.add(e.getFieldKey());
                    if (ObjectNull.isNotNull(html.getDataFilterGroupList())) {
                        html.getDataFilterGroupList()
                                .stream()
                                .flatMap(a -> a.stream()
                                        .filter(as -> TypeHtml.prop.equals(as.getType()))
                                        .map(as -> {
                                            if (as.getValue() instanceof List) {
                                                return (List) as.getValue();
                                            } else {
                                                return Collections.singletonList(as.getValue());
                                            }
                                        })
                                        .map(as -> as.stream().collect(Collectors.joining(".")))
                                        .map(Object::toString))
                                .forEach(associationSettingsFields::add);
                    }
                }
                associationSettingsFields.add(e.getFieldKey());
            }
        });

        //判断公式是否有设计到 字段
        functionBusinessMapper.selectList(new LambdaQueryWrapper<FunctionBusinessPo>().eq(FunctionBusinessPo::getDesignId, po.getId()))
                .stream()
                .peek(e -> associationSettingsFields.add(e.getBusinessId()))
                .flatMap(e -> e.getRelatedIds().stream())
                .forEach(e -> fieldMap.keySet().forEach(s -> {
                    //需要循环递归的选项
                    if (e.contains(s)) {
                        associationSettingsFields.add(s);
                    }
                    associationSettingsFields.add(e);
                }));

        for (Map<String, Object> form : forms) {
            Object prop = form.get("prop");
            for (String field : fieldMap.keySet()) {
                if (!prop.equals(field) && fieldMap.get(field).contains(prop.toString())) {
                    //进行匹配属性是否存在，如果存在，则添加上
                    associationSettingsFields.add(field);
                    //关联的字段和被关联的字段都需要添加上
                    associationSettingsFields.add(prop.toString());
                }
            }
        }
        List collect = fieldMap.values().stream().map(JSONObject::parseObject).collect(Collectors.toList());
        formDesignHtml.getFormdata().get(0).setForms(collect);
        //设置关联的字段
        po.setAssociationSettingsFields(associationSettingsFields);
        formDesignHtml.setTablePath(tablePath);
        po.setViewJson(JSONObject.toJSONString(formDesignHtml));
    }

    private void setHasRelationPropList(FieldBasicsHtml e, String path) {
        //过滤所有筛选数据获取其筛选条件，并将筛选条件的字段全路径放进来
        switch (e.getFieldType()) {
            case select:
            case radio:
            case cascader:
                if (CollectionUtils.isNotEmpty(e.getDataFilterGroupList())) {
                    //查询出关联的字段有哪一些
                    List<String> linkField = e.getDataFilterGroupList()
                            .stream()
                            .flatMap(Collection::stream)
                            .filter(s -> TypeHtml.prop.equals(s.getType()))
                            .map(FilterHtml::getValue)
                            .map(a -> {
                                if (a instanceof Collection) {
                                    return ((Collection<?>) a).stream().map(Object::toString).collect(Collectors.joining(StrUtil.DOT));
                                } else {
                                    return a.toString();
                                }
                            })
                            .collect(Collectors.toList());

                    String hasRelationPropList = "hasRelationPropList";
                    e.setHasRelationPropList(linkField);
                    e.setDesignJson((Map<String, Object>) JSONPath.set(e.getDesignJson(), hasRelationPropList, linkField));
                }
                break;
            default:

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormPo create(String dataModelId, String jvsAppId, String buttonName) {
        String name = dataModelService.getById(dataModelId).getName();
        FormPo formPo = new FormPo();
        formPo.setJvsAppId(jvsAppId);
        formPo.setDataModelId(dataModelId);
        formPo.setName(name + "_" + buttonName);
        this.create(formPo);
        return formPo;
    }

    @Override
    public FormPo deploy(String appId, String id) {
        FormPo formPo = this.getOne(Wrappers.query(new FormPo().setId(id).setJvsAppId(appId)));
        if (!Boolean.TRUE.equals(formPo.getIsDeploy())) {
            this.update(Wrappers.<FormPo>lambdaUpdate().set(FormPo::getIsDeploy, true).eq(FormPo::getId, id));
        }
        return formPo;
    }

    @Override
    public FormPo unload(String appId, String id) {
        FormPo formPo = this.getOne(Wrappers.query(new FormPo().setId(id).setJvsAppId(appId)));
        if (Boolean.TRUE.equals(formPo.getIsDeploy())) {
            this.update(Wrappers.<FormPo>lambdaUpdate().set(FormPo::getIsDeploy, false).eq(FormPo::getId, id));
        }
        return formPo;
    }

    @Override
    public List<FormTemplateDto> getTemplate(List<String> ids) {
        if (ObjectUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<FormPo> formPo = this.listByIds(ids);
        return BeanCopyUtil.copys(formPo, FormTemplateDto.class);
    }

    @Override
    public DesignRoleSettingDto getDesignRole(String designId) {
        FormPo form = this.getOne(Wrappers.<FormPo>lambdaQuery().select(FormPo::getDataModelId, FormPo::getJvsAppId).eq(FormPo::getId, designId));
        if (Objects.isNull(form)) {
            return null;
        }
        return permissionCompatibleService.getDesignPermission(form.getJvsAppId(), form.getId(), form.getDataModelId());
    }

    @Override
    public List<? extends ButtonSettingDto> getButtonSettings(String designId, String useCase) {
        FormPo form = this.getOne(Wrappers.<FormPo>lambdaQuery().select(FormPo::getViewJson).eq(FormPo::getId, designId));
        if (Objects.isNull(form)) {
            return Collections.emptyList();
        }
        String viewJson = form.getViewJson();
        FormDesignHtml formDesignHtml = DesignUtils.parseForm(viewJson);
        return formDesignHtml.getFormdata().get(0).getFormsetting().getBtnSetting();
    }

    @Override
    public void delete(String appId, String designId) {
        FormPo form = this.getOne(Wrappers.<FormPo>lambdaQuery()
                .select(FormPo::getIsDeploy)
                .eq(FormPo::getJvsAppId, appId)
                .eq(FormPo::getId, designId));
        if (Objects.isNull(form)) {
            return;
        }
        //同时删除这个设计下的字段
        dataFieldService.remove(new LambdaQueryWrapper<DataFieldPo>().eq(DataFieldPo::getDesignId, designId));
        this.removeById(designId);
    }

    @Override
    public void updateName(String appId, @Nullable String designId, @Nullable String name) {
        if (ObjectNull.isNull(name)) {
            return;
        }
        this.update(Wrappers.<FormPo>lambdaUpdate()
                .set(FormPo::getName, name)
                .eq(FormPo::getJvsAppId, appId)
                .eq(StringUtils.isNotBlank(designId), FormPo::getId, designId));
    }

    @Override
    public String getDataModelId(String appId, String designId) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(designId)) {
            return null;
        }
        return Optional.ofNullable(getOne(Wrappers.<FormPo>lambdaQuery()
                        .eq(FormPo::getJvsAppId, appId)
                        .eq(FormPo::getId, designId))).orElseGet(FormPo::new)
                .getDataModelId();
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<FormPo>lambdaQuery().eq(FormPo::getJvsAppId, appId));
    }

    @Override
    public FormPo getFormDetail(FormPo po, String appId, String id) {
        //判断权限类型
        FormDesignHtml design = DesignUtils.parseForm(po.getViewJson());
        if (ObjectNull.isNull(design.getFormdata())) {
            return po;
        }
        //TODO 根据表单结构判断是否存在动态关联属性关系,如果存在 ,则查询模型数据进行处理回显
        //找到了有多条存在数据关联的数据值
        for (Map<String, Object> map : design.getFormdata().get(0).getForms()) {
            //如果有值才处理
            if (!map.containsKey("dataLinkageEnable")) {
                continue;
            }
            //1、寻找这个模型设计,确定字段
            //获取多个字段集
            Optional<FieldBasicsHtml> formId = dataFieldService.getAllField(appId, String.valueOf(map.get("formId")))
                    .stream()
                    .filter(e -> id.equals(e.getDesignId()))
                    //必须要是多选的控件组件
                    .filter(s -> s.getType().equals(DataFieldType.checkbox))
                    .findFirst();
            //根据控件确定字段,根据控件确定组件模型数据
            if (formId.isPresent()) {
                //找到关联数据
                List<String> fieldKey = new ArrayList<>();
                fieldKey.add("id");
                String fieldKey1 = formId.get().getFieldKey();
                fieldKey.add(fieldKey1);
                String key = String.valueOf(((Map) map.get("props")).get("label"));
                fieldKey.add(key);
                //获取到多种不同的设备属性匹配规则
                List<Map<String, Object>> props = dynamicDataService.queryList(String.valueOf(map.get("formId")), fieldKey);
                for (Map<String, Object> form : design.getFormdata().get(0).getForms()) {
                    for (Map<String, Object> prop : props) {
                        //获取多选的属性值
                        try {
                            ArrayList arrayList = (ArrayList) prop.get(fieldKey1);
                            if (ObjectNull.isNotNull(arrayList) && arrayList.contains(form.get("prop"))) {
                                List displayExpress = (List) form.getOrDefault("displayExpress", new ArrayList<>());
                                displayExpress.add(Dict.create().set("prop", map.get("prop")).set("label", map.get("label")).set("value", prop.get("id")));
                                form.put("displayExpress", displayExpress);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("多选匹配失败:" + fieldKey1);
                        }
                    }
                }

            }
        }
        List<String> execs = new ArrayList<>();
        //根据结构添加公式组件数据
        List<FunctionBusinessPo> functionBusinessPos = businessMapper.selectList(Wrappers.query(new FunctionBusinessPo().setDesignId(po.getId())));
        if (ObjectNull.isNotNull(functionBusinessPos)) {
            execs.addAll(functionBusinessPos
                    .stream()
                    .peek(e -> execs.add(e.getBusinessId()))
                    .filter(e -> ObjectNull.isNotNull(e.getRelatedIds()))
                    .flatMap(e -> e.getRelatedIds().stream())
                    .distinct()
                    .collect(Collectors.toList()));
        }

        design.setExecs(execs);
        //处理表单中的脱敏
        DataModelPo byId = dataModelService.getById(po.getDataModelId());
        List<String> fields = dynamicDataService.encryptionData(byId);
        design.getFormdata().get(0).getForms().removeIf(e -> {
            FieldPublicHtml baseDto = parseField(e, FieldPublicHtml.class);
            if (fields.contains(baseDto.getProp())) {
                if (!String.class.equals(baseDto.getType().getAClass())) {
                    return true;
                }
            }
            return false;
        });
        //如果是移动端，需要移掉其它的控件
        if (IpUtil.isMobile()) {

            DesignUtils.filterMobile(design);
        }
        po.setViewJson(JSONObject.toJSONString(design));
        return po;
    }

    /**
     * 处理表单的整体结构， 获取将所有的关系图都嵌套组装为一个对象
     *
     * @param formDesignHtml
     * @param list
     * @param cls            字段结构转换获取数据
     */
    @Override
    public void structure(FormDesignHtml formDesignHtml, List<FieldHtml> list, Class<? extends FieldHtml> cls) {
        //获取所有的字段
        List<Map<String, Object>> forms = formDesignHtml.getFormdata().get(0).getForms();
        for (Map<String, Object> form : forms) {
            FieldHtml field = DesignUtils.parseField(form, cls);
            field.setPath(field.getProp());
            //判断是否是容器组件
            if (DataFieldType.CONTAINER.contains(field.getType())) {
                //目前容器只支持两种类型
                switch (field.getType()) {
                    case tableForm:
                        formField(form, field, cls);
                        break;
                    case tab:
                        //判断是否有下级结构
                        TabItemHtml tabItemHtml = tabFieldHandler.toHtml(form);
                        //构造一个中间数据层
                        if (tabItemHtml.getDetachData()) {
                            //返回当前层级所有的key
                            for (FormValueHtml dicDatum : tabItemHtml.getDicData()) {
                                //未配置 key 时返回所有关联的字段
                                List<FieldBasicsHtml> fieldBasicsHtmls = tabItemHtml.getColumn().get(dicDatum.getName());
                                if (ObjectNull.isNull(fieldBasicsHtmls)) {
                                    continue;
                                }
                                //脱离数据配置了 key后直接返回 key
                                if (ObjectNull.isNotNull(dicDatum.getProp())) {

                                    //没有直接是第一级
                                    List<FieldHtml> collect = fieldBasicsHtmls
                                            .stream()
                                            .map(a -> {
                                                FieldHtml fieldHtml = BeanCopyUtil.copy(a, cls).setPath(dicDatum.getProp() + StrUtil.DOT + a.getProp());
                                                switch (a.getType()) {
                                                    case tableForm:
                                                        formField(a.getDesignJson(), fieldHtml, cls);
                                                        break;
                                                    default:

                                                }
                                                return fieldHtml;
                                            })
                                            .collect(Collectors.toList());
                                    field.setChildren(collect);
                                } else {
                                    //没有直接是第一级
                                    List<FieldHtml> collect = fieldBasicsHtmls
                                            .stream()
                                            .map(a -> {
                                                FieldHtml fieldHtml = BeanCopyUtil.copy(a, cls).setPath(a.getProp());
                                                switch (a.getType()) {
                                                    case tableForm:
                                                        formField(a.getDesignJson(), fieldHtml, cls);
                                                        break;
                                                    default:

                                                }
                                                return fieldHtml;
                                            })
                                            .collect(Collectors.toList());
                                    list.addAll(collect);
                                }
                            }
                        } else {
                            Map<String, List<FieldBasicsHtml>> column = tabItemHtml.getColumn();
                            List<FieldHtml> tabList = column.keySet().stream().flatMap(e ->
                                    column.get(e).stream().map(a -> {
                                        FieldHtml fieldHtml = BeanCopyUtil.copy(a, cls).setPath(tabItemHtml.getProp() + e + a.getProp());
                                        //可能会存在表格操作
                                        switch (a.getType()) {
                                            case tableForm:
                                                //处理里面存在表格的情况
                                                formField(a.getDesignJson(), fieldHtml, cls);
                                                break;
                                            default:

                                        }
                                        return fieldHtml;
                                    })
                            ).collect(Collectors.toList());
                            field.setChildren(tabList);
                        }
                        break;
                    default:

                }
            }
            list.add(field);
        }
    }

    private void formField(Map<String, Object> form, FieldHtml field, Class<? extends FieldHtml> cls) {
        TableFormItemHtml html = tableFormFieldHandler.toHtml(form);
        List<FieldHtml> collect = html.getTableColumn().stream().map(e -> BeanCopyUtil.copy(e, cls))
                .peek(e -> e.setPath(field.getPath() + StrUtil.DOT + e.getProp()))
                .collect(Collectors.toList());
        field.setChildren(collect);
    }
}
