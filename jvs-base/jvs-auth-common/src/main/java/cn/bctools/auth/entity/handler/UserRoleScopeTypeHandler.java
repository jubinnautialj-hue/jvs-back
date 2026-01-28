package cn.bctools.auth.entity.handler;

import cn.bctools.auth.entity.po.UserRoleScope;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 */
public class UserRoleScopeTypeHandler extends AbstractJsonTypeHandler<List<UserRoleScope>> {
    @Override
    protected List<UserRoleScope> parse(String json) {
        List<UserRoleScope> list = JSON.parseArray(json, UserRoleScope.class);
        return new ArrayList(list);
    }

    @Override
    protected String toJson(List<UserRoleScope> obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullStringAsEmpty);
    }
}
