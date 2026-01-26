package cn.bctools.permission.handler;

import cn.bctools.permission.dto.AuthRole;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 角色类型处理器
 *
 * @author guojing
 */
@Slf4j
@MappedTypes({ Object.class })
@MappedJdbcTypes(JdbcType.VARCHAR)
public class AuthRoleTypeHandler extends AbstractJsonTypeHandler<AuthRole> {


    public AuthRoleTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public AuthRole parse(String json) {
        return JSONUtil.toBean(json, AuthRole.class);
    }

    @Override
    public String toJson(AuthRole obj) {
        return JSONUtil.toJsonStr(obj);
    }
}
