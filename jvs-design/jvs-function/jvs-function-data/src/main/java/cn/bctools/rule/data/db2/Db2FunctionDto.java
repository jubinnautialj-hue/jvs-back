package cn.bctools.rule.data.db2;

import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.data.enums.OperationEnum;
import cn.bctools.rule.data.mongo.MongodbDataSourceSelected;
import cn.bctools.rule.data.selected.OperationTypeSelected;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author wl
 */
@Data
@Inspect
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Db2FunctionDto {


    @NotNull(message = "数据源不能为空")
    @ParameterValue(info = "目标数据源", type = InputType.selected, cls = MongodbDataSourceSelected.class)
    public String dbName;

    @ParameterValue(info = "操作类型", type = InputType.selected, cls = OperationTypeSelected.class)
    public OperationEnum operation;

    @NotNull(message = "执行JSON不能为空")
    @ParameterValue(info = "执行语句", type = InputType.json)
    public String execJson;

}
