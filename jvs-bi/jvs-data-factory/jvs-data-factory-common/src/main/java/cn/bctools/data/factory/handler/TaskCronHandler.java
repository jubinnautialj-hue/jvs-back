package cn.bctools.data.factory.handler;

import cn.bctools.data.factory.entity.TaskCronDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;


/**
 * @author xiaohui
 */
@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class TaskCronHandler extends AbstractJsonTypeHandler<TaskCronDto> {

    public TaskCronHandler(Class<?> type) {
    }

    @Override
    protected TaskCronDto parse(String json) {
        return JSONObject.parseObject(json, TaskCronDto.class);
    }

    @Override
    protected String toJson(TaskCronDto obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }
}