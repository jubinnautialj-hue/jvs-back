package cn.bctools.report.model.univer;

import cn.bctools.database.util.IdGenerator;
import cn.bctools.report.enums.EDataAggTech;
import cn.bctools.report.enums.EDataGrowthPlan;
import cn.bctools.report.enums.ESortType;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.conf.UField;
import cn.bctools.report.model.univer.conf.UJoin;
import cn.bctools.report.model.univer.conf.stats.StatsSetup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Optional;

@Data
@Accessors(chain = true)
@ApiModel("单元拓展设置")
public class UCellExpand implements Serializable,Cloneable {

    private static final long serialVersionUID = -3652493087386546371L;
    @ApiModelProperty(value = "行号",notes = "后端生成")
    private Integer r;

    @ApiModelProperty(value = "列号",notes = "后端生成")
    private Integer c;

    @ApiModelProperty(value = "原始行号",notes = "后端生成，初始化后不可更改,用于计算合并")
    private Integer oR;

    @ApiModelProperty(value = "原始列号",notes = "后端生成，初始化后不可更改,用于计算合并")
    private Integer oC;

    @ApiModelProperty("值类型")
    private EValueType valueType;

    @ApiModelProperty(value = "数据列",notes = "单元格位置，A3")
    private String location;

    @ApiModelProperty(value = "动态属性",notes = "交叉报表 动态属性")
    private Boolean dynamic = false;

    @ApiModelProperty("聚合方式")
    private EDataAggTech dataAggTech = EDataAggTech.LIST;

    @ApiModelProperty("拓展方向")
    private EDataGrowthPlan dataGrowthPlan = EDataGrowthPlan.VERTICAL;

    @ApiModelProperty("排序方式")
    private ESortType sortType = ESortType.NONE;

    @ApiModelProperty(value = "字段属性",notes = "当前单元格中选择的数据集的字段的数据")
    private UField field;

    @ApiModelProperty(value = "是否已被标记",notes = "被标记后无法与其他单元格组成组合")
    private Boolean flagged = false;

    @ApiModelProperty("空值替换值")
    private Object nullReplaceValue;

    /*小计、总计*/
    @ApiModelProperty("列小计设置")
    private StatsSetup colSubStatsSetup;
    @ApiModelProperty("列总计设置")
    private StatsSetup colTotalStatsSetup;
    @ApiModelProperty("行小计设置")
    private StatsSetup rowSubStatsSetup;
    @ApiModelProperty("行总计设置")
    private StatsSetup rowTotalStatsSetup;

    @ApiModelProperty("合并的时候是否忽略当前单元格")
    private Boolean mergeIgnore = Boolean.FALSE;

    @ApiModelProperty(value = "自定义合并标识",notes = "忽略合并链后，会以自定义合并标识进行合并，且无法加入合并链")
    private String customMergeSign;

    @ApiModelProperty(value = "是否忽略合并链",notes = "多个分组单元格 会形成合并链 例如：[[2,3],[[1,1],[1,2]]]")
    private Boolean ignoreGroupChain = Boolean.FALSE;

    @ApiModelProperty(value = "多表关联条件")
    private UJoin join = new UJoin();

    public boolean isCrossTable(){
        return EDataGrowthPlan.CROSS_TAB.equals(this.getDataGrowthPlan());
    }

    @Override
    protected UCellExpand clone() throws CloneNotSupportedException {
        return (UCellExpand)super.clone();
    }

    public void randomCustomSign(){
        this.setCustomMergeSign(IdGenerator.getIdStr(36));
    }

    public boolean isStats(){
        return Optional.ofNullable(this.colSubStatsSetup).map(StatsSetup::getEnabled).orElse(false)
                || Optional.ofNullable(this.colTotalStatsSetup).map(StatsSetup::getEnabled).orElse(false)
                || Optional.ofNullable(this.rowSubStatsSetup).map(StatsSetup::getEnabled).orElse(false)
                || Optional.ofNullable(this.rowTotalStatsSetup).map(StatsSetup::getEnabled).orElse(false);
    }
}
