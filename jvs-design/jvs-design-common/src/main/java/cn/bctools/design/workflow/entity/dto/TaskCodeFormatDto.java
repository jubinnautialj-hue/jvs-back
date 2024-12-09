package cn.bctools.design.workflow.entity.dto;

import cn.bctools.design.common.OrderFormat;
import cn.bctools.design.workflow.entity.enums.TaskCodeFormatTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hs
 */
@Data
@ApiModel("流程任务编号格式")
public class TaskCodeFormatDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * true-自定义编号格式，false-系统默认
     */
    private Boolean custom = Boolean.FALSE;

    /**
     * 格式类型
     */
    private TaskCodeFormatTypeEnum formatType;

    /**
     * 格式类型为自动计算的编号格式配置
     */
    private OrderFormat autoFormat;
}
