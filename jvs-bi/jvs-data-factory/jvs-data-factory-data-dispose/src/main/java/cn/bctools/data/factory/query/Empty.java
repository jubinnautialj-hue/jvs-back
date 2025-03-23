package cn.bctools.data.factory.query;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 为空
 *
 * @author xiaohui
 */
@Slf4j
@Component
public class Empty implements Query {

    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        whereSql.append("null_or_empty(").append("`").append(queryExecDto.getFieldKey()).append("`").append(")");
        return new ArrayList<>();
    }
}
