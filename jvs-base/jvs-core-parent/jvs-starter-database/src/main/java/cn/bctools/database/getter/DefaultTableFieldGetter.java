package cn.bctools.database.getter;

import cn.bctools.database.entity.TableInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 默认的表字段获取方式
 * <p>
 * 项目启动时获取当前数据库连接的字段信息
 *
 * @Author: GuoZi
 */
@Slf4j
public class DefaultTableFieldGetter implements ITableFieldGetter {

    private final Map<String, List<String>> tableInfoCache = new HashMap<>(2 << 7);
    @Getter
    private static final Set<String> TABLELIST = new HashSet<>(3);
    private static final String TENANTID = "tenant_id";

    @Override
    public List<String> getFieldNames(String ip, String port, String databaseName, String tableName) {
        String key = this.getKey(ip, port, databaseName, tableName);
        List<String> fieldNames = tableInfoCache.get(key);
        if (Objects.isNull(fieldNames)) {
            fieldNames = Collections.emptyList();
        }
        return fieldNames;
    }

    public void saveCache(@NotNull List<TableInfo> tableInfo) {
        for (TableInfo info : tableInfo) {
            String key = this.getKey(info.getIp(), info.getPort(), info.getDatabaseName(), info.getTableName()).toLowerCase();
            List<String> fieldNames = tableInfoCache.computeIfAbsent(key, e -> new ArrayList<>());
            fieldNames.add(info.getFieldName().toLowerCase());
            if (TENANTID.equals(info.getFieldName())) {
                TABLELIST.add(info.getTableName());
            }
        }
    }

    private String getKey(String ip, String port, String databaseName, String tableName) {
        return String.format("%s:%s:%s:%s", ip, port, databaseName, tableName);
    }

}
