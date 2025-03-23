package cn.bctools.rule.dto;

import cn.bctools.rule.cons.CEEnum;
import cn.bctools.rule.entity.enums.InputType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import springfox.documentation.service.ParameterType;

import java.io.Serializable;
import java.util.List;

/**
 * @author guojing
 * @describe 这个方法的入参解释
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RuleFunctionDtoParameter implements Serializable {
    private static final long serialVersionUID = 6616178581524444797L;
    /**
     * 是否是必填写
     */
    @ApiModelProperty("是否是必须填写")
    private boolean necessity;

    @ApiModelProperty("是否支持函数")
    private Boolean supportFunction;
    /**
     * 输入框类型
     */
    @ApiModelProperty("输入框类型")
    private InputType inputType;
    private InputType subtype;
    /**
     * 额外的参数解释说明
     */
    @ApiModelProperty("字段解释")
    private String explain;

    @ApiModelProperty("选择项配置类型，默认为空")
    private OptionsType optionsType;
    /**
     * 数据解释
     */
    @ApiModelProperty("数据解释")
    private String info;
    @ApiModelProperty("服务状态不可用描述")
    private String statsMsg = "没有可选择数据";
    /**
     * 变量的名
     */
    @ApiModelProperty("变量的名称")
    private String key;

    @ApiModelProperty("关联的字段")
    private List<String> linkField;
    @ApiModelProperty("组件扩展属性")
    private List<CEEnum> extension;
    @ApiModelProperty("处理的类型")
    private String selectedClass;
    @ApiModelProperty("关联类型处理")
    private String selectedClassLink;
    @ApiModelProperty("是否跳过刷新动态参数")
    Boolean skipRefresh;
    /**
     * 选项的值
     */
    @ApiModelProperty("选项的值")
    private List options;

    @ApiModelProperty("自定义标识")
    private String customOptionValue;
    /**
     * 是否自定义配置
     */
    @ApiModelProperty("是否自定义配置，如果有，则显示标识")
    private Object customOption;
    /**
     * 默认的值
     */
    private Object defaultValue;
    @ApiModelProperty("接口的测试值")
    private Object testValue;
    /**
     * 请求参数的类型
     */
    private ParameterType paramType;

    @ApiModelProperty("是否是缓存")
    Boolean cache = false;
}
