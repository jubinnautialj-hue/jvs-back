package cn.bctools.document.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("权限与角色的绑定关系")
@EqualsAndHashCode(callSuper = false)
@TableName("message_variable_bin_ding")
public class MessageVariableBinDing implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("变量id")
    @TableField("variable_id")
    private String variableId;
    @ApiModelProperty("变量名称")
    @TableField("variable_name")
    private String variableName;
    @ApiModelProperty("变量中文名称")
    @TableField("variable_explain")
    private String variableExplain;
    @ApiModelProperty("消息id")
    @TableField("message_id")
    private String messageId;
    @ApiModelProperty("颜色代码")
    @TableField("colour")
    private String colour;
    @ApiModelProperty("消息变量名称")
    @TableField("message_variable_name")
    private String messageVariableName;
}
