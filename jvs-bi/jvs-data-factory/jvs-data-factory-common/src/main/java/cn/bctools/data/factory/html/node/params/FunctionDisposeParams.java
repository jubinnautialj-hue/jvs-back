package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.NodeHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class FunctionDisposeParams extends NodeHtml<FunctionDisposeParams> {
    List<FunctionDisposeObj> functionDispose;

    @Data
    @ApiModel("函数")
    public static class FunctionDisposeObj {

        @ApiModelProperty(value = "函数名称与key名称", required = true)
        private String name;
        @ApiModelProperty(value = "字段属性", required = true)
        private DataSourceField dataSourceField;
        @ApiModelProperty(value = "title-key名称", required = true)
        private String key;
        @ApiModelProperty(value = "函数", required = true)
        private String functionStr;
    }

}
