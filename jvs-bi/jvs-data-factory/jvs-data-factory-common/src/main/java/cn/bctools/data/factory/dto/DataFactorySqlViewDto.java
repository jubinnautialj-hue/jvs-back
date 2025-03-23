package cn.bctools.data.factory.dto;

import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class DataFactorySqlViewDto {
    @ApiModelProperty("sql")
    private String sql;
    @ApiModelProperty("输出字段")
    private List<DataSourceField> outField;

    @ApiModelProperty("输入表")
    private List<InputDataSource> inputDataSource;

    @Data
    public static class InputDataSource{
        /**
         * 数据源id
         * */
        private String dataSourceId;
        /**
         * 数据源名称
         * */
        private String sourceName;
        /**
         * 数据源类型
         * */
        private DataSourceTypeEnum sourceType;

        /**
         * 数据源表名称
         * */
        private String tableNameDesc;
        /**
         * 表id
         * */
        private String dataSourceStructureId;
    }

}
