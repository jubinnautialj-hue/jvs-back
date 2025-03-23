package cn.bctools.data.factory.query;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 整个查询的抽取
 */

@Data
@Accessors(chain = true)
public class QueryDto {
    /**
     * 条件表达式 例如:
     * ((7207214025891909632&7207214939176439808)|7207214628764389376)
     */
    private String expression;
    /**
     * 过滤条件
     */
    private List<QueryExecDto> nodeTwigs;
}
