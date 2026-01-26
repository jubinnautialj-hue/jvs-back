package cn.bctools.design.workflow.entity.handler;

import cn.bctools.design.workflow.entity.dto.ParallelBranchDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 自定义typeHandler，支持List<ParallelBranchDto>
 */
public class ParallelBranchTypeHandler extends AbstractJsonTypeHandler<List<ParallelBranchDto>> {

    public ParallelBranchTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public List<ParallelBranchDto> parse(String json) {
        List<ParallelBranchDto> list = JSON.parseArray(json, ParallelBranchDto.class);
        return new ArrayList(list);
    }

    @Override
    public String toJson(List<ParallelBranchDto> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
   }
}
