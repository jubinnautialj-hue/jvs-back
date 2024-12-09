package cn.bctools.index.controller;


import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.*;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.index.annotation.FormFormQuery;
import cn.bctools.index.design.ComponentBaseInfo;
import cn.bctools.index.design.FormAttribute;
import cn.bctools.index.design.SelectedAttribute;
import cn.bctools.index.design.component.service.ComponentBaseService;
import cn.bctools.index.design.enums.ComponentType;
import cn.bctools.index.dto.FormQueryParamsBase;
import cn.bctools.index.dto.OptionsBase;
import cn.bctools.index.entity.SysHome;
import cn.bctools.index.utils.DesignCacheUtil;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.bctools.index.constant.ComponentConstant;
import cn.bctools.index.dto.FormQueryParamsDto;
import cn.bctools.index.dto.PresetRenderQueryDto;
import cn.bctools.index.service.SysHomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The type Sys home controller.
 *
 * @author jvs The type Sys home controller.
 */
@RestController
@RequestMapping("/sys/home")
@Api(tags = "[index]首页")
public class SysHomeController {
    static Map<String, String> urlMap = new HashMap<>();
    static List<ComponentBaseInfo> collect = null;
    /**
     * The Service map.
     */
    @Resource
    Map<String, ComponentBaseService> serviceMap;
    /**
     * The Sys home service.
     */
    @Resource
    SysHomeService sysHomeService;
    /**
     * The Oss template.
     */
    @Resource
    OssTemplate ossTemplate;
    /**
     * The Design cache util.
     */
    @Resource
    DesignCacheUtil designCacheUtil;

    /**
     * Component r.
     *
     * @return the r
     */
    @ApiOperation("获取当前这个业务项目有哪些组件,和组件属性")
    @GetMapping("/component")
    public R<List> component() {
        if (ObjectNull.isNotNull(collect)) {
            return R.ok(collect);
        }
        SecurityContext context = SecurityContextHolder.getContext();
        Map<String, Object> stringObjectMap = SystemThreadLocal.get();
        collect = serviceMap.values().parallelStream().map(e -> {
            SecurityContextHolder.setContext(context);
            SystemThreadLocal.setAll(stringObjectMap);
            ComponentBaseInfo generate = e.generate();
            Class<? extends FormQueryParamsBase> aClass = e.generateClass();
            if (ObjectNull.isNotNull(aClass)) {
                //解析对象，进行属性组装
                List<FormAttribute> list = getFormAttributes(aClass, e);
                generate.setFormAttributes(list);
                //设置基础属性类
                generate.setFormQueryParamsBaseClass(aClass.getSimpleName());
            }
            //设置类型
            generate.setType(ComponentType.bClass(generate.getClass()));
            if (generate.getType().equals(ComponentType.DesignApp)) {
                return null;
            }
            //设置名称
            if (ObjectNull.isNull(generate.getName())) {
                generate.setName(generate.getType().getDesc());
            }
            //设置 bean
            if (ObjectNull.isNull(generate.getBean())) {
                generate.setBean(e.getClass().getSimpleName());
            }
            return generate;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return R.ok(collect);
    }

    /**
     * 根据实体类解析属性参数值
     *
     * @param aClass 实体类对象属性,动态定义参数个参数和来源
     */
    private List<FormAttribute> getFormAttributes(Class<? extends FormQueryParamsBase> aClass, ComponentBaseService base) {
        //此处需要添加永久缓存
        //获取这个类的注解
        List<FormAttribute> collect = Arrays.stream(aClass.getFields()).filter(e -> e.isAnnotationPresent(FormFormQuery.class))
                .map(e -> {
                    FormFormQuery annotation = e.getAnnotation(FormFormQuery.class);
                    FormAttribute formAttribute = new FormAttribute().setDesc(annotation.desc()).setProp(e.getName()).setLinkProp(Arrays.asList(annotation.link())).setType(annotation.type()).setLabel(annotation.label());
                    switch (annotation.type()) {
                        case input:
                            //输入框不处理
                            break;
                        case tree:
                        case radio:
                        case select:
                        case selectMultiple:
                        case search:
                            Object copy = BeanCopyUtil.copy("{}", base.generateClass());
                            //获取具体的值
                            List<SelectedAttribute> init = base.fieldInitOrLink(e.getName(), (FormQueryParamsBase) copy);
                            formAttribute.setDicData(init);
                    }
                    return formAttribute;
                }).collect(Collectors.toList());
        return collect;
    }

    /**
     * Query all r.
     *
     * @return the r
     */
    @ApiOperation("所有的首页")
    @GetMapping("/query/all")
    public R<List<SysHome>> queryAll() {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        String clientId = currentUser.getClientId();
        List<SysHome> list = sysHomeService.list(new LambdaQueryWrapper<SysHome>()
                .select(SysHome::getId, SysHome::getRoles, SysHome::getSort, SysHome::getClientCode, SysHome::getTitle)
                .eq(SysHome::getClientCode, clientId));
        if (currentUser.getPlatformAdmin() || currentUser.getAdminFlag()) {
        } else {
            list.removeIf(e -> ObjectNull.isNull(e.getRoles()));
            //如果不包含的，角色全部都给删除掉
            list.removeIf(e -> e.getRoles().stream().allMatch(s -> !currentUser.getRoleIds().contains(s.getId())));
        }
        list.sort(Comparator.comparing(SysHome::getSort));
        return R.ok(list);
    }

    /**
     * Sort r.
     *
     * @param ids the ids
     * @return the r
     */
    @ApiOperation("排序")
    @PostMapping("/sort")
    public R sort(@RequestBody List<String> ids) {
        //根据ids 进行排序首页
        List<SysHome> sysHomes = sysHomeService.listByIds(ids);
        if (ObjectNull.isNotNull(sysHomes)) {
            List<SysHome> collect = sysHomes.stream().map(e -> e.setSort(ids.indexOf(e.getId()))).collect(Collectors.toList());
            sysHomeService.updateBatchById(collect);
        }
        return R.ok();
    }

    /**
     * Query r.
     *
     * @param id the id
     * @return the r
     */
    @ApiOperation("获取当前用户的首页设计")
    @GetMapping("/query")
    public R<SysHome> query(@RequestParam(value = "id", required = false) String id) {
        SysHome home = null;
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        String clientId = currentUser.getClientId();
        List<SysHome> list = sysHomeService.list(new LambdaQueryWrapper<SysHome>()
                .select(SysHome::getId, SysHome::getRoles, SysHome::getSort, SysHome::getClientCode, SysHome::getDesignContent, SysHome::getTitle)
                .eq(SysHome::getClientCode, clientId));
        if (currentUser.getPlatformAdmin() || currentUser.getAdminFlag()) {
        } else {
            list.removeIf(e -> ObjectNull.isNull(e.getRoles()));
            //如果不包含的，角色全部都给删除掉
            list.removeIf(e -> e.getRoles().stream().allMatch(s -> !currentUser.getRoleIds().contains(s.getId())));
        }
        list.sort(Comparator.comparing(SysHome::getSort));
        if (ObjectNull.isNull(list)) {
            return R.ok();
        } else {
            if (ObjectNull.isNotNull(id)) {
                home = list.stream().filter(e -> e.getId().equals(id)).findAny().get();
            } else {
                home = list.get(0);
            }
        }
        //组件属性动态解析控制
        String designContent = home.getDesignContent();
        JSONObject jsonObject = JSONUtil.parseObj(designContent);
        SecurityContext context = SecurityContextHolder.getContext();
        Map<String, Object> stringObjectMap = SystemThreadLocal.get();
        JSONArray array = jsonObject.getJSONArray("list");
        if (ObjectNull.isNull(array)) {
            return R.ok(home);
        }
        List<JSONObject> collect = array.parallelStream()
                .map(a -> {
                    SecurityContextHolder.setContext(context);
                    SystemThreadLocal.setAll(stringObjectMap);
                    Map map = (Map) a;
                    List<FormQueryParamsDto> queryParams = new ArrayList<>();
                    Object o1 = map.get(ComponentConstant.FORM_QUERY_PARAMS);
                    if (ObjectUtil.isNotNull(o1)) {
                        queryParams = JSONUtil.toList(StrUtil.toString(o1), FormQueryParamsDto.class);
                    }
                    Map<String, Object> metaData = (Map<String, Object>) map.getOrDefault(ComponentConstant.META_DATA, new HashMap<>(8));
                    //判断是否启用了缓存
                    Object o = metaData.get(ComponentConstant.COMPONENT_TYPE);
                    Boolean enableCache = designCacheUtil.check(metaData);
                    if (enableCache) {
                        Object data = designCacheUtil.getData(metaData);
                        if (data != null) {
                            map.put(ComponentConstant.DATA_LOCATION, data);
                            return JSONUtil.parseObj(map);
                        }
                    }

                    if (ObjectUtil.isNotNull(o)) {
                        String type = StrUtil.toString(o);
                        //通过不同的项目,自己的实现类型是不是有实现类, 根据实现类获取当前用户登陆的是否有权限控制的数据

                        ComponentType homeType = ComponentType.valueOf(type);
                        if (ObjectUtil.isNull(homeType)) {
                            return null;
                        }
                        Class<? extends ComponentBaseService> aClass = homeType.getAClass();
                        if (aClass == null) {
                            return null;
                        }
                        String name = String.valueOf(metaData.get(ComponentConstant.NAME));

                        ComponentBaseService bean = SpringContextUtil.getApplicationContext().getBeansOfType(aClass)
                                .values()
                                .stream()
                                .filter(e -> e.generate().getName().equals(name))
                                .findAny().get();

                        //兼容之前老的数据结构
                        Map<String, Object> paramsDtoMap = queryParams.stream().collect(Collectors.toMap(FormQueryParamsDto::getProp, FormQueryParamsDto::getValue));
                        //将Map 转对象属性
                        FormQueryParamsBase copy = (FormQueryParamsBase) BeanCopyUtil.copy(paramsDtoMap, bean.generateClass());
                        OptionsBase optionsBase = bean.fillData(copy);
                        if (ObjectNull.isNull(optionsBase) && paramsDtoMap.containsKey("mockData")) {
                            map.put(ComponentConstant.DATA_LOCATION, com.alibaba.fastjson2.JSONObject.parseObject(paramsDtoMap.get("mockData").toString()));
                        } else {
                            map.put(ComponentConstant.DATA_LOCATION, optionsBase);
                            if (enableCache) {
                                designCacheUtil.setData(metaData, optionsBase);
                            }
                        }
                    }

                    return JSONUtil.parseObj(map);
                }).filter(ObjectNull::isNotNull)
                .collect(Collectors.toList());
        jsonObject.set("list", collect);
        home.setDesignContent(JSONUtil.toJsonStr(jsonObject));
        return R.ok(home);
    }

    /**
     * 可能组件会有查询条件
     *
     * @param type        组件类型
     * @param prop        要获取的属性值
     * @param name        the name
     * @param queryParams 整个实体类对象
     * @return r r
     */
    @ApiOperation("根据type获取某一个组件的数据")
    @PostMapping("/type")
    public R type(@RequestParam("type") String type, @RequestParam("prop") String prop, @RequestParam("name") String name, @RequestBody Map<String, Object> queryParams) {
        ComponentType homeType = ComponentType.valueOf(type);
        if (ObjectUtil.isNull(homeType)) {
            return null;
        }
        Class<? extends ComponentBaseService> aClass = homeType.getAClass();
        if (aClass == null) {
            return null;
        }
        ComponentBaseService bean = SpringContextUtil.getApplicationContext().getBeansOfType(aClass)
                .values()
                .stream()
                .filter(e -> e.generate().getName().equals(name))
                .findAny().get();

        FormQueryParamsBase copy = (FormQueryParamsBase) BeanCopyUtil.copy(queryParams, bean.generateClass());
        Object optionsBase = bean.fieldInitOrLink(prop, copy);
        return R.ok(optionsBase);
    }

    /**
     * Gets render.
     *
     * @param dto the dto
     * @return the render
     */
    @ApiOperation("根据组件设计数据 获取组件渲染数据")
    @PostMapping("/get/data")
    public R getRender(@RequestBody PresetRenderQueryDto dto) {
        ComponentType homeType = ComponentType.valueOf(dto.getType());
        if (ObjectUtil.isNull(homeType)) {
            return null;
        }
        Class<? extends ComponentBaseService> aClass = homeType.getAClass();
        if (aClass == null) {
            return null;
        }
        ComponentBaseService bean = SpringContextUtil.getApplicationContext().getBeansOfType(aClass)
                .values()
                .stream()
                .filter(e -> e.generate().getName().equals(dto.getComponentMetaData().get(ComponentConstant.NAME)))
                .findAny().get();
        Map<String, Object> paramsDtoMap = dto.getQueryParams().stream().collect(Collectors.toMap(FormQueryParamsDto::getProp, FormQueryParamsDto::getValue));
        //将Map 转对象属性
        FormQueryParamsBase copy = (FormQueryParamsBase) BeanCopyUtil.copy(paramsDtoMap, bean.generateClass());
        OptionsBase optionsBase = bean.fillData(copy);
        return R.ok(optionsBase);
    }

    /**
     * Save design r.
     *
     * @param dto the dto
     * @return the r
     */
    @ApiOperation("保存首页设计")
    @PostMapping("/save")
    public R<SysHome> saveDesign(@RequestBody SysHome dto) {
        //这里处理保存图片转对应的相对地址
        String designContent = dto.getDesignContent();
        // 正则表达式
        String regex = "data:image/png;base64,[A-Za-z0-9+/=]+";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(designContent);
        // 当前替换的新数据索引
        int currentIndex = 0;
        StringBuilder modifiedString = new StringBuilder();
        // 替换设计
        // 逐个查找匹配
        while (matcher.find()) {
            String base64 = matcher.group();
            modifiedString.append(designContent, currentIndex, matcher.start());
            if (urlMap.containsKey(base64)) {
                String url = urlMap.get(base64);
                modifiedString.append(url);
            } else {
                byte[] decode = Base64.getDecoder().decode(base64.replaceAll("data:image/png;base64,", ""));
                InputStream byteArrayInputStream = new ByteArrayInputStream(decode);
                BaseFile baseFile = ossTemplate.putFile("jvs-public", IdGenerator.getIdStr() + ".png", byteArrayInputStream, "home");
                String url = ossTemplate.fileJvsPublicLink(baseFile.getFileName());
                urlMap.put(base64, url);
                modifiedString.append(url);
            }
            // 更新当前索引
            currentIndex = matcher.end();
        }
        modifiedString.append(designContent.substring(currentIndex));
        dto.setDesignContent(modifiedString.toString());
        //查询如果一个都没有，默认 title 为默认
        long count = sysHomeService.count(Wrappers.query(new SysHome().setClientCode(UserCurrentUtils.getCurrentUser().getClientId())));
        if (count == 0) {
            dto.setTitle("默认");
        }
        dto.setClientCode(UserCurrentUtils.getCurrentUser().getClientId());
        sysHomeService.saveOrUpdate(dto);
        return R.ok(dto);
    }


    /**
     * Delete r.
     *
     * @param id the id
     * @return the r
     */
    @ApiOperation("删除首页门户")
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable("id") String id) {
        //查询如果一个都没有，默认 title 为默认
        sysHomeService.removeById(id);
        return R.ok();
    }


}
