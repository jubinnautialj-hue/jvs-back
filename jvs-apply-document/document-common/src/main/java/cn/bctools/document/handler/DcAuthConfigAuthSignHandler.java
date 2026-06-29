package cn.bctools.document.handler;

import cn.bctools.document.dto.IdentifyingAuthDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;

/**
 * 权限绑定时的mysql json 拦截器
 *
 * @author xiaohui
 */
@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class DcAuthConfigAuthSignHandler extends AbstractJsonTypeHandler<List<IdentifyingAuthDto>> {
    @Override
    protected List<IdentifyingAuthDto> parse(String json) {
        return JSONObject.parseArray(json, IdentifyingAuthDto.class);
    }

    @Override
    protected String toJson(List<IdentifyingAuthDto> obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }
}
