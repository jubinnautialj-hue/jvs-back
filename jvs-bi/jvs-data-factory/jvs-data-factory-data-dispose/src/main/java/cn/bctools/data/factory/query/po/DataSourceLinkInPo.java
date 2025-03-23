package cn.bctools.data.factory.query.po;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel("包含任一的数据源")
public class DataSourceLinkInPo {
    /**
     * 数据集id
     */
    private String id;
    /**
     * 数据集key
     */
    private String fieldKey;
    /**
     * 数据集存储的表名称
     */
    private String tableName;

}
