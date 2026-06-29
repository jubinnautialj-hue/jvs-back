package cn.bctools.design.data.source.impl.sql.methods;

import cn.bctools.design.data.source.impl.sql.DynamicCriteria;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: 解析后的条件
 *
 * <p> 解析{@link DynamicCriteria}，转换为mybatis可执行的sql条件片段
 * @see cn.bctools.design.data.source.impl.sql.SqlStatementUtil
 */
@Data
@Accessors(chain = true)
public class WhereParam {

    @ApiModelProperty(value = "条件sql片段")
    private String sqlSegment;

    @ApiModelProperty(value = "条件sql对应的参数")
    private Map<String, Object> param;

    public static WhereParam empty() {
        return new WhereParam()
                .setSqlSegment(StringPool.EMPTY)
                .setParam(Collections.emptyMap());
    }
}
