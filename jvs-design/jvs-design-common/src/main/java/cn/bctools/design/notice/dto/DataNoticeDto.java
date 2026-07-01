package cn.bctools.design.notice.dto;

import cn.bctools.design.notice.handler.bo.ReceiverBo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhuxiaokang
 * 消息通知
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("消息通知")
public class DataNoticeDto extends NoticeDto{
    @ApiModelProperty(value = "数据模型id", required = true)
    @NotBlank(message = "模型id不能为空")
    private String modelId;

    @ApiModelProperty(value = "接收者", required = true)
    @NotNull(message = "请选择人员")
    private List<ReceiverBo> receiver;
}
