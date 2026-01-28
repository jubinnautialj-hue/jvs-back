package cn.bctools.auth.entity.handler;

import cn.bctools.common.utils.ObjectNull;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jvs
 */
@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class DeptJsonTypeHandler extends AbstractJsonTypeHandler<List<String>> {

    @Override
    protected List<String> parse(String json) {
        if (JSONUtil.isTypeJSON(json)) {
            //判断是数组，
            return JSONArray.parseArray(json, String.class);
        }
        if (ObjectNull.isNotNull(json)) {
            List<String> list = new ArrayList<>();
            list.add(json);
            return list;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected String toJson(List<String> obj) {
        return JSONObject.toJSONString(obj);
    }

}
