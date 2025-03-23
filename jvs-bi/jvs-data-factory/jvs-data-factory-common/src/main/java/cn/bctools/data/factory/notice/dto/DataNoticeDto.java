package cn.bctools.data.factory.notice.dto;

import cn.bctools.data.factory.notice.bo.ReceiverBo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知
 */
@Data
@Accessors(chain = true)
@ApiModel("消息通知")
public class DataNoticeDto extends NoticeDto {
    @ApiModelProperty(value = "数据集", required = true)
    @NotNull(message = "请选择数据集")
    private String dataFactoryId;

    @ApiModelProperty(value = "接收者", required = true)
    @NotNull(message = "请选择人员")
    private List<ReceiverBo> receiver;
}
