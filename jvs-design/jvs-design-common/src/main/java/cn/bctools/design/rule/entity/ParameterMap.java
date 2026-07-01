package cn.bctools.design.rule.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import springfox.documentation.service.ParameterType;

import java.io.Serializable;

/**
 * @author jvs
 * The type Parameter map.
 */
@Data
@Accessors(chain = true)
public class ParameterMap implements Serializable {
    /**
     * The Key.
     */
    @ApiModelProperty("数据key")
    String key;
    /**
     * The Explain.
     */
    @ApiModelProperty("说明")
    String explain;
    /**
     * The Label.
     */
    @ApiModelProperty("字段名")
    String label;
    /**
     * The Disabled.
     */
    @ApiModelProperty("是否禁用")
    Boolean disabled;
    /**
     * The Necessity.
     */
    @ApiModelProperty("是否必填写")
    Boolean necessity;
    /**
     * The Cache.
     */
    @ApiModelProperty("是否是缓存")
    Boolean cache;
    /**
     * The Default value.
     */
    @ApiModelProperty("默认值")
    Object defaultValue;
    @ApiModelProperty("接口的测试值")
    private Object testValue;
    /**
     * The Encryption express.
     */
    @ApiModelProperty("正则脱敏")
    Object encryptionExpress;
    /**
     * The Input type.
     */
    @ApiModelProperty("组件输入类型")
    String inputType;
    /**
     * The Path.
     */
    @ApiModelProperty("路径")
    String path;
    /**
     * The Rule.
     */
    @ApiModelProperty("校验规则")
    String rule;
    /**
     * The Condition.
     */
    @ApiModelProperty("成功校验位信息")
    Object condition;
    /**
     * 请求参数的类型
     */
    ParameterType paramType;

}
