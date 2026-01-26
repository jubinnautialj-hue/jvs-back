package cn.bctools.design.workflow.entity.handler;

import cn.bctools.design.workflow.entity.dto.CourseDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 自定义typeHandler，支持LinkedList<CourseDto>
 */
public class CoursesTypeHandler extends AbstractJsonTypeHandler<LinkedList<CourseDto>> {
    public CoursesTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public LinkedList<CourseDto> parse(String json) {
        List<CourseDto> list = JSON.parseArray(json, CourseDto.class);
        return new LinkedList<>(list);
    }

    @Override
    public String toJson(LinkedList<CourseDto> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }

}
