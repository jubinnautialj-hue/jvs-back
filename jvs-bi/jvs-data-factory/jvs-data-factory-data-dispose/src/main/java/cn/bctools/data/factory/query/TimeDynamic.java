package cn.bctools.data.factory.query;

import cn.bctools.data.factory.query.dynamic.DynamicTimeExecuteFactory;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态时间范围
 *
 * @author xiaohui
 */
@Component
@Slf4j
public class TimeDynamic implements Query {
    @Autowired
    DynamicTimeExecuteFactory dynamicTimeExecuteFactory;

    @Override
    public List<Object> exec(QueryExecDto queryExecDto, StringBuffer whereSql) {
        DynamicTimeDto dynamicTimeDto = JSONObject.parseObject(queryExecDto.getMethodValue(), DynamicTimeDto.class);
        dynamicTimeDto.setFormat(queryExecDto.getFormat());
        DynamicTimeValue dynamicTimeValue = dynamicTimeExecuteFactory.execute(dynamicTimeDto);
        //根据format 重新设置数据格式 防止数据格式不一致 导致对比结果错误
        whereSql.append("`").append(queryExecDto.getFieldKey()).append("`")
                .append(" BETWEEN")
                .append(" ? and ? ");
        List<Object> list = new ArrayList<>();
        list.add(DateUtil.format(dynamicTimeValue.getStartTime(), queryExecDto.getFormat()));
        list.add(DateUtil.format(dynamicTimeValue.getEndTime(), queryExecDto.getFormat()));
        return list;
    }
}
