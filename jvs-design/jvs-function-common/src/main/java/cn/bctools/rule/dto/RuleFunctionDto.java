package cn.bctools.rule.dto;

import cn.bctools.rule.entity.enums.ClassType;
import cn.hutool.http.Method;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * @describe
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("方法对象")
public class RuleFunctionDto implements Serializable {

    private static final long serialVersionUID = -8313085259365677544L;
    @ApiModelProperty("方法id ，只有扩展类型才有")
    String functionId;
    @ApiModelProperty("方法名称")
    String functionName;
    @ApiModelProperty(value = "方法标签", notes = "用于前端在英文环境下数据显示的")
    String functionLabel;
    @ApiModelProperty("业务逻辑分组")
    String group;
    @ApiModelProperty("是否支持测试")
    Boolean test = false;
    @ApiModelProperty("是否支持自定义结构")
    Boolean customStructure = false;
    @ApiModelProperty("此方法的所有参数")
    List<RuleFunctionDtoParameter> parameters;
    @ApiModelProperty("返回选项值，key 为parameters 的Key")
    Map<String, List> selected;
    @ApiModelProperty("方法使用说明,直接由upms  label-value字典决定前端统一调用接口，此数据动态修改")
    String explain;
    @ApiModelProperty("状态")
    Boolean status;
    @ApiModelProperty("不可用原因")
    String statusMsg;
    @ApiModelProperty("是否跳过刷新动态参数")
    Boolean skipRefresh;
    @ApiModelProperty("处理类")
    @JsonIgnore
    String functionClass;
    @ApiModelProperty("方法的图片")
    String icon;
    @ApiModelProperty("演示禁用")
    boolean demoDisabled;
    @JsonIgnore
    @ApiModelProperty("参数实体类")
    String parameterClass;
    @ApiModelProperty("返回类型")
    ClassType returnType;
    @ApiModelProperty("后台使用排序")
    Integer order;
    @ApiModelProperty("是否自定义校验")
    Boolean inspect;
    @ApiModelProperty("用于自动注册服务名")
    Method method;
    @ApiModelProperty("是否允许修改")
    Boolean edit;

}
