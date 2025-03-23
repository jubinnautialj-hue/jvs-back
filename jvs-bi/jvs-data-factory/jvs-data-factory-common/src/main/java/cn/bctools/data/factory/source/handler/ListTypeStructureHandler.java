package cn.bctools.data.factory.source.handler;

import cn.bctools.data.factory.source.entity.DataSourceStructure;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;


@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ListTypeStructureHandler extends AbstractJsonTypeHandler<List<DataSourceStructure.Structure>> {

    public ListTypeStructureHandler(Class<?> type) {
    }

    @Override
    protected List<DataSourceStructure.Structure> parse(String json) {
        return JSON.parseArray(json, DataSourceStructure.Structure.class);
    }

    @Override
    protected String toJson(List<DataSourceStructure.Structure> obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }

}