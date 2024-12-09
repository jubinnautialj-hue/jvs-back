package cn.bctools.function.entity.po;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.database.handler.U0000TypeHandler;
import cn.bctools.function.enums.CheckTypeEnums;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表达式
 *
 * @author guojing
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("业务表达式")
@TableName(value = "jvs_function_business", autoResultMap = true)
public class FunctionBusinessPo extends BasalPo {

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty("应用的ID")
    private String jvsAppId;

    @ApiModelProperty("设计ID")
    private String designId;

    @ApiModelProperty("业务ID(参数标识)")
    private String businessId;

    @ApiModelProperty("表达式使用场景")
    private String useCase;

    @ApiModelProperty("函数执行语句")
    @TableField(value = "body", updateStrategy = FieldStrategy.IGNORED, typeHandler = U0000TypeHandler.class)
    private String body;

    @ApiModelProperty("组件类型")
    private String type;
    @ApiModelProperty("公式描述信息")
    private String description;

    @ApiModelProperty("校验")
    private CheckTypeEnums checkType;

    @ApiModelProperty("上级组件类型")
    private String parentType;

    @ApiModelProperty("表达式关联的参数标识")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<String> relatedIds;

    @ApiModelProperty("轻应用版本号")
    private String appVersion;

}
