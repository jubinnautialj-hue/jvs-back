package cn.bctools.data.factory.query;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 不包含 字符串
 *
 * @author xiaohui
 */
@Slf4j
@Component
public class NotLike implements Query {

    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        whereSql.append("`").append(queryExecDto.getFieldKey()).append("`")
                .append("not like")
                .append("('%")
                .append(queryExecDto.getMethodValue())
                .append("%')");
        return new ArrayList<>();
    }

}
