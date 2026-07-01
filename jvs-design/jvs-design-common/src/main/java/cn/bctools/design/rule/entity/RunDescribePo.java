package cn.bctools.design.rule.entity;

import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author bctools.cn
 * @describe 逻辑某些节点的详细描述
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "jvs_rule_describe", autoResultMap = true)
public class RunDescribePo extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("节点的备注")
    private String remark;

    @ApiModelProperty("逻辑的 key")
    private String reqRunKey;

    @ApiModelProperty("节点的id")
    private String nodeId;

    @ApiModelProperty("应用的 id")
    private String jvsAppId;

    @ApiModelProperty("租户")
    private String tenantId;

}
