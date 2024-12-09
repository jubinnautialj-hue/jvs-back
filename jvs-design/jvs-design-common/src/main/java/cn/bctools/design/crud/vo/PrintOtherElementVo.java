package cn.bctools.design.crud.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 表单打印模板设计返回的其它字段
 */
@Data
@ApiModel("表单打印模板设计返回的其它字段")
public class PrintOtherElementVo {

    @ApiModelProperty(value = "数据来源字段", notes = "前端根据这个字段确定打印时用的接口")
    private String dataSourceType;
}
