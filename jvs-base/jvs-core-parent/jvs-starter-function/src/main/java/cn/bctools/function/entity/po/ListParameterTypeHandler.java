package cn.bctools.function.entity.po;

import cn.bctools.function.entity.vo.Parameter;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;

@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ListParameterTypeHandler extends AbstractJsonTypeHandler<List<Parameter>> {

    @Override
    protected List<Parameter> parse(String json) {
        return JSONArray.parseArray(json, Parameter.class);
    }

    @Override
    protected String toJson(List<Parameter> obj) {
        return JSONUtil.toJsonStr(obj);
    }
}
