package cn.bctools.design.workflow.entity.handler;

import cn.bctools.design.workflow.entity.dto.TransferDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 自定义typeHandler，支持LinkedList<TransferDto>
 */
public class TransferTypeHandler  extends AbstractJsonTypeHandler<LinkedList<TransferDto>> {
    public TransferTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public LinkedList<TransferDto> parse(String json) {
        List<TransferDto> list = JSON.parseArray(json, TransferDto.class);
        return new LinkedList<>(list);
    }

    @Override
    public String toJson(LinkedList<TransferDto> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }

}
