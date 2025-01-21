package cn.bctools.design.crud;

import cn.bctools.auth.api.api.DictApi;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.crud.entity.CrudAssociationPo;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.service.JvsCrudAssociationService;
import cn.bctools.design.crud.service.JvsTreeService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.*;
import cn.bctools.design.data.fields.dto.form.FormDataHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.item.FilterHtml;
import cn.bctools.design.data.fields.dto.form.item.TypeHtml;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.util.DesignPermissionUtil;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.mapper.FunctionBusinessMapper;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static cn.bctools.design.data.fields.enums.DataFieldType.RESERVED_WORDS;

/**
 * 表单设计
 *
 * @author auto
 */
@Api(tags = "表单设计")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/form")
public class FormDesignController {

    DictApi dictApi;
    JvsTreeService treeService;
    FormService formService;
    CrudPageService pageService;
    JvsAppService jvsAppService;
    DataModelService dataModelService;
    DataFieldService dataFieldService;
    JvsCrudAssociationService jvsCrudAssociationService;
    DynamicDataService dynamicDataService;
    RuleDesignService ruleDesignService;
    Map<String, IDataFieldHandler> iDataFieldHandler;
    FunctionBusinessMapper execMapper;
    AppMenuService appMenuService;
    FunctionBusinessService functionBusinessService;

    @Log
    @ApiOperation(value = "表单-分页")
    @GetMapping("/page")
    public R<Page<FormPo>> page(Page<FormPo> page, FormPo formPo, @PathVariable String appId) {
        LambdaQueryWrapper<FormPo> wrapper = this.handleOrder(page);
        String type = formPo.getType();
        String name = StrUtil.trimToEmpty(formPo.getName());
        String desc = StrUtil.trimToEmpty(formPo.getDescription());
        formService.page(page, wrapper
                //查询非view_json的字段
                .select(FormPo.class, tableFieldInfo -> !tableFieldInfo.getProperty().equalsIgnoreCase(Get.name(FormPo::getViewJson)))
                //按表单类
                .eq(ObjectUtil.isNotNull(type), FormPo::getType, type)
                //应用表单只能引用当前应用下的单表
                .eq(ObjectUtil.isNotNull(formPo.getJvsAppId()), FormPo::getJvsAppId, appId)
                //按名称
                .like(StrUtil.isNotBlank(name), FormPo::getName, name)
                //按描述
                .like(StrUtil.isNotBlank(desc), FormPo::getDescription, desc));
        return R.ok(page);
    }

    @Log
    @ApiOperation(value = "下拉框选择")
    @GetMapping("/list")
    public R<List<FormSelectItemDto>> list(@ApiParam("名称(模糊搜索)") @RequestParam(name = "name", required = false) String name, @PathVariable String appId) {
        name = StrUtil.trimToEmpty(name);
        // 直接设计的表单
        List<FormPo> formPoList = formService.list(Wrappers.<FormPo>lambdaQuery().select(FormPo::getId, FormPo::getName, FormPo::getDataModelId, FormPo::getJvsAppId).eq(FormPo::getJvsAppId, appId).like(StrUtil.isNotBlank(name), FormPo::getName, name).orderByDesc(FormPo::getCreateTime));
        List<FormSelectItemDto> itemList = BeanCopyUtil.copys(formPoList, FormSelectItemDto.class);
        // 列表页中的表单
        List<FormSelectItemDto> pageFormList = pageService.getAllForm(name, appId);
        itemList.addAll(pageFormList);
        return R.ok(itemList);
    }

    @Log
    @ApiOperation("根据权限获取预览和使用的结果")
    @GetMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<FormPo> getById(@PathVariable("id") String id, @PathVariable String appId) {
        FormPo formPo = formService.getOne(Wrappers.query(new FormPo().setJvsAppId(appId).setId(id)));
        if (ObjectNull.isNull(formPo)) {
            return R.ok(formPo);
        }
        AppMenu appMenu = appMenuService.getDesignMenu(formPo.getId(), formPo.getJvsAppId());
        formPo.setAppMenu(appMenu);
        return R.ok(formPo);
    }

    @Log
    @ApiOperation(value = "拷贝组件", notes = "同时复制组件上面使用的公式值")
    @PostMapping("/copy/component/{designId}")
    public R copy(@RequestBody String json, @PathVariable String appId, @PathVariable String designId) {
        Map<String, FunctionBusinessPo> map = functionBusinessService.list(Wrappers.query(new FunctionBusinessPo().setDesignId(designId).setJvsAppId(appId))).stream().collect(Collectors.toMap(FunctionBusinessPo::getId, Function.identity()));
        if (ObjectNull.isNotNull(map)) {
            List<FunctionBusinessPo> list = new ArrayList<>();
            for (String oldId : map.keySet()) {
                if (json.contains(oldId)) {
                    FunctionBusinessPo functionBusinessPo = map.get(oldId).setId(IdWorker.getIdStr());
                    functionBusinessPo.setCreateTime(LocalDateTime.now());
                    functionBusinessPo.setUpdateTime(LocalDateTime.now());
                    list.add(functionBusinessPo);
                    //产生新的对象数据
                    json = json.replaceAll(oldId, functionBusinessPo.getId());
                }
            }
            functionBusinessService.saveBatch(list);
        }
        return R.ok(JSON.parseObject(json));
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("新增")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<FormPo> create(@RequestBody @Valid FormPo design, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        design.setJvsAppId(appId);
        return R.ok(formService.create(design));
    }

    @Log
    @ApiOperation("检查字段是否为类型变更")
    @PutMapping("/check")
    public R checkFields(@RequestBody @Valid FormDto design, @PathVariable String appId) {
        design.setJvsAppId(appId);
        if (ObjectNull.isNotNull(design.getViewJson())) {
            // 检查是否有数据类型发生改化，如果有类型发生变化会给出提示，并无法再继续保存
            FormDesignHtml form = JSON.parseObject(design.getViewJson(), FormDesignHtml.class);
            if (Objects.isNull(form)) {
                //如果所有都是输入框
                return R.ok();
            }
            List<FormDataHtml> formData = form.getFormdata();
            if (ObjectUtils.isEmpty(formData)) {
                return R.ok();
            }
            FormDataHtml formDataHtml = formData.get(0);
            List<Map<String, Object>> forms = formDataHtml.getForms();
            if (ObjectUtils.isEmpty(forms)) {
                return R.ok();
            }

            // 保存字段信息
            // 保存字段信息
            List<DataFieldPo> fieldPos = DesignUtils.getFields(forms, design.getDataModelId(), design.getId());
            List<String> collect = fieldPos.stream().filter(e -> RESERVED_WORDS.contains(e.getFieldKey())).map(DataFieldPo::getFieldKey).collect(Collectors.toList());
            if (ObjectNull.isNotNull(collect)) {
                throw new BusinessException("字段名不允许使用" + collect);
            }
            //校验系统字段的选择组件是否正确
            Map<String, DataFieldPo> defaultFields = dataFieldService.getDefaultFields("");
            fieldPos.forEach(e -> {
                if (defaultFields.containsKey(e.getFieldKey())) {
                    DataFieldType fieldType = defaultFields.get(e.getFieldKey()).getFieldType();
                    if (!e.getFieldType().equals(fieldType)) {
                        throw new BusinessException("系统默认字段的组件类型错误应该为" + fieldType.getDesc());
                    }
                }
            });
            Map<String, DataFieldPo> fields = fieldPos.stream().filter(e -> !e.getFieldType().equals(DataFieldType.input)).collect(Collectors.toMap(DataFieldPo::getFieldKey, Function.identity()));

            Map<String, List<DataFieldPo>> allFieldGroup = dataFieldService.list(Wrappers.query(new DataFieldPo().setDesignType(DesignType.form).setModelId(design.getDataModelId())).lambda().select(DataFieldPo::getFieldKey, DataFieldPo::getDesignJson, DataFieldPo::getDesignId, DataFieldPo::getFieldType)).stream().filter(e -> ObjectNull.isNotNull(e.getFieldType())).collect(Collectors.groupingBy(DataFieldPo::getFieldKey));
            //如果库里面没有其它类型
            if (ObjectNull.isNull(allFieldGroup)) {
                return R.ok();
            }
            //根据类型排序

            //查询出数据库中所有的类型，校验能不能和前端修改的类型进行处理，如果确定
            List<FieldPublicHtml> updateField = allFieldGroup.keySet().stream().peek(e -> {
                //添加现有的数据设计
                List<DataFieldPo> dataFieldPos = allFieldGroup.get(e);
                if (fields.containsKey(e)) {
                    dataFieldPos.add(BeanCopyUtil.copy(fields.get(e), DataFieldPo.class));
                }
            }).filter(fields::containsKey).map(e -> {
                //获取 库里面的类型
                List<DataFieldPo> dataFieldPos = allFieldGroup.get(e);
                //获取前端要修改的类型
                DataFieldPo dataFieldDto = fields.get(e);
                DataFieldType dataFieldType = dataFieldDto.getFieldType();
                for (DataFieldPo dataFieldPo : dataFieldPos) {
                    if (dataFieldPo.getFieldType().equals(dataFieldType)) {
                        //判断属性是否一致。如果不一致，不允许变更
                        IDataFieldHandler handler = iDataFieldHandler.get(dataFieldType.getDesc());
                        if (ObjectNull.isNotNull(handler)) {
                            FieldBasicsHtml html = handler.toHtml(dataFieldPo.getDesignJson());
                            FieldBasicsHtml dbHtml = handler.toHtml(dataFieldDto.getDesignJson());
                            try {
                                handler.checkFieldTypeAttributeChanged(html, dbHtml);
                            } catch (Exception ex) {
                                FormPo one = formService.getOne(new LambdaQueryWrapper<FormPo>().eq(FormPo::getId, dataFieldPo.getDesignId()).select(FormPo::getName, FormPo::getId));
                                FieldPublicHtml fieldBasicsHtml = new FieldPublicHtml();
                                fieldBasicsHtml.setFieldName(dataFieldDto.getFieldName() + "组件变更在[" + one.getName() + "]设计[" + ex.getMessage() + "]").setFieldKey(dataFieldDto.getFieldKey()).setType(dataFieldDto.getFieldType());
                                return fieldBasicsHtml;
                            }
                        }
                        continue;
                    }
                    if (!dataFieldPo.getFieldType().getTransformationList().contains(dataFieldType)) {
                        //TODO 有索引可能导致无法删除字段
                        FormPo one = formService.getOne(new LambdaQueryWrapper<FormPo>().eq(FormPo::getId, dataFieldPo.getDesignId()).select(FormPo::getName, FormPo::getId));
                        if (ObjectNull.isNull(one)) {
                            return null;
                        }
                        FieldPublicHtml fieldBasicsHtml = new FieldPublicHtml();
                        fieldBasicsHtml.setFieldName(dataFieldDto.getFieldName() + "组件变更在[" + one.getName() + "]设计[" + dataFieldPo.getFieldType().getDesc() + "]改变为[" + dataFieldDto.getFieldType().getDesc() + "]").setFieldKey(dataFieldDto.getFieldKey()).setType(dataFieldDto.getFieldType());
                        return fieldBasicsHtml;
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            //将变化的字段直接返回给前端，前端将变更后再次进行提交保存
            if (ObjectNull.isNotNull(updateField)) {
                return R.ok(updateField);
            }
        }
        return R.ok();
    }

    @GetMapping("/structure/{id}")
    @ApiOperation("获取表单的树形设计结构")
    public R structure(@PathVariable("id") String id) {
        FormPo formPo = formService.getById(id);
        FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
        //获取这个设计中所有设计字段和数据字段路径的关系

        Map<String, String> fieldPathMap = formPo.getFieldPathMap();

        List<FieldHtml> structure = new ArrayList<>();
        List<FunctionBusinessPo> functionBusinessPos = functionBusinessService.list(new LambdaQueryWrapper<FunctionBusinessPo>().select(FunctionBusinessPo::getBusinessId, FunctionBusinessPo::getRelatedIds).eq(FunctionBusinessPo::getDesignId, id)).stream()
                //过滤为空的数据
                .filter(s -> !s.getRelatedIds().isEmpty()).collect(Collectors.toList());
        //获取设计路径和公式路径的转换规则

        //如果没有值 ，直接返回
        if (functionBusinessPos.isEmpty()) {
            return R.ok();
        }
        //获取结构树
        formService.structure(formDesignHtml, structure, FieldJsonHtml.class);
        // 将树结构转换为同级结构
        List<FieldHtml> list = new ArrayList<>();
        //todo 处理返回设计的整体结构
        convertSameLevel(structure, list, e -> {
            //并且知道是有下拉筛选的才进行处理
            switch (e.getType()) {
                case select:
                case radio:
                case cascader:
                    //获取 json结构
                    Map<String, Object> designJson = ((FieldJsonHtml) e).getDesignJson();
                    if (!designJson.containsKey("dataFilterList")) {
                        return false;
                    }
                    FieldBasicsHtml html = iDataFieldHandler.get(e.getType().getDesc()).toHtml(designJson);
//                    //判断是否有数据筛选
                    //直接的不取全路径
                    List<String> collect = html.getDataFilterGroupList().stream().flatMap(Collection::stream).filter(a -> TypeHtml.prop.equals(a.getType())).map(FilterHtml::getValue).map(a -> {
                        if (a instanceof Collection) {
                            return ((Collection<?>) a).stream().map(Object::toString).collect(Collectors.joining(StrUtil.DOT));
                        } else {
                            return a.toString();
                        }
                    }).collect(Collectors.toList());
                    //获取自己这个路径
                    String path = e.getPath();
                    //递归根据公式找父级，并排除自己避免死循环
                    getHasRelationPropList(path, path, collect, collect, functionBusinessPos, fieldPathMap);
                    //重新将结构存放进去
                    designJson.put("hasRelationPropList", collect);
                    //删除设计 Json
                    return true;
                default:
                    return false;
            }
        });
        //将数据结构存放到设计字段中去

        return R.ok(list);
    }

    /**
     * 递归添加这个组件的关联筛选属性值
     *
     * @param fieldPath           字段的路径
     * @param thisPath            需要判断的路径
     * @param collect             关联的属性值
     * @param strings
     * @param functionBusinessPos 这个表单中所有的公式
     * @param fieldPathMap        字段路径的映射关系
     */
    private void getHasRelationPropList(String fieldPath, String thisPath, List<String> collect, List<String> strings, List<FunctionBusinessPo> functionBusinessPos, Map<String, String> fieldPathMap) {
        for (FunctionBusinessPo po : functionBusinessPos) {
            //如果路径一致，则不处理，直接执行下一次
            if (po.getBusinessId().equals(fieldPath) || po.getBusinessId().equals(thisPath)) {
                continue;
            }
            if (collect.contains(po.getBusinessId())) {
                for (String relatedId : po.getRelatedIds()) {
                    //避免死循环
                    if (collect.contains(relatedId)) {
                        continue;
                    }
                    String s = fieldPathMap.get(relatedId);
                    collect.add(s);
                    getHasRelationPropList(fieldPath, relatedId, collect, strings, functionBusinessPos, fieldPathMap);
                }
            }
            //进行数据匹配
            if (po.getRelatedIds().stream().anyMatch(collect::contains)) {
                //匹配到路径后，证明组件间是有关联的，需要将其把数据再存放进去
                if (collect.contains(po.getBusinessId())) {
                    continue;
                }
                String s = fieldPathMap.get(po.getBusinessId());
                collect.add(s);
                getHasRelationPropList(fieldPath, po.getBusinessId(), collect, strings, functionBusinessPos, fieldPathMap);
            }
        }
    }

    /**
     * 过滤出有下拉拉多选的组件
     *
     * @param structure
     * @param list
     */
    private void convertSameLevel(List<FieldHtml> structure, List<FieldHtml> list, Predicate<FieldHtml> predicate) {
        for (FieldHtml fieldHtml : structure) {
            if (ObjectNull.isNotNull(fieldHtml.getChildren())) {
                convertSameLevel(fieldHtml.getChildren(), list, predicate);
            }
            fieldHtml.setChildren(null);
            //根据条件处理是否继续筛选
            if (predicate.test(fieldHtml)) {
                list.add(fieldHtml);
            }
        }
    }


    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("更新")
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public R<FormPo> update(@RequestBody @Valid FormDto designDto, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        FormPo design = BeanCopyUtil.copy(designDto, FormPo.class);
        design.setJvsAppId(appId);
        String id = design.getId();
        String name = design.getName();
        String type = design.getType();
        String jvsAppId = design.getJvsAppId();
        String viewJson = design.getViewJson();
        String dataModelId = design.getDataModelId();
        List<DataFieldPo> fields = Collections.emptyList();

        // 修改模型名称
        dataModelService.updateName(dataModelId, name);
        AppMenu appMenu = new AppMenu().setName(design.getName()).setDesignType(DesignType.form).setJvsAppId(appId).setDataModelId(dataModelId).setDesignId(design.getId());
        appMenuService.update(appMenu);
        if (ObjectNull.isNotNull(viewJson)) {
            FormDesignHtml formDesignHtml = JSON.parseObject(viewJson, FormDesignHtml.class);
            if (Objects.isNull(formDesignHtml)) {
                //如果所有都是输入旷
                return R.ok();
            }
            //判断是否有表单引用 ，需要替换公式的值
            viewJson = getLinkFormHtml(formDesignHtml, id, viewJson);
            //这里需要二次转换,将新的 id处理过去
            formDesignHtml = JSON.parseObject(viewJson, FormDesignHtml.class);
            design.setViewJson(viewJson);
            List<FormDataHtml> formData = formDesignHtml.getFormdata();
            if (ObjectUtils.isEmpty(formData)) {
                return R.ok();
            }
            FormDataHtml formDataHtml = formData.get(0);
            //获取所有的按钮
            List<ButtonDesignHtml> btnSetting = formDataHtml.getFormsetting().getBtnSetting();
            appMenu.setPermissionJson(JSONArray.parseArray(JSON.toJSONString(btnSetting))).setRole(JSONArray.parseArray(JSON.toJSONString(designDto.getRole()))).setRoleType(designDto.getRoleType()).setPermission(DesignPermissionUtil.parseDesign(DesignType.form, design.getViewJson())).setIcon(StringUtils.defaultString(designDto.getIcon(), ""));
            appMenuService.update(appMenu);

            List<Map<String, Object>> forms = formDataHtml.getForms();
            if (ObjectUtils.isEmpty(forms)) {
                return R.ok();
            }

            DesignUtils.checkFormButton(formDesignHtml);
            // 保存字段信息
            fields = DesignUtils.getFields(formDesignHtml, dataModelId, id);
            String checkReserved = fields.stream().filter(e -> RESERVED_WORDS.contains(e.getFieldKey())).map(DataFieldPo::getFieldKey).collect(Collectors.joining(","));
            if (ObjectNull.isNotNull(checkReserved)) {
                throw new BusinessException("字段名不允许使用" + checkReserved);
            }
            formService.associationSettingsFields(design, formDesignHtml, fields);

            //获取按钮里面的规则
            //删除多余的数据
            jvsCrudAssociationService.remove(Wrappers.query(new CrudAssociationPo().setDataModelId(design.getDataModelId()).setJvsAppId(design.getJvsAppId()).setDesignId(design.getId())));
            //处理规则
            List<CrudAssociationPo> collect = btnSetting.stream().filter(e -> e.getEnable() && ObjectNull.isNotNull(e.getAssociation())).map(e -> new CrudAssociationPo().setDataModelId(design.getDataModelId()).setDesignId(design.getId()).setJvsAppId(design.getJvsAppId()).setName(e.getName()).setPermissionFlag(e.getPermissionFlag()).setData(e.getAssociation())).collect(Collectors.toList());
            if (ObjectNull.isNotNull(collect)) {
                jvsCrudAssociationService.saveBatch(collect);
            }
        }
        formService.updateById(design);
        // 处理数据模型
        dataFieldService.updateFields(appId, id, DesignType.form, dataModelId, fields);
        dataModelService.addModelField(appId, dataModelId, fields);
        return R.ok(design);
    }

    /**
     * 表单引用了公式，需要直接替换公式的数据值
     */
    private String getLinkFormHtml(FormDesignHtml formDesignHtml, String id, String viewJson) {
        if (ObjectNull.isNotNull(formDesignHtml.getLinkFormId())) {
            //根据公式的 Id进行替换数据
            List<FunctionBusinessPo> functionBusinessPos = execMapper.selectList(Wrappers.query(new FunctionBusinessPo().setDesignId(formDesignHtml.getLinkFormId())));
            for (FunctionBusinessPo e : functionBusinessPos) {
                //获取所有公式如果有公式则直接替换，并生成新的公式
                if (viewJson.contains(e.getId())) {
                    String businessId = e.getId();
                    e.setId(null);
                    e.setDesignId(id);
                    execMapper.insert(e);
                    viewJson = viewJson.replaceAll(businessId, e.getId());
                }
            }

        }
        //查询当前这个设计的全部公式，删除多余的
        List<FunctionBusinessPo> functionBusinessPos = execMapper.selectList(Wrappers.query(new FunctionBusinessPo().setDesignId(id)));
        for (FunctionBusinessPo e : functionBusinessPos) {
            if (!viewJson.contains(e.getId())) {
                execMapper.deleteById(e.getId());
            }
        }
        return viewJson;
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("发布")
    @PutMapping("/deploy/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deploy(@PathVariable("id") String id, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        formService.deploy(appId, id);
        return R.ok(true, "发布成功");
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @PostMapping("/unload/{id}")
    @ApiOperation("卸载")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> unload(@ApiParam("设计id") @PathVariable("id") String id, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        formService.unload(appId, id);
        return R.ok(true, "卸载成功");
    }

    @Deprecated
    @Log(callBackClass = JvsLogServiceImpl.class)
    @DeleteMapping("/del/{id}")
    @ApiOperation("删除")
    public R<Boolean> del(@PathVariable("id") String id, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        formService.delete(appId, id);
        return R.ok(true, "删除成功");
    }

    /**
     * 处理排序字段
     *
     * @param page 分页查询条件
     * @param <T>  查询对象
     * @return 查询条件对象
     */
    private <T extends BasalPo> LambdaQueryWrapper<T> handleOrder(Page<T> page) {
        QueryWrapper<T> wrapper = Wrappers.query();
        List<OrderItem> orders = page.orders();
        if (ObjectUtils.isEmpty(orders)) {
            wrapper.orderByDesc("create_time");
            return wrapper.lambda();
        }
        for (OrderItem order : orders) {
            String column = order.getColumn();
            boolean isAsc = order.isAsc();
            wrapper.orderBy(StringUtils.isNotBlank(column), isAsc, column);
        }
        return wrapper.lambda();
    }


    @GetMapping("/clear/{id}")
    @ApiOperation(value = "清空设计", notes = "清空设计，包括公式，逻辑")
    @Transactional(rollbackFor = Exception.class)
    public R clear(@PathVariable String id, @PathVariable String appId) {
        //清空公式
        execMapper.delete(Wrappers.query(new FunctionBusinessPo().setJvsAppId(appId).setDesignId(id)));
        //清空逻辑
        ruleDesignService.remove(Wrappers.query(new RuleDesignPo().setJvsAppId(appId).setComponentId(id).setComponentType(DesignType.form)));
        return R.ok();
    }

    @Log
    @ApiOperation(value = "获取指定模型的所有表单")
    @GetMapping("/model/{modelId}/all")
    public R<List<FormPo>> getModelAllForm(@PathVariable String appId, @PathVariable String modelId) {
        return R.ok(formService.list(Wrappers.<FormPo>lambdaQuery().eq(FormPo::getDataModelId, modelId).eq(FormPo::getJvsAppId, appId)
                //查询非view_json的字段
                .select(FormPo.class, tableFieldInfo -> !tableFieldInfo.getProperty().equalsIgnoreCase(Get.name(FormPo::getViewJson))).orderByDesc(FormPo::getCreateTime)));
    }
}
