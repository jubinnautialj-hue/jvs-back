package cn.bctools.design.identification.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.rule.entity.RuleType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 标识
 * <p>
 * 自定义设计标识符
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "jvs_identification")
public class Identification extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("主键id")
    private String id;

    @ApiModelProperty("标识符")
    @TableField("identifier")
    private String identifier;

    @ApiModelProperty("设计id")
    @TableField("design_id")
    private String designId;

    @ApiModelProperty("设计名称")
    @TableField("design_name")
    private String designName;

    @ApiModelProperty("设计类型")
    @TableField("design_type")
    private DesignType designType;

    @TableField(exist = false)
    @ApiModelProperty("逻辑类型")
    private RuleType reqType;

    @ApiModelProperty("应用")
    @TableField("jvs_app_id")
    private String jvsAppId;

    @ApiModelProperty("租户")
    private String tenantId;
}
