package cn.bctools.data.factory.source.handler;

import cn.bctools.data.factory.source.data.po.JsonAnalysisPo;
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
public class ListTypeJsonAnalysisHandler extends AbstractJsonTypeHandler<List<JsonAnalysisPo>> {

    public ListTypeJsonAnalysisHandler(Class<?> type) {
    }

    @Override
    protected List<JsonAnalysisPo> parse(String json) {
        return JSON.parseArray(json, JsonAnalysisPo.class);
    }

    @Override
    protected String toJson(List<JsonAnalysisPo> obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }
}