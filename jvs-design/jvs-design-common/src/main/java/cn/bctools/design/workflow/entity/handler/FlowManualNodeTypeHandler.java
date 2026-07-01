package cn.bctools.design.workflow.entity.handler;

import cn.bctools.design.workflow.entity.dto.FlowManualNode;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhuxiaokang
 */
public class FlowManualNodeTypeHandler extends AbstractJsonTypeHandler<LinkedList<FlowManualNode>> {

    @Override
    protected LinkedList<FlowManualNode> parse(String json) {
        List<FlowManualNode> list = JSON.parseArray(json, FlowManualNode.class);
        return new LinkedList<>(list);
    }

    @Override
    protected String toJson(LinkedList<FlowManualNode> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }
}
