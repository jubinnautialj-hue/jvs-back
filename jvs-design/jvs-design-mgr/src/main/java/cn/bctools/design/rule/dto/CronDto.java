package cn.bctools.design.rule.dto;

import cn.bctools.design.rule.component.CronEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class CronDto {

    @ApiModelProperty("cron表达式")
    private CronEnum cron;

    @ApiModelProperty("名称")
    private String name;

}
