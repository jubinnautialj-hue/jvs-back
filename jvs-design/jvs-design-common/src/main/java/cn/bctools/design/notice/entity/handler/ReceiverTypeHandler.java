package cn.bctools.design.notice.entity.handler;

import cn.bctools.design.notice.handler.bo.ReceiverBo;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 自定义typeHandler，支持List<ReceiverBo>
 */
public class ReceiverTypeHandler extends AbstractJsonTypeHandler<List<ReceiverBo>> {

    @Override
    protected List<ReceiverBo> parse(String json) {
        List<ReceiverBo> list = JSON.parseArray(json, ReceiverBo.class);
        return new LinkedList<>(list);
    }

    @Override
    protected String toJson(List<ReceiverBo> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }

}
