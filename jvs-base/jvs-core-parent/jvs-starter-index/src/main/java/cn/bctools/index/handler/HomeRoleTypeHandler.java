package cn.bctools.index.handler;

import cn.bctools.index.entity.HomeRole;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jvs The type Home role type handler.
 */
public class HomeRoleTypeHandler extends AbstractJsonTypeHandler<List<HomeRole>> {

    @Override
    protected List<HomeRole> parse(String json) {
        List<HomeRole> list = JSON.parseArray(json, HomeRole.class);
        return new ArrayList(list);
    }

    @Override
    protected String toJson(List<HomeRole> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }
}
