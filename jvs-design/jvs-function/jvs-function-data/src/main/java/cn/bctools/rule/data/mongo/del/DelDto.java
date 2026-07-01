package cn.bctools.rule.data.mongo.del;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.data.mongo.MongoDBSelected;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class DelDto {

    @ParameterValue(info = "数据库", type = InputType.selected, cls = MongoDBSelected.class)
    public String options;

    @ParameterValue(info = "表名称", type = InputType.input)
    public String tableName;

    @ParameterValue(info = "条件", type = InputType.map)
    public Map<String,Object> queryArgs;
}
