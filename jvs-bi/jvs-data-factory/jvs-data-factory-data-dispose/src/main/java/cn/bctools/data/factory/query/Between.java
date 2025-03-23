package cn.bctools.data.factory.query;

import cn.bctools.common.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xiaohui
 */
@Slf4j
@Component
public class Between implements Query {
    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        whereSql
                .append("( ");
        List<Object> exec = SpringContextUtil.getBean(queryExecDto.getIntervalType().getAClass()).exec(queryExecDto.getFieldType(), queryExecDto.getFormat(), queryExecDto.getMethodValue(), whereSql,queryExecDto.getFieldKey());
        whereSql.append(" )");
        return exec;
    }

}
