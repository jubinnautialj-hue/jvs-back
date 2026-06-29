package cn.bctools.design.data.source.impl.sql.methods;

import cn.bctools.design.data.source.impl.sql.DynamicQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 动态数据SQL请求信息
 *
 * <p> 用以传递请求参数
 */
@Data
@Accessors(chain = true)
public class DynamicSqlInfo {
    @ApiModelProperty(value = "条件")
    private DynamicQuery dynamicQuery;

    @ApiModelProperty(value = "数据")
    private Object data;

    @ApiModelProperty(value = "表名")
    private String tableName;
}
