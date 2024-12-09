package cn.bctools.design.rule.entity.handler;

import cn.bctools.design.rule.entity.ParameterMap;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * @author jvs
 * 自定义typeHandler，支持LinkedList<CourseDto>
 */
public class ParameterMapTypeHandler extends AbstractJsonTypeHandler<List<ParameterMap>> {
    @Override
    protected List<ParameterMap> parse(String json) {
        List<ParameterMap> list = JSON.parseArray(json, ParameterMap.class);
        return new LinkedList<>(list);
    }

    @Override
    protected String toJson(List<ParameterMap> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }

}
