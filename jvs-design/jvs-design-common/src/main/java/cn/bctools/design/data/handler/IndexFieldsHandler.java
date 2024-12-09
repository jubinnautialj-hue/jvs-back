package cn.bctools.design.data.handler;

import cn.bctools.design.crud.entity.IndexFields;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Index fields handler.
 */
public class IndexFieldsHandler extends AbstractJsonTypeHandler<ArrayList<IndexFields>> {

    @Override
    protected ArrayList<IndexFields> parse(String json) {
        List<IndexFields> list = JSON.parseArray(json, IndexFields.class);
        return new ArrayList<>(list);
    }

    @Override
    protected String toJson(ArrayList<IndexFields> obj) {
        return JSON.toJSONString(obj);
    }
}
