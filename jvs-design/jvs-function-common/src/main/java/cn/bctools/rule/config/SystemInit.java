package cn.bctools.rule.config;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.rule.annotations.*;
import cn.bctools.rule.common.*;
import cn.bctools.rule.dto.OptionsType;
import cn.bctools.rule.dto.RuleFunctionDto;
import cn.bctools.rule.dto.RuleFunctionDtoParameter;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.mapper.RuleOptionDao;
import cn.bctools.rule.po.RuleOptionPo;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ClassUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guojing
 * @describe
 */
@Slf4j
@Order(100)
@Component
@Import({SpringContextUtil.class, RestTemplate.class})
@Getter
@AllArgsConstructor
public class SystemInit extends SpringContextUtil {

    /**
     * 如果是启动了多个服务,其它人操作可能会存在问题
     */
    private static Map<String, RuleFunctionDto> functionsMap = new LinkedHashMap<>();
    @Getter
    private static Map<String, ParameterSelected> selectedMap = null;
    private static Map<String, LinkFieldSelected> fieldSelectedMap = new LinkedHashMap<>();
    private static Map<String, List<OptionsDto>> selectOption = new HashMap<>(10);

    public static Collection<RuleFunctionDto> getFunctionsMaps() {
        return functionsMap.values();
    }

    public static Map<String, RuleFunctionDto> getFunctionsMap(Collection o, String functionName) {
        StringBuffer name = new StringBuffer("");
        Map<String, RuleFunctionDto> collect = functionsMap.values().stream()
                //过滤不禁用的
                .filter(e -> {
                    return !e.isDemoDisabled();
                })
                .map(e -> {
                    if (ObjectNull.isNotNull(functionName)) {
                        boolean matchName = e.getFunctionName().toLowerCase().contains(functionName.toLowerCase()) || PinyinUtils.getCameCasePinYin(e.getFunctionName()).toLowerCase().contains(functionName.toLowerCase());
                        if (!matchName) {
                            return null;
                        }
                    } else if (!o.contains(e.getGroup())) {
                        return null;
                    }
                    if (!e.getStatus()) {
                        //如果不可用直接返回,不加载
                        return e;
                    }
                    if (e.getSkipRefresh()) {
                        //如果是非动态的,不刷新加载
                        return e;
                    }
                    long l = System.currentTimeMillis();
                    RuleFunctionDto selected = getSelected(e, v -> selectedMap.get(v));
                    name.append("\n加载").append(e.getFunctionName()).append("模块,消耗").append(System.currentTimeMillis() - l).append("时间");
                    return selected;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(RuleFunctionDto::getFunctionName, Function.identity()));
        log.info(name.toString());
        return collect;
    }

    public static RuleFunctionDto getFunctionsBase(String functionName) {
        return functionsMap.get(functionName);
    }

    /**
     * 初始化外部扩展服务的动态属性
     *
     * @param name
     * @return
     */
    public static List<OptionsDto> getGroupNameOption(String name) {
        if (selectOption.containsKey(name)) {
            return selectOption.get(name);
        }
        List<OptionsDto> optionsDtos = ClassUtil.scanPackageByAnnotation("cn.bctools", SelectOption.class).stream().filter(e ->
                        e.getAnnotation(SelectOption.class).value().equals(name)
                )
                .map(e -> {
                    return Arrays.stream(e.getFields())
                            .map(v -> new OptionsDto().setType(v.getAnnotation(SelectOptionField.class).type()).setFiled(v.getName()).setName(v.getAnnotation(SelectOptionField.class).value()))
                            .collect(Collectors.toList());
                }).findFirst().orElseGet(() -> new ArrayList<>());
        selectOption.put(name, optionsDtos);
        return optionsDtos;
    }

    protected static final Map<Class, Map<String, ParameterValue>> fieldTypeMap = new HashMap<>(10);

    /**
     * 根据参数类，获取这个参数类的所有属性类型
     *
     * @param cls
     * @return
     */
    public static Map<String, ParameterValue> getParameterMap(Class<?> cls) {
        if (fieldTypeMap.containsKey(cls)) {
            return fieldTypeMap.get(cls);
        }
        Map<String, ParameterValue> fieldMap = Arrays.stream(cls.getDeclaredFields()).filter(a -> a.isAnnotationPresent(ParameterValue.class))
                .collect(Collectors.toMap(Field::getName, a1 -> a1.getAnnotation(ParameterValue.class)));
        fieldTypeMap.put(cls, fieldMap);
        return fieldMap;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        super.setApplicationContext(context);
        //项目初始化操作
        fieldSelectedMap = context.getBeansOfType(LinkFieldSelected.class);
        List<RuleFunctionDto> functions = getFunctions(context).stream().sorted(Comparator.comparing(RuleFunctionDto::getOrder).reversed()).collect(Collectors.toList());
        functionsMap = functions.stream().collect(Collectors.toMap(RuleFunctionDto::getFunctionName, Function.identity()));
        selectedMap = context.getBeansOfType(ParameterSelected.class);
    }

    /**
     * 获取所有的操作方法函数
     *
     * @param context
     * @author guojing
     * @returnType
     */
    List<RuleFunctionDto> getFunctions(ApplicationContext context) {
        Map<String, BaseCustomFunctionInterface> beansOfType = context.getBeansOfType(BaseCustomFunctionInterface.class);
        JvsSystemConfig bean = context.getBean(JvsSystemConfig.class);
        boolean equals = bean.getDomain().equals("bctools.cn");

        List<RuleFunctionDto> functionPoList = beansOfType.keySet().stream()
                .filter(e -> {
                    Class<? extends BaseCustomFunctionInterface> aClass = beansOfType.get(e).getClass();
                    if (aClass.isAnnotationPresent(Rule.class)) {
                        return true;
                    }
                    log.error(" 方法没有添加Rule注解不会被正常使用", e);
                    return false;
                })
                .map(e -> {
                    Class<? extends BaseCustomFunctionInterface> aClass = beansOfType.get(e).getClass();
                    Rule rule = aClass.getAnnotation(Rule.class);
                    //获取注解
                    Class cls = getParameterClass(aClass);
                    List<RuleFunctionDtoParameter> parameters = new ArrayList<>();
                    String typeName = null;
                    boolean annotationPresent = false;
                    if (ObjectNull.isNotNull(cls)) {
                        //获取处理类名
                        parameters = getList(cls);
                        typeName = cls.getTypeName();
                        annotationPresent = cls.isAnnotationPresent(Inspect.class);
                    }
                    String value = rule.value();
                    String explain = rule.explain();
                    if (ObjectNull.isNotNull(rule.help())) {
                        explain = explain + "<a  href=\"" + rule.help() + "\"  target=\"_blank\">更多描述</a>";
                    }
                    //有一个不空为,则不能跳过
                    boolean present = parameters.stream().filter(s -> ObjectNull.isNotNull(s.getOptionsType())).findFirst().isPresent();
                    return RuleFunctionDto.builder()
                            // 图标文件: /resources/icon
                            .icon(rule.iconUrl())
                            //设置参数实体类
                            .parameterClass(typeName)
                            .functionLabel(rule.label())
                            .functionName(value)
                            .parameters(parameters)
                            .skipRefresh(!present)
                            .demoDisabled(equals ? rule.demoDisabled() : false)
                            .test(rule.test())
                            .status(rule.enable())
                            .group(rule.group().name())
                            .returnType(rule.returnType())
                            .customStructure(rule.customStructure())
                            .order(rule.order())
                            //是否包含此自定义校验
                            .inspect(annotationPresent)
                            .explain(explain)
                            .build();
                }).collect(Collectors.toList());

        return functionPoList;
    }

    /**
     * 获取class类的泛型类型
     *
     * @param e class类
     * @return 泛型
     */
    public static Class getParameterClass(Class<?> e) {
        Type obj = e.getGenericInterfaces()[0];
        if (obj instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) obj;
            Class cls = (Class) parameterizedType.getActualTypeArguments()[0];
            return cls;
        } else {
            //表示无参方法
            return null;
        }
    }

    /**
     * 将实体类直接转换成业务参数
     *
     * @param cls 实体类对象
     * @author: guojing
     * @return: {@linkplain List< RuleFunctionDtoParameter >}
     */
    private static List<RuleFunctionDtoParameter> getList(Class cls) {
        Map<String, List<String>> linkFields = new HashMap<>(8);
        //获取根据link类型获取下级
        Map<String, ParameterValue> valueMap = checkField(cls, linkFields);
        return valueMap.keySet().stream().map(e -> {
                    ParameterValue ann = valueMap.get(e);
                    //根据注解类型获取是否支持公式
                    RuleFunctionDtoParameter functionParameter = new RuleFunctionDtoParameter()
                            .setInfo(ann.info())
                            .setSkipRefresh(true)
                            .setInputType(ann.type())
                            .setSubtype(ann.subtype())
                            .setExplain(ann.explain())
                            .setSupportFunction(ann.type().getExpression())
                            .setNecessity(ann.necessity())
                            .setExtension(Arrays.asList(ann.extension()))
                            .setKey(e);
                    //匹配选择类型,不能是关联类型，关联类型走的额外的数据接口
                    if (linkFields.containsKey(e)) {
                        //设置关联字段
                        functionParameter.setLinkField(linkFields.get(e));
                        functionParameter.setSelectedClass(toLowerCaseFirstOne(ann.cls().getSimpleName()));
                        functionParameter.setOptionsType(OptionsType.dynimic);
                        functionParameter.setSelectedClassLink(toLowerCaseFirstOne(ann.linkCls().getSimpleName()));
                        functionParameter.setSkipRefresh(false);
                        //如果是关联类型,则不能使用公式
                        functionParameter.setSupportFunction(false);
                    } else if (linkFields.values().stream().anyMatch(v -> v.contains(e))) {
                        //被关联字段 什么都不做
                        //如果是关联类型,则不能使用公式
                        functionParameter.setSupportFunction(false);
                    } else if (ann.type().toString().toLowerCase().endsWith(InputType.selected.name())) {
                        //关联类型没有默认值
                        functionParameter.setSelectedClass(toLowerCaseFirstOne(ann.cls().getSimpleName()));
                        functionParameter.setSupportFunction(true);
                        //如果是关联类型,则不能使用公式
                        //判断是否是数据库选择类型类型
                        try {
                            if (ObjectNull.isNull(dbSelected(functionParameter, ann.cls()))) {
                                //将空的排除调
                                return null;
                            }
                        } catch (Exception exception) {
                            log.error("加载字段{}失败,{},{}", cls, e, ann.info());
                            throw exception;
                        }
                        if (ObjectNull.isNotNull(ann.defaultValue())) {
                            functionParameter.setDefaultValue(ann.defaultValue());
                        }
                    } else {
                        Object s = ann.defaultValue();
                        if (ann.type().equals(InputType.onOff)) {
                            functionParameter.setDefaultValue(Boolean.valueOf(s.toString()));
                        } else {
                            functionParameter.setDefaultValue(s);
                        }
                    }
                    return functionParameter;
                })
                .filter(ObjectNull::isNotNull)
                .collect(Collectors.toList());
    }

    private static Object dbSelected(RuleFunctionDtoParameter functionParameter, Class<? extends ParameterSelected> cls) {
        //判断泛型是否是自定义类型
        Class parameterClass = SystemInit.getParameterClass(cls);
        if (ObjectNull.isNull(parameterClass)) {
            return null;
        }
        if (cls.getInterfaces()[0].equals(EnvironmentVariableSelected.class)) {
            StringBuffer jsonString = new StringBuffer();
            jsonString.append("<br/>");
            Arrays.stream(parameterClass.getFields())
                    .filter(e -> e.isAnnotationPresent(SelectOptionField.class))
                    .forEach(e -> {
                        SelectOptionField annotation = e.getAnnotation(SelectOptionField.class);
                        jsonString.append("key:" + e.getName() + "(" + annotation.value() + ")");
                        jsonString.append("<br/>");
                    });
            String str = functionParameter.getExplain() + "\n请在环境变量中添加键值对数据结构:" + jsonString;
            functionParameter.setExplain(str);
        }
        functionParameter.setOptionsType(OptionsType.dynimic);
        return parameterClass;
    }

    private static Map<String, ParameterValue> checkField(Class cls, Map<String, List<String>> linkFields) {
        Field[] fields = cls.getFields();
        //字段关联检查是否有连接字段
        Map<String, ParameterValue> fieldMap = new LinkedHashMap<>(fields.length);
        for (Field e : fields) {
            ParameterValue ann = e.getAnnotation(ParameterValue.class);
            Optional.ofNullable(ann).map(ParameterValue::info).orElseThrow(() -> new BusinessException("属性不完善", e.getName()));
            String[] objects = ann.linkFields();
            if (ObjectNull.isNotNull(objects)) {
                if (ObjectNull.isNotNull(ann.linkCls())) {
                    //获取集合
                    List<String> emFields = SpringContextUtil.getBean(ann.linkCls()).fields();
                    List<String> c = Arrays.asList(objects);
                    if (!new HashSet<>(emFields).containsAll(c)) {
                        log.error("没有找到逻辑解析的字段,{},{}", emFields, c);
                        throw new BusinessException("关联字段没有设置完成");
                    }
                    //设置关联字段
                    linkFields.put(e.getName(), new ArrayList<>(Arrays.asList(objects)));

                } else {
                    log.error("链接类型没有添加{}", e.getName());
                    throw new BusinessException("链接类型没有填写");
                }
            }
            fieldMap.put(e.getName(), ann);
        }
        return fieldMap;
    }

    /**
     * 首字母转小写
     *
     * @param s
     * @return
     */
    private static String toLowerCaseFirstOne(String s) {
        if (StringUtils.isBlank(s)) {
            return "";
        }
        String sub = "";
        if (s.length() > 1) {
            sub = s.substring(1);
        }
        return Character.toLowerCase(s.charAt(0)) + sub;
    }


    /**
     * 获取方法的入参参数处理
     * 时实获取所有方法中的变量值
     *
     * @param apply 方法参数值
     * @return: void
     */
    public static RuleFunctionDto getSelected(RuleFunctionDto dto, Function<String, ParameterSelected> apply) {
        RuleFunctionDto functionDto = BeanCopyUtil.copy(dto, RuleFunctionDto.class);
        for (RuleFunctionDtoParameter functionParameter : functionDto.getParameters()) {
            if (ObjectNull.isNull(functionParameter.getOptionsType())) {
                continue;
            }
            switch (functionParameter.getOptionsType()) {
                case dynimic:
                    try {
                        String selectedClass = functionParameter.getSelectedClass();
                        List list = SystemThreadLocal.get(selectedClass);
                        if (ObjectNull.isNotNull(list)) {
                            ParameterOption o = (ParameterOption) list.get(0);
                            functionParameter.setOptions(list);
                            Object defaultValue = functionParameter.getDefaultValue();
                            Optional<ParameterOption> first = list.stream().filter(e -> ((ParameterOption) e).getValue().toString().equals(defaultValue)).findFirst();
                            if (first.isPresent()) {
                                functionParameter.setDefaultValue(first.get().getValue());
                            } else {
                                functionParameter.setDefaultValue(o.getValue());
                            }
                            break;
                        }
                        //直接使用
                        ParameterSelected parameterSelected = apply.apply(selectedClass);
                        //超过1秒即失败
                        List options = parameterSelected.getOptions();
                        functionParameter.setOptions(options);
                        if (functionParameter.getOptions().isEmpty()) {
                            functionDto.setStatusMsg(functionParameter.getStatsMsg());
                        } else {
                            Object defaultValue = functionParameter.getDefaultValue();
                            Optional<ParameterOption> first = functionParameter.getOptions().stream().filter(e -> ((ParameterOption) e).getValue().toString().equals(defaultValue)).findFirst();
                            if (first.isPresent()) {
                                functionParameter.setDefaultValue(first.get().getValue());
                            } else {
                                ParameterOption o = (ParameterOption) functionParameter.getOptions().get(0);
                                functionParameter.setDefaultValue(o.getValue());
                            }
                        }
                        SystemThreadLocal.set(selectedClass, options);
                    } catch (BusinessException e) {
                        log.error("获取方法失败,{}", dto.getFunctionName(), e);
                        functionDto.setStatus(false);
                        functionDto.setStatusMsg(e.getMessage());
                    } catch (Exception e) {
                        log.error("获取方法失败,{}", dto.getFunctionName(), e);
                        functionDto.setStatus(false);
                        functionDto.setStatusMsg("功能异常不可使用");
                    }
                    break;
                case dboptions:
                    List<RuleOptionPo> ruleOptionPos = SpringContextUtil.getBean(RuleOptionDao.class).selectList(Wrappers.query(new RuleOptionPo().setField(functionParameter.getCustomOptionValue())));
                    List options = new ArrayList();
                    ruleOptionPos.forEach(s -> {
                        s.getMap().put(Get.name(RuleOptionPo::getName), s.getName());
                        options.add(s.getMap());
                    });
                    functionParameter.setOptions(options);
                    if (!options.isEmpty()) {
                        functionParameter.setDefaultValue(options.get(0));
                    }
                    break;
                case extension:
                    //扩展，暂时不编写
                default:
                    //没有类型不做操作
            }
        }
        return functionDto;
    }


}
