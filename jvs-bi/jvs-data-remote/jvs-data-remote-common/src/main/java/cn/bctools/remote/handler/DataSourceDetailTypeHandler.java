package cn.bctools.remote.handler;

import cn.bctools.remote.dto.DataSourceDetail;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
@NoArgsConstructor
public class DataSourceDetailTypeHandler extends AbstractJsonTypeHandler<DataSourceDetail> {

    @Override
    protected DataSourceDetail parse(String json) {
        return JSONUtil.toBean(json,DataSourceDetail.class);
    }

    @Override
    protected String toJson(DataSourceDetail obj) {
        return JSONUtil.toJsonStr(obj);
    }
}
