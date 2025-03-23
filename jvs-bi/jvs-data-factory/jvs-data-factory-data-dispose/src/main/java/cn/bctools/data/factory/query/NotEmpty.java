package cn.bctools.data.factory.query;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 不为空
 *
 * @author xiaohui
 */
@Slf4j
@Component
public class NotEmpty implements Query {

    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        whereSql.append("not_null_or_empty(").append("`").append(queryExecDto.getFieldKey()).append("`").append(")");
        return new ArrayList<>();
    }
}
