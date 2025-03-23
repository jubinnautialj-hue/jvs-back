package cn.bctools.report.model.univer.conf;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("查询条件-字段")
public class USearchField implements Serializable {


    private static final long serialVersionUID = -1568851931227173238L;
    @ApiModelProperty("是否开启查询")
    private Boolean openQuery;

    @ApiModelProperty("字段key")
    private String fieldKey;

    @ApiModelProperty("字段名称")
    private String fieldName;

    @ApiModelProperty("字段类型")
    private DataFieldTypeClassifyEnum dataFieldTypeClassify;

    @ApiModelProperty("字段名称")
    private String id;

    @ApiModelProperty("查询模式")
    private EQueryType queryType;

    @ApiModelProperty("查询模式")
    private String quickType;

    @ApiModelProperty("查询默认值")
    private Object queryDefaultValue;

    @ApiModelProperty(value = "查询值",notes = "用户输入或选择的值")
    private Object queryValue;

    @ApiModelProperty("格式化")
    private String format;

    @ApiModelProperty("下拉框数据")
    private List<SelectData> querySelectData;

    @ApiModelProperty(value = "是否显示",notes = "引入数据集时，被勾选的字段才能被使用")
    private Boolean isShow;

    @ApiModel("查询模式")
    public enum EQueryType{
        select,
        input,
        date,
        dateRange,
        quickDate,
        selectMultiple
    }

    @Data
    @Accessors(chain = true)
    @ApiModel("下拉选择数据")
    public static class SelectData implements Serializable {

        private static final long serialVersionUID = -1005531302729924205L;

        private String label;

        private String value;
    }
}
