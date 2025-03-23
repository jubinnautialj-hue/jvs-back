package cn.bctools.data.factory.html.node.dto;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.enums.FieldSetupSortEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FieldSortDto extends DataSourceField {
    /**
     * 排序
     */
    private FieldSetupSortEnum sortType;
}
