package cn.bctools.data.factory.query;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NotEq implements Query {
    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        //判断对比值是否为null 如果为null 就不能使用 jdbc 自动填充值的功能
        if (StrUtil.isBlank(queryExecDto.getMethodValue())) {
            if (queryExecDto.getFieldType().getClassifyEnum().equals(DataFieldTypeClassifyEnum.字符串)) {
                return SpringContextUtil.getBean(NotEmpty.class).exec(queryExecDto, whereSql);
            } else {
                whereSql.append("`").append(queryExecDto.getFieldKey()).append("`")
                        .append(" is not null");
                return new ArrayList<>();
            }
        } else {
            whereSql.append("`").append(queryExecDto.getFieldKey()).append("`")
                    .append("!=")
                    .append(" ")
                    .append("?");
        }
        Object value = queryExecDto.getMethodValue();
        List<Object> list = new ArrayList<>();
        list.add(value);
        return list;
    }
}
