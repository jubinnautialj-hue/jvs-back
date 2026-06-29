package cn.bctools.document.receiver.dto;

import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "mq队列的队列消息体")
public class DocumentMqDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "文档id")
    private String documentId;
    @ApiModelProperty(value = "内容部分文档上传时就已经解析了减少二次解析")
    private String content;
    @ApiModelProperty(value = "租户Id")
    private String tenantId;
    @ApiModelProperty(value = "当前uuid")
    private String uuid;
    @ApiModelProperty(value = "操作用户信息")
    private UserDto userDto;
    @ApiModelProperty(value = "当前语言")
    private Locale locale;
}
