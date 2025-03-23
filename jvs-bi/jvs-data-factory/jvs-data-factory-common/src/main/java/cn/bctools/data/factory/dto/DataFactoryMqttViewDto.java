package cn.bctools.data.factory.dto;

import cn.bctools.data.factory.enums.CollectTypeEnum;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.hutool.core.date.DateField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class DataFactoryMqttViewDto {
    @ApiModelProperty("登录密码")
    private String sourcePwd;

    @ApiModelProperty("登录用户名")
    private String sourceUserName;

    @ApiModelProperty("ip地址")
    private String sourceHost;

    @ApiModelProperty("端口")
    private Integer sourcePort;
    @ApiModelProperty("订阅的主题")
    private String subTopic;

    @ApiModelProperty("时间 单位")
    private DateField timeUnit;

    @ApiModelProperty("时间跨度")
    private Integer timeSpan;

    @ApiModelProperty("最大条数")
    private Integer maxNumber;


    @ApiModelProperty("聚合方式")
    private CollectTypeEnum collectType;

    @ApiModelProperty("聚合的key")
    private String collectKey;
    @ApiModelProperty("输入源")
    private InputDataSource inputDataSource;

    @Data
    public static class InputDataSource {
        /**
         * 数据源id
         */
        private String dataSourceId;
        /**
         * 数据源名称
         */
        private String sourceName;
        /**
         * 数据源类型
         */
        private DataSourceTypeEnum sourceType;

        /**
         * 数据源表名称
         */
        private String tableNameDesc;
        /**
         * 表id
         */
        private String dataSourceStructureId;
    }

}
