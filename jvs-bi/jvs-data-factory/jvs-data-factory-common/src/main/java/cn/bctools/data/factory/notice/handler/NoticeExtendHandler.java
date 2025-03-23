package cn.bctools.data.factory.notice.handler;

import cn.bctools.data.factory.notice.dto.NoticeExtendTemplateDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description:
 */
public class NoticeExtendHandler extends AbstractJsonTypeHandler<List<NoticeExtendTemplateDto>> {

    @Override
    protected List<NoticeExtendTemplateDto> parse(String json) {
        List<NoticeExtendTemplateDto> list = JSON.parseArray(json, NoticeExtendTemplateDto.class);
        return new LinkedList<>(list);
    }

    @Override
    protected String toJson(List<NoticeExtendTemplateDto> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }
}
