package cn.bctools.data.factory.dto;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskTypeEnum;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author xiaohui
 */
@Data
@ApiModel("数据etl")
@Accessors(chain = true)
public class DataFactoryTaskMqDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("需要执行的数据id")
    private String dataFactoryId;
    @ApiModelProperty("数据集队列id")
    private String dataFactoryQueueId;
    @ApiModelProperty("前置任务后置任务的任务本身")
    private String taskItselfId;
    @ApiModelProperty("批次id")
    private String batchId;
    @ApiModelProperty("用户信息")
    private UserDto userDto;
    @ApiModelProperty("用户设计的json")
    private String executionGraph;
    @ApiModelProperty("级别")
    private Integer priority;
    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("更新方式 auto 自动 manual 手动")
    private OperateMethodEnum operateMethod;
    @ApiModelProperty("任务类型")
    private QueueTaskTypeEnum queueTaskType;
}
