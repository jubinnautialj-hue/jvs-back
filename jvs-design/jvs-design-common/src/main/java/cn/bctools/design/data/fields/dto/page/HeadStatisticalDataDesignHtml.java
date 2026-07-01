package cn.bctools.design.data.fields.dto.page;

import cn.bctools.design.data.fields.dto.RuleDesignHtml;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <头部自定义统计数据设计>
 *
 * @author auto
 **/
@Data
@Accessors(chain = true)
public class HeadStatisticalDataDesignHtml {
    @ApiModelProperty("是否开启")
    private Boolean enable;
    @ApiModelProperty("统计名称")
    private String name;
    @ApiModelProperty("统计标识")
    private String statisticsCode;
    @ApiModelProperty(value = "备注", notes = "设计阶段和开发阶段的沟通记录")
    private String statisticalRemark;
    @ApiModelProperty("逻辑引擎")
    private RuleDesignHtml rule;

}
