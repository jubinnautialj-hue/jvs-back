package cn.bctools.permission.handler;

import cn.bctools.permission.dto.AuthRole;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;


@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ListAuthRoleTypeHandler extends AbstractJsonTypeHandler<List<AuthRole>> {

    public ListAuthRoleTypeHandler(Class<?> type) {
    }

    @Override
    protected List<AuthRole> parse(String json) {
        return JSONUtil.toList(json, AuthRole.class);
    }

    @Override
    protected String toJson(List<AuthRole> obj) {
        return JSONUtil.toJsonStr(obj);
    }
}
