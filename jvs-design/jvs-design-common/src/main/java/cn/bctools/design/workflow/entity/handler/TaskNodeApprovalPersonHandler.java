package cn.bctools.design.workflow.entity.handler;

import cn.bctools.common.entity.dto.UserDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 */
public class TaskNodeApprovalPersonHandler extends AbstractJsonTypeHandler<List<UserDto>> {

    public TaskNodeApprovalPersonHandler(Class<?> type) {
        super(type);
    }

    @Override
    public List<UserDto> parse(String json) {
        List<UserDto> list = JSON.parseArray(json, UserDto.class);
        return new ArrayList(list);
    }

    @Override
    public String toJson(List<UserDto> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }
}
