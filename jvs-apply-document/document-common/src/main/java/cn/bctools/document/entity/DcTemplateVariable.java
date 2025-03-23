package cn.bctools.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 文档模板
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文档模板")
@EqualsAndHashCode(callSuper = false)
@TableName(value = "dc_template_variable")
public class DcTemplateVariable implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("变量名称")
    private String name;
    @ApiModelProperty("其他值 其他客户定制开发可能需要的")
    private String otherData;
    @ApiModelProperty("模板id")
    private String templateId;
    @ApiModelProperty("变量备注或说明")
    private String remark;
}
