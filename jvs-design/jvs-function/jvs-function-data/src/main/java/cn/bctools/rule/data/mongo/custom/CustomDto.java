package cn.bctools.rule.data.mongo.custom;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.data.mongo.MongoDBSelected;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CustomDto {

    @ParameterValue(info = "数据库", type = InputType.selected, cls = MongoDBSelected.class)
    public String options;

    @ParameterValue(info = "自定查询语句", type = InputType.longtext)
    public String customJson;
}
