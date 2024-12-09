package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.fields.enums.DataEventType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 回调信息
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@Accessors(chain = true)
public class DataCallbackDto {

    @ApiModelProperty("数据id")
    private String dataId;

    @ApiModelProperty("数据内容")
    private Map<String, Object> data;

    @ApiModelProperty("事件名称")
    private DataEventType eventType;

}
