package cn.bctools.data.factory.query;


import cn.bctools.data.factory.query.po.DataSourceLinkInPo;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 不包含 任一
 *
 * @author xiaohui
 */
@Slf4j
@Component
@AllArgsConstructor
public class DataSourceNotLikeIn implements Query {

    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        whereSql.append("`").append(queryExecDto.getFieldKey()).append("`").append("NOT IN ( ");
        DataSourceLinkInPo dataSourceLinkInPo = JSONObject.parseObject(queryExecDto.getMethodValue(), DataSourceLinkInPo.class);
        whereSql.append("SELECT ")
                .append("`")
                .append(dataSourceLinkInPo.getFieldKey())
                .append("`")
                .append("FROM ")
                .append(dataSourceLinkInPo.getTableName());
        whereSql.append(" )");
        return new ArrayList<>();
    }
}
