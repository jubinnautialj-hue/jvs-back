package cn.bctools.data.factory.query.interval;

import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONArray;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IntervalHalfOpenHalfClosed implements IntervalBase{
    @Override
    public List<Object> exec(DataFieldTypeEnum fieldType, String format, String value, StringBuffer whereSql,String fieldKey) {
        whereSql.append("`").append(fieldKey).append("` > ")
                .append("? and `")
                .append(fieldKey)
                .append("` <= ")
                .append("?");
        List<Object> objects = JSONArray.parseArray(value, Object.class);
        if (DataFieldTypeEnum.isNormalDate(fieldType)) {
            objects = objects.stream().map(e -> DateUtil.format(DateUtil.parse(e.toString(), format), format)).collect(Collectors.toList());
        }
        return objects;
    }
}
