package cn.bctools.design.common;

import cn.bctools.design.data.entity.DataIdPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

/**
 * 流水号-重置规则
 *
 * @Author: GuoZi
 */
@Getter
@AllArgsConstructor
@ApiModel("流水号-重置规则")
public enum OrderResetRuleEnum {

    /**
     * 重置规则
     */
    n("不重置", DataIdPo::getCurrentId),
    y("按年重置", DataIdPo::getCurrentYearId),
    m("按月重置", DataIdPo::getCurrentMonthId),
    d("按天重置", DataIdPo::getCurrentDayId),
    h("按小时重置", DataIdPo::getCurrentHourId);

    @ApiModelProperty("描述")
    private final String desc;
    @ApiModelProperty("id获取")
    private final Function<DataIdPo, Integer> idGetter;

}
