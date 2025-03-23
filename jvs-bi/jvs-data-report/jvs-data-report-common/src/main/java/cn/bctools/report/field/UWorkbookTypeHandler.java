package cn.bctools.report.field;

import cn.bctools.report.model.univer.UWorkbook;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class UWorkbookTypeHandler extends AbstractJsonTypeHandler<UWorkbook> {

    @Override
    protected UWorkbook parse(String json) {
        return JSONUtil.toBean(json,UWorkbook.class);
    }

    @Override
    protected String toJson(UWorkbook obj) {
        return JSONUtil.toJsonStr(obj);
    }
}
