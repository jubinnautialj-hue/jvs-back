package cn.bctools.data.factory.handler;

import cn.bctools.data.factory.entity.JvsDataFactoryLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;


/**
 * @author xiaohui
 */
@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class NodeLogHandler extends AbstractJsonTypeHandler<List<JvsDataFactoryLog.NodeLog>> {

    public NodeLogHandler(Class<?> type) {
    }

    @Override
    protected List<JvsDataFactoryLog.NodeLog> parse(String json) {
        return JSON.parseArray(json, JvsDataFactoryLog.NodeLog.class);
    }

    @Override
    protected String toJson(List<JvsDataFactoryLog.NodeLog> obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }
}