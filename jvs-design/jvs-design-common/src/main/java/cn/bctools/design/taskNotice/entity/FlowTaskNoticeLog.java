package cn.bctools.design.taskNotice.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.taskNotice.dto.FlowNoticeResponseDto;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wayne
 * 待办提醒发送日志
 */
@Data
@Accessors(chain = true)
@ApiModel("待办提醒发送日志")
@TableName(value = "jvs_flow_task_notice_log", autoResultMap = true)
public class FlowTaskNoticeLog extends BasalPo {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "类型")
    @TableField("type")
    private String type;

    @ApiModelProperty(value = "业务实例ID")
    @TableField("instance_id")
    private String instanceId;

    @ApiModelProperty(value = "工作流节点id")
    @TableField("node_id")
    private String nodeId;

    //用不到
    @ApiModelProperty(value = "单据ID")
    @TableField("task_id")
    private String taskId;

    @ApiModelProperty(value = "请求接口地址")
    @TableField("api_url")
    private String apiUrl;

    @ApiModelProperty(value = "请求入参")
    @TableField(value = "request_data", typeHandler = Fastjson2TypeHandler.class)
    private List requestData;

    @ApiModelProperty(value = "返回结果")
    @TableField(value = "response_data", typeHandler = Fastjson2TypeHandler.class)
    private FlowNoticeResponseDto responseData;

    @ApiModelProperty(value = "应用id")
    @TableField(value = "jvs_app_id")
    private String jvsAppId;

    @ApiModelProperty(value = "是否删除 0未删除  1已删除")
    @TableField("del_flag")
    @TableLogic
    private Boolean delFlag;
}
