package cn.bctools.design.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流水号-时间标识
 *
 * @Author: GuoZi
 */
@Getter
@AllArgsConstructor
@ApiModel("流水号-时间标识")
public enum OrderTimeMarkEnum {

    /**
     * 时间标识
     */
    n("不添加时间标识", ""),
    y("年", "yyyy"),
    ym("年月", "yyyyMM"),
    ymd("年月日", "yyyyMMdd"),
    ymdh("年月日时", "yyyyMMddHH"),
    ymdhm("年月日时分", "yyyyMMddHHmm"),
    ymdhms("年月日时分秒", "yyyyMMddHHmmss");

    @ApiModelProperty("描述")
    private final String desc;
    @ApiModelProperty("时间格式")
    private final String format;

}
