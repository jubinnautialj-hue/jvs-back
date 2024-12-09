package cn.bctools.design.workflow.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.workflow.model.enums.PurviewGroupEnum;
import cn.bctools.design.workflow.model.enums.PurviewPersonTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流权限
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("工作流权限")
@TableName(value = "jvs_flow_purview", autoResultMap = true)
public class FlowPurview implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "工作流设计id")
    @TableField("flow_design_id")
    private String flowDesignId;

    @ApiModelProperty(value = "权限组")
    @TableField("purview_group")
    private PurviewGroupEnum purviewGroup;

    @ApiModelProperty(value = "成员类型")
    @TableField("person_type")
    private PurviewPersonTypeEnum personType;

    @ApiModelProperty(value = "权限用户id集合")
    @TableField(value = "users", typeHandler = Fastjson2TypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private List<String> users;

    @ApiModelProperty(value = "权限部门id集合")
    @TableField(value = "depts", typeHandler = Fastjson2TypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private List<String> depts;

    @ApiModelProperty(value = "权限角色id集合")
    @TableField(value = "roles", typeHandler = Fastjson2TypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private List<String> roles;

    @ApiModelProperty(value = "权限岗位id集合")
    @TableField(value = "jobs", typeHandler = Fastjson2TypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private List<String> jobs;

    @ApiModelProperty(value = "应用id")
    @TableField("jvs_app_id")
    private String jvsAppId;
}
