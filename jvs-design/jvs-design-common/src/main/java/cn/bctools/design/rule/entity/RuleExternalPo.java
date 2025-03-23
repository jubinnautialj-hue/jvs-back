package cn.bctools.design.rule.entity;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.rule.entity.handler.ParameterMapTypeHandler;
import cn.bctools.rule.dto.RuleFunctionDtoParameter;
import cn.bctools.rule.entity.enums.InputType;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import springfox.documentation.service.ParameterType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guojing
 * 逻辑引擎分组扩展
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "jvs_rule_external", autoResultMap = true)
public class RuleExternalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("url地址")
    private String url;
    @ApiModelProperty("web-services方法的原始名称")
    private String functionName;
    @ApiModelProperty("serverUr地址")
    private String serverUrl;
    @ApiModelProperty("方法的图片")
    private String icon;
    private String explainInfo;
    @ApiModelProperty("状态是否可用")
    private Boolean status;
    @ApiModelProperty("分组")
    private String ruleGroup;
    @ApiModelProperty("方法名不能重复")
    private String name;
    @ApiModelProperty("请求方法")
    @TableField("method_type")
    private Method method;
    @ApiModelProperty("自定义解析处理类")
    private String handler;
    @ApiModelProperty("类型")
    private String type = "http";
    @ApiModelProperty("缓存的key")
    private String cacheKey;
    @ApiModelProperty("缓存的时间分钟")
    private Long cacheTime;
    @ApiModelProperty("同步删除的缓存 key,的前缀")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<String> deleteCaches;
    @ApiModelProperty("mock")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map mock;
    @ApiModelProperty("启用 mock")
    private Boolean enableMock;
    @ApiModelProperty("字典数据")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List fieldList;
    @ApiModelProperty("路径数据")
    @TableField(typeHandler = ParameterMapTypeHandler.class)
    private List<ParameterMap> pathList;
    @ApiModelProperty("请示参数")
    @TableField(typeHandler = ParameterMapTypeHandler.class)
    private List<ParameterMap> queryList;
    @ApiModelProperty("请求头")
    @TableField(typeHandler = ParameterMapTypeHandler.class)
    private List<ParameterMap> headerList;
    @ApiModelProperty("响应参数")
    @TableField(typeHandler = ParameterMapTypeHandler.class)
    private List<ParameterMap> responseList;
    @ApiModelProperty("响应示例")
    private String response;
    @TableField(exist = false)
    @ApiModelProperty("响应示例")
    private String body;

    public List<RuleFunctionDtoParameter> getFieldLists() {
        if (ObjectNull.isNull(fieldList)) {
            fieldList = new ArrayList<>();
        }

        List<RuleFunctionDtoParameter> collect = (List<RuleFunctionDtoParameter>) fieldList.stream().map(e -> {
                    Map<String, Object> a = JSONObject.parseObject(JSONObject.toJSONString(e));
                    RuleFunctionDtoParameter s = BeanCopyUtil.copy(e, RuleFunctionDtoParameter.class);
                    if (a.get("inputType").equals("boolean")) {
                        s.setInputType(InputType.onOff);
                    }
                    Boolean cache = a.get("cache") == null ? false : Boolean.parseBoolean(a.toString());
                    Object orDefault = a.getOrDefault("paramType", ParameterType.BODY.name());
                    ParameterType parameterType = ParameterType.valueOf(orDefault.toString());
                    s.setSupportFunction(true)
                            .setParamType(parameterType)
                            .setInfo(s.getExplain()).setCache(cache);
                    return s;
                })
                .collect(Collectors.toList());
        //将路径处理和请求，和body进行处理并添加到 fieldList里面
        if (ObjectNull.isNotNull(pathList)) {
            pathList.stream().map(e ->
                            new RuleFunctionDtoParameter()
                                    .setInfo(e.getLabel())
                                    .setKey(e.getKey())
                                    .setTestValue(e.getTestValue())
                                    .setParamType(ParameterType.PATH)
                                    .setExplain(e.getExplain())
                                    .setSupportFunction(true)
                                    .setInputType(InputType.input)
                                    .setCache(Optional.ofNullable(e.getCache()).orElse(Boolean.FALSE))
                                    .setNecessity(Optional.ofNullable(e.getNecessity()).orElse(false)))
                    .forEach(collect::add);
        }

        if (ObjectNull.isNotNull(headerList)) {
            headerList.stream().map(e -> new RuleFunctionDtoParameter()
                            .setInfo(e.getLabel())
                            .setKey(e.getKey())
                            .setExplain(e.getExplain())
                            .setTestValue(e.getTestValue())
                            .setParamType(ParameterType.HEADER)
                            .setSupportFunction(true)
                            .setInputType(InputType.input)
                            .setCache(Optional.ofNullable(e.getCache()).orElse(Boolean.FALSE))
                            .setNecessity(Optional.ofNullable(e.getNecessity()).orElse(false))
                    )
                    .forEach(collect::add);
        }
        if (ObjectNull.isNotNull(queryList)) {
            queryList.stream().map(e -> new RuleFunctionDtoParameter()
                            .setInfo(e.getLabel())
                            .setKey(e.getKey())
                            .setExplain(e.getExplain())
                            .setSupportFunction(true)
                            .setTestValue(e.getTestValue())
                            .setParamType(ParameterType.QUERY)
                            .setInputType(InputType.input)
                            .setCache(Optional.ofNullable(e.getCache()).orElse(Boolean.FALSE))
                            .setNecessity(Optional.ofNullable(e.getNecessity()).orElse(false))
                    )
                    .forEach(collect::add);
        }
        return collect;
    }
}
