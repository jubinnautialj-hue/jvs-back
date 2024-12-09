package cn.bctools.function.entity.po;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.function.entity.vo.FunctionBusinessTestVo;
import cn.bctools.function.entity.vo.Parameter;
import cn.bctools.function.enums.JvsParamType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 基础函数
 *
 * @author xh
 */
@Data
@Accessors(chain = true)
@TableName(value = "jvs_function_base", autoResultMap = true)
@ApiModel("基础函数")
public class BaseFunctionPo implements Serializable {

    private static final long serialVersionUID = -5438750212078010246L;

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("分类")
    private String type;

    @ApiModelProperty("描述")
    private String info;
    @ApiModelProperty("短的描述")
    private String shortName;

    @ApiModelProperty("参数数量(不包括可变参数)")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Set<Integer> paramCount;
    @ApiModelProperty("参数数量(不包括可变参数)")
    @TableField(typeHandler = ListParameterTypeHandler.class)
    List<Parameter> parameters;
    @ApiModelProperty("是否存在可变参数")
    private Boolean dynamicParam;

    @ApiModelProperty("是否允许缓存")
    private Boolean enableCache;

    @ApiModelProperty("函数脚本")
    private String body;

    @ApiModelProperty("返回值类型")
    private JvsParamType jvsParamType;

    @TableField(typeHandler = Fastjson2TypeHandler.class)
    @ApiModelProperty("入参类型")
    private List<JvsParamType> inParamTypes;

}
