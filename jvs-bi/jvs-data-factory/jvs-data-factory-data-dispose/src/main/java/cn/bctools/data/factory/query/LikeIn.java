package cn.bctools.data.factory.query;


import com.alibaba.fastjson2.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 包含 任一
 *
 * @author xiaohui
 */
@Slf4j
@Component
public class LikeIn implements Query {

    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        whereSql.append("(");
        JSONArray.parseArray(queryExecDto.getMethodValue())
                .forEach(e ->
                        whereSql.append("`").append(queryExecDto.getFieldKey()).append("`")
                                .append("LIKE '%")
                                .append(e)
                                .append("%'")
                                .append("OR")
                );
        whereSql.delete(whereSql.length() - 2, whereSql.length());
        whereSql.append(")");
        return new ArrayList<>();
    }
}
