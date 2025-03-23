package cn.bctools.report.model.univer.conf.stats;

import cn.bctools.report.enums.EStatsType;
import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Data
@Accessors(chain = true)
@ApiModel("统计项设置")
public class TotalSetup implements Serializable {

    private static final long serialVersionUID = -2188228402559698429L;

    @ApiModelProperty("项唯一标识")
    private String id;

    @ApiModelProperty("统计项名称")
    private String name;

    @ApiModelProperty(value = "统计字段列表",notes = "纵向拓展 行小计可多选，横向拓展 列小计可多选")
    private List<StatsField> fields;

    @ApiModelProperty(value = "统计字段字符串列表",notes = "前端渲染使用")
    private List<String> renderFields;

    @ApiModelProperty(value = "统计类型")
    private EStatsType statsType = EStatsType.COUNT;

    @ApiModelProperty("保留小数位数 默认0")
    private Integer scale = 0;

    public Integer getFirstR(){
        return Optional.ofNullable(CollectionUtil.getFirst(this.fields)).map(StatsField::getR).orElse(-1000);
    }

    public Integer getFirstC(){
        return Optional.ofNullable(CollectionUtil.getFirst(this.fields)).map(StatsField::getC).orElse(-1000);
    }

}
