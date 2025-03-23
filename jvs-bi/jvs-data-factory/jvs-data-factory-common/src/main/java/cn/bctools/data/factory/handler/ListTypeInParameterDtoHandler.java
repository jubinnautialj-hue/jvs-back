package cn.bctools.data.factory.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import cn.bctools.data.factory.source.dto.InParameterDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;


@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ListTypeInParameterDtoHandler extends AbstractJsonTypeHandler<List<InParameterDto>> {

    public ListTypeInParameterDtoHandler(Class<?> type) {
    }

    @Override
    protected List<InParameterDto> parse(String json) {
        return JSON.parseArray(json, InParameterDto.class);
    }

    @Override
    protected String toJson(List<InParameterDto> obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }
}