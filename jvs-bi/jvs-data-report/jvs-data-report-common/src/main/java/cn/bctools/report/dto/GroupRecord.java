package cn.bctools.report.dto;

import cn.bctools.report.enums.EDataGrowthPlan;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GroupRecord {

    private Integer r;

    private Integer c;

    private EDataGrowthPlan dataGrowthPlan;

    private List<Integer> groupList;

}
