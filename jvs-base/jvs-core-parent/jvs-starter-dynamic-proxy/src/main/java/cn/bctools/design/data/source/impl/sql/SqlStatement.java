package cn.bctools.design.data.source.impl.sql;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: mybatis mapper 参数
 *
 * <p> 解析后最终生成完整的sql及相关数据，用以mybatis调用
 */
@Data
@Accessors(chain = true)
public class SqlStatement {

    @ApiModelProperty(value = "完整的sql")
    private String sql;

    @ApiModelProperty(value = "数据")
    private Object data;

    @ApiModelProperty(value = "条件参数")
    private Map<String, Object> whereParam;
}
