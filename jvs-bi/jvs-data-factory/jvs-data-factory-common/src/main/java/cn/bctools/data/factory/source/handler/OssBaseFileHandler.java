package cn.bctools.data.factory.source.handler;

import cn.bctools.oss.dto.FileNameDto;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class OssBaseFileHandler extends AbstractJsonTypeHandler<FileNameDto> {

    public OssBaseFileHandler(Class<?> type) {
    }

    @Override
    protected FileNameDto parse(String json) {
        return JSONUtil.toBean(json, FileNameDto.class);
    }

    @Override
    protected String toJson(FileNameDto obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }
}
