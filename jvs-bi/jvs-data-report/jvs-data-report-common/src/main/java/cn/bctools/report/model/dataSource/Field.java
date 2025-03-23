package cn.bctools.report.model.dataSource;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import com.sun.jna.Structure;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("数据源-字段")
public class Field {

    @ApiModelProperty("字段类型分类")
    private DataFieldTypeClassifyEnum dataFieldTypeClassify;

    @ApiModelProperty("dataId")
    private String dataId;

    @ApiModelProperty("对应doris的字段类型")
    private String dorisType;

    @ApiModelProperty("字段key")
    private String fieldKey;

    @ApiModelProperty("字段名称")
    private String fieldName;

    @ApiModelProperty("字段类型")
    private String fieldType;

    @ApiModelProperty("格式 例如时间:YYYY-MM-DD HH:MM:SS")
    private String format;

    @ApiModelProperty(value = "是否显示",notes = "引入数据集时，被勾选的字段才能被使用")
    private Boolean isShow;

    @ApiModelProperty(value = "是否被选中")
    private Boolean selected;

}
