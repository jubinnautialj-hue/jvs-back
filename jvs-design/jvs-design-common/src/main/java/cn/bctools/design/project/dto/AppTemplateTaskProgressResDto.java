package cn.bctools.design.project.dto;

import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author hrl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("模板创建应用或迭代应用进度")
public class AppTemplateTaskProgressResDto extends JvsAppTemplateTaskProgress {
    @ApiModelProperty("进度百分比")
    private Long ratio;
}
