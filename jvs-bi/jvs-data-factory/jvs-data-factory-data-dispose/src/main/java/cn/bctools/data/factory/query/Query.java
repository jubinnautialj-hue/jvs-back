package cn.bctools.data.factory.query;

import java.util.List;

/**
 * 过滤条件的抽象类
 *
 * @author xiaohui
 */
public interface Query {

    /**
     * 生成条件
     *
     * @param queryExecDto 入参
     * @param whereSql     条件sql
     * @return 条件的值
     */
    List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql);
}
