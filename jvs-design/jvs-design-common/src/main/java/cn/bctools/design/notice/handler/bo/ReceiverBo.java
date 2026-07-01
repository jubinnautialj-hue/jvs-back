package cn.bctools.design.notice.handler.bo;

import cn.bctools.design.notice.handler.enums.ReceiverTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 接收人员
 */
@Data
@Accessors(chain = true)
@ApiModel("接收人员")
public class ReceiverBo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "类型")
    private ReceiverTypeEnum type;
}
