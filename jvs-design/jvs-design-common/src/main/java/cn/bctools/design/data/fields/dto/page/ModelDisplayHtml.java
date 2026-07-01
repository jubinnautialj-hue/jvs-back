package cn.bctools.design.data.fields.dto.page;

import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.dto.QueryConditionExtendDto;
import cn.bctools.design.data.fields.enums.DisplayShowModelTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author jvs
 * 显示方式-配置显示关联模型
 */
@Data
public class ModelDisplayHtml {
    @ApiModelProperty("数据联动模型id")
    private String dataLinkageModelId;
    @ApiModelProperty("开启数据联动条件")
    private Boolean dataLinkageEnable;
    @ApiModelProperty("数据联动条件")
    private List<QueryConditionDto> dataLinkageList;
    @ApiModelProperty("显示字段")
    private List<ModelDisplayLinkageFieldHtml> linkageFieldKeys;
    @ApiModelProperty("关联显示方式")
    private DisplayShowModelTypeEnum showModelType;
}
