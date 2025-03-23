package cn.bctools.data.factory.notice.po;

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

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("消息变量")
@EqualsAndHashCode(callSuper = false)
@TableName(value = "message_variable")
public class MessageVariable implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("变量中文名称")
    @TableField("variable_explain")
    private String variableExplain;
    @ApiModelProperty("变量名称")
    @TableField("variable_name")
    private String variableName;
    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

}
