package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.html.node.dto.FilterParam;
import cn.bctools.data.factory.query.QueryDto;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FilterParams extends NodeHtml<FilterParams> {

    private QueryDto ruleObj;

}
