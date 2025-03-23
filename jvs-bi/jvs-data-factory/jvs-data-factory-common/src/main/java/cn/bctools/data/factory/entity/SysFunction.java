package cn.bctools.data.factory.entity;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 函数
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Data
@ApiModel("系统函数")
@Accessors(chain = true)
@TableName(value = "sys_function", autoResultMap = true)
public class SysFunction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("函数名称")
    private String name;
    @ApiModelProperty("函数入参")
    private String inParameter;
    @ApiModelProperty("函数说明")
    private String functionInfo;
    @ApiModelProperty("返回值类型")
    private DataFieldTypeEnum returnFieldType;
    @ApiModelProperty("函数类型")
    private String functionType;
    @ApiModelProperty("是否为doris自带的函数-此类函数不能修改")
    private Boolean isDorisFunction;
    @ApiModelProperty("doris返回值类型 例如DECIMAL(18,5)与returnNumber结合使用")
    private String dorisReturnType;
    @ApiModelProperty("java类路径")
    private String classPath;
    @ApiModelProperty("jar包url地址")
    private String jarUrl;
    @ApiModelProperty("jar包md5值")
    @TableField("jar_md5")
    private String jarMd5;
    @ApiModelProperty("返回值下标")
    @TableField("return_index")
    private Integer returnIndex;
    @ApiModelProperty("字段处理类路径")
    @TableField("field_class")
    private String fieldClass;
    @ApiModelProperty("是否为动态入参")
    @TableField("dynamic_in_parameter")
    private Boolean dynamicInParameter;
    @ApiModelProperty("长度-例如 varchar  datetime DECIMAL")
    private Integer length;
    @ApiModelProperty("精度-DECIMAL 类型")
    private Integer fieldPrecision;
    @ApiModelProperty("返回值是否为动态-根据入参类型返回")
    private Boolean returnDynamic;
    @ApiModelProperty("示例用于前端渲染")
    private String examples;
    @ApiModelProperty("是否为用户手动选择的类型")
    private Boolean userSelectType;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ApiModelProperty("是否可见-部分函数 不需要在 字段拓展展示")
    private DataFieldTypeClassifyEnum returnDataFieldTypeClassify;

}
