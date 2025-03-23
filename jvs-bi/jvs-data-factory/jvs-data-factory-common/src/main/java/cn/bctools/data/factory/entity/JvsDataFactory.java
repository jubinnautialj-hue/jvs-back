package cn.bctools.data.factory.entity;

import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.handler.ListTypeInParameterDtoHandler;
import cn.bctools.data.factory.handler.TaskCronHandler;
import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.source.dto.InParameterDto;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 数据etl
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Data
@ApiModel("数据etl")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "jvs_data_factory", autoResultMap = true)
public class JvsDataFactory extends RolePo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    /**
     * 设计
     */
    @ApiModelProperty("设计")
    @TableField("name")
    private String name;

    /**
     * 优先级
     */
    @ApiModelProperty("优先级")
    @TableField("priority")
    private Integer priority;

    /**
     * 渲染json
     */
    @ApiModelProperty("渲染json")
    private String viewJson;

    /**
     * 表单描述
     */
    @ApiModelProperty("描述")
    private String description;
    /**
     * 租户id
     */
    private String tenantId;
    @ApiModelProperty("是否启用")
    private Boolean enable;

    @ApiModelProperty("任务类型")
    private TaskTypeEnum taskType;

    @ApiModelProperty("定时任务逻辑")
    @TableField(typeHandler = TaskCronHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private TaskCronDto task;

    /**
     * 逻辑删除
     */
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;

    @ApiModelProperty("前置任务的数据集id")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> prefixTaskId;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    @ApiModelProperty("后置任务的数据集id")
    private List<String> rearTaskId;

    @ApiModelProperty("数据源的名称")
    private String documentName;

    @ApiModelProperty("是否完整")
    private Boolean integrityIs;
    @ApiModelProperty("列权限 人员不满足时其他成员是否可见")
    private Boolean columnOtherUserVisible;
    @ApiModelProperty("行权限 人员不满足时其他成员是否可见")
    private Boolean rowOtherUserVisible;
    @ApiModelProperty("入参")
    @TableField(typeHandler = ListTypeInParameterDtoHandler.class)
    private List<InParameterDto> inParameterPo;

    @ApiModelProperty("最近一次运行时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateRunTime;

    @TableField(exist = false)
    @ApiModelProperty("源id用于导入的时候使用")
    private String sourceId;

    @ApiModelProperty("类型")
    @TableField("type")
    @NotBlank(message = "未选择类型")
    private String type;

    @ApiModelProperty("排序")
    @TableField("sort")
    private int sort;

    @ApiModelProperty("删除的输入节点用于删除历史数据")
    @TableField(exist = false)
    private List<NodeHtml> deleteNode;


}
