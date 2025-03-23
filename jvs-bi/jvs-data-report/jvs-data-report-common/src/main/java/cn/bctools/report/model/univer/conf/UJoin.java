package cn.bctools.report.model.univer.conf;

import cn.bctools.report.model.JoinSetting;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class UJoin implements Serializable {

    private static final long serialVersionUID = 5139030647203290882L;

    @ApiModelProperty("主表")
    private String mainExecuteName;

    @ApiModelProperty("主表解释")
    private String mainExecuteNameInfo;

    @ApiModelProperty("表关联条件")
    private List<JoinSetting> joinSettings;
}
