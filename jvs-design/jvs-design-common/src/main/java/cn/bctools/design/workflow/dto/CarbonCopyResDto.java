package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.FlowTask;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("抄送给我列表响应")
public class CarbonCopyResDto extends FlowTask {

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime carbonCopyTime;

    @ApiModelProperty(value = "发起人头像")
    private String headImg;
}
