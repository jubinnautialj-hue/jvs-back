package cn.bctools.report.model;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ApiModel("多数据源关联条件")
public class JoinSetting implements Serializable {

    private static final long serialVersionUID = -4720335800309120929L;

    @ApiModelProperty("主表")
    private String leftExecuteName;

    @ApiModelProperty(value = "主表字段",notes = "主表名称.主表字段,例子：school.className")
    private String leftColumn;

    @ApiModelProperty("子表")
    private String rightExecuteName;

    @ApiModelProperty(value = "子表字段",notes = "子表名称.子表字段,例子：student.name")
    private String rightColumn;

    public boolean isAllNotBlank(){
        return StrUtil.isAllNotBlank(this.leftExecuteName, this.leftColumn, this.rightExecuteName, this.rightColumn);
    }
}
