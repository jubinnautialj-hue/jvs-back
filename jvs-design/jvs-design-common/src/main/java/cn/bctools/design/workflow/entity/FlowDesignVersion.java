package cn.bctools.design.workflow.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("工作流设计版本")
@TableName(value = "jvs_flow_design_version", autoResultMap = true)
public class FlowDesignVersion extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "工作流id")
    @TableField("flow_design_id")
    private String flowDesignId;

    @ApiModelProperty(value = "版本状态")
    @TableField("version_status")
    private FlowDesignVersionStatusEnum versionStatus;

    @ApiModelProperty(value = "版本号")
    @TableField("design_version")
    private Integer designVersion;

    @ApiModelProperty("工作流流程设计JSON")
    @TableField("design_body")
    private String designBody;

    @ApiModelProperty("应用ID")
    private String jvsAppId;
}
