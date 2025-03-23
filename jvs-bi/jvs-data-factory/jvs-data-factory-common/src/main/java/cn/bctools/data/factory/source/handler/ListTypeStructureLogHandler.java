package cn.bctools.data.factory.source.handler;

import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.SyncStructureLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;


@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ListTypeStructureLogHandler extends AbstractJsonTypeHandler<List<SyncStructureLog.TableDetail>> {

    public ListTypeStructureLogHandler(Class<?> type) {
    }

    @Override
    protected List<SyncStructureLog.TableDetail> parse(String json) {
        return JSON.parseArray(json, SyncStructureLog.TableDetail.class);
    }

    @Override
    protected String toJson(List<SyncStructureLog.TableDetail> obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty);
    }

}