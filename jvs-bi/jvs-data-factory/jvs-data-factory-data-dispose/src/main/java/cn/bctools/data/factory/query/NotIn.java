package cn.bctools.data.factory.query;


import com.alibaba.fastjson2.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 等于任一
 *
 * @author xiaohui
 */
@Slf4j
@Component
public class NotIn implements Query {

    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        whereSql.append("`").append(queryExecDto.getFieldKey()).append("`")
                .append("not in(");
        JSONArray.parseArray(queryExecDto.getMethodValue())
                .forEach(e ->
                        whereSql.append("'")
                                .append(e)
                                .append("'")
                                .append(",")
                );
        whereSql.delete(whereSql.length() - 1, whereSql.length());
        whereSql.append(")");
        return new ArrayList<>();
    }
}
