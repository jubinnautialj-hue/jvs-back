package cn.bctools.data.factory.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 大于等于
 *
 * @author xiaohui
 */
@Component
@Slf4j
public class Ge implements Query {
    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        whereSql.append("`").append(queryExecDto.getFieldKey()).append("`")
                .append(">=")
                .append(" ")
                .append("?");
        Object value = queryExecDto.getMethodValue();
        List<Object> list = new ArrayList<>();
        list.add(value);
        return list;
    }
}
