package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.NodeHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("条件分组")
public class DataDeduplicationParams extends NodeHtml<DataDeduplicationParams> {
    private List<DataSourceField> dataDeduplicationObj;
}
