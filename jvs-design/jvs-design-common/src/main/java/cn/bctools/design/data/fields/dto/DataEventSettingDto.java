package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.entity.DataEventPo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 数据模型设置
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
public class DataEventSettingDto implements Serializable {

    @ApiModelProperty("事件列表")
    private List<DataEventPo> eventList;

}
