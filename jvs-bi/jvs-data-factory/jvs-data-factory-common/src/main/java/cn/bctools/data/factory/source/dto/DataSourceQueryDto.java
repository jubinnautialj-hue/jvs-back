package cn.bctools.data.factory.source.dto;

import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DataSourceQueryDto {

    private String sourceName;

    private List<DataSourceTypeEnum> typeEnumList;
}
