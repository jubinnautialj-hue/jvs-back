package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.html.NodeHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class ExecSqlSourceParams extends NodeHtml<ExecSqlSourceParams> {

    private FromSource fromSource;

    @Data
    @ApiModel("输入节点入参")
    public static class FromSource {

        /**
         * 数据源id
         */
        private String dataSourceId;

        /**
         * 数据源类型
         */
        private String sourceType;
        /**
         * 需要查询的条数
         */
        Long size;
        /**
         * sql
         */
        String execSql;
    }
}
