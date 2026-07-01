package cn.bctools.design.notice.handler.bo.trigger;

import cn.bctools.design.notice.handler.enums.TriggerEditedConditionTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 编辑成功触发通知条件
 */
@Data
@Accessors(chain = true)
@ApiModel("编辑成功触发通知条件")
public class EditedCondition {

    @ApiModelProperty(value = "编辑成功-触发条件类型")
    private TriggerEditedConditionTypeEnum type;

    @ApiModelProperty(value = "指定字段集合")
    private List<String> fieldKeys;
}
