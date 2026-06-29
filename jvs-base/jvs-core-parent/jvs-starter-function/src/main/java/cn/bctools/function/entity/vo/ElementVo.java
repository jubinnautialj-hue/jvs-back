package cn.bctools.function.entity.vo;

import cn.bctools.function.enums.JvsParamType;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 系统函数实体类
 * <p>
 * 1. 参数: ${参数名}
 * 2. 函数: 函数名()
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
@ApiModel("系统函数实体类")
public class ElementVo {

    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("原始路径")
    private String path;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("短名,用于参数下拉显示的短信")
    private String shortName;

    /**
     * 该值不为空时, 返回的参数,函数列表会按分类进行分组
     */
    @ApiModelProperty("分类")
    private String type;

    @ApiModelProperty("详细描述")
    private String info;

    /**
     * 该字段的值由实现的接口决定
     * <p>
     * 1. true {@link cn.bctools.function.handler.IJvsParam}
     * 2. false {@link cn.bctools.function.handler.IJvsFunction}
     */
    @ApiModelProperty("是否为参数(不使用括号)")
    private boolean isParam;

    @ApiModelProperty("是否允许缓存(默认false)")
    private boolean enableCache = false;
    @ApiModelProperty("是否存在可变参数")
    private Boolean dynamicParam;
    @ApiModelProperty("入参个数")
    private int reqCont;

    @ApiModelProperty("返回值类型")
    private JvsParamType jvsParamType;

    @ApiModelProperty("入参类型")
    private List<JvsParamType> inParamTypes;

    @ApiModelProperty("字段类型")
    private String fieldType;

    @ApiModelProperty("子类数据")
    List<ElementVo> children;

    @ApiModelProperty("扩展数据")
    private JSONObject other = new JSONObject();
}
