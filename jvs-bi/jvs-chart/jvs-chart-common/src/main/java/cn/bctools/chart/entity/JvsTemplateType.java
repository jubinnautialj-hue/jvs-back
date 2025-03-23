package cn.bctools.chart.entity;

import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 模板分类
 *
 * @author zqs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "jvs_template_type", autoResultMap = true)
@ApiModel
public class JvsTemplateType extends BasalPo implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;
    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("简介或者备注")
    private String remark;
    @ApiModelProperty("此分类的条数")
    @TableField(exist = false)
    private Long count;
}
