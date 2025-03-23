package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.NodeHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class DataExtractingParams extends NodeHtml<DataExtractingParams> {

    private DataExtractingDataField dataExtractingNode;


    @Data
    @Accessors(chain = true)
    @ApiModel("数据抽取入参")
    public static class DataExtractingDataField extends DataSourceField {
        /**
         * 前端生成的新key
         */
        private String fieldKeyNew;
        /**
         * 是否保留原有的字段
         */
        private Boolean showHistoryData;
    }

}
