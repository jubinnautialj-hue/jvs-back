package cn.bctools.database.util;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.DbType;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 *         SQL函数
 */
public class SqlFunctionUtil {

    private static VendorDatabaseIdProvider provider = SpringContextUtil.getBean(VendorDatabaseIdProvider.class);
    private static DataSource dataSource = SpringContextUtil.getBean(DataSource.class);
    static DbType type = null;
    private static final int STEP = 2;

    private static DbType dbType() {
        if (ObjectNull.isNotNull(type)) {
            return type;
        }
        String databaseId = provider.getDatabaseId(dataSource);
        type = DbType.getDbType(databaseId);
        return type;
    }

    /**
     * JSON包含指定值
     *
     * @param columnName json字段/json数据
     * @param val        具体值(暂不支持占位符如{0})
     * @param path       路径
     * @return
     */
    public static String jsonContains(String columnName, String val, String path) {
        switch (dbType()) {
            case KINGBASE_ES:
            case POSTGRE_SQL:
                return "jsonb_path_exists(" + columnName + "::jsonb, '" + path + " ? (@==\"" + val + "\"" + ")')";
            case DM:
                // dm数据库支持JSON_CONTAINS和CONCAT函数
            default:
                // 默认Mysql
                return "JSON_CONTAINS(" + columnName + ", CONCAT('\"', '" + val + "', '\"'), '" + path + "')";
        }
    }

    /**
     * 构造JSON数据包含某对象的SQL语句
     * <p>
     * 支持的数据格式有：
     * - JSON对象
     * 如：{"email": "789797@xxx.com", "phone": "13945678945", "gender": "1"}
     * 查询phone为13945678945的数据
     * <p>
     * - 纯数组
     * 如：["aaaa","bbbb"]
     * 查询包含 bbbb 的数据
     * <p>
     * - 数组对象
     * 如：[{"name":"小明","id":"xxxxxxxxxxx","type":"user"},
     * {"name":"小红","id":"xxxxxxxxxxx11","type":"user"}]
     * 查询name为小明的数据
     *
     * @param columnName json字段
     * @param path       路径
     * @param objects    单个值表示要查询包含指定值的数据(可以是具体数据，也可以是json字符串)；
     *                   多个值必须是偶数个数据，前一个是key，后一个是val
     * @return
     */
    public static String jsonContainsObject(String columnName, String path, String... objects) {
        // 待查询的数组生成的字符串，用以组装JSON对象。其格式为："k1","v1","k2","v2"
        String objectStr = null;
        // 根据objects生成的SQL语句
        String candidate = null;
        boolean candidateJson = false;
        if (objects.length == 1) {
            if (JSON.isValid(objects[0])) {
                candidate = "'" + objects[0] + "'";
                candidateJson = true;
            } else {
                candidate = "'\"" + objects[0] + "\"'";
            }
        } else {
            objectStr = Arrays.stream(objects).map(v -> "\"" + v + "\"").collect(Collectors.joining(","));
        }

        switch (dbType()) {
            case MYSQL:
                if (ObjectNull.isNotNull(objectStr)) {
                    candidate = "JSON_OBJECT(" + objectStr + ")";
                }
                return "JSON_CONTAINS(" + columnName + ", " + candidate + ", '" + path + "')";
            case KINGBASE_ES:
            case POSTGRE_SQL:
                Function<Map<Object, Object>, String> buildCandidate = map -> map.entrySet().stream()
                        .map(e -> {
                            Object val = e.getValue();
                            if (e.getValue() instanceof CharSequence) {
                                val = "\"" + e.getValue() + "\"";
                            }
                            return "@." + e.getKey() + "==" + val;
                        }).collect(Collectors.joining(" && ", " ? (", ")"));

                if (ObjectNull.isNotNull(objectStr)) {
                    Map<Object, Object> objectMap = new HashMap<>(objects.length);
                    for (int i = 0; i < objects.length; i += STEP) {
                        objectMap.put(objects[i], objects[i + 1]);
                    }
                    candidate = buildCandidate.apply(objectMap);
                    return "jsonb_path_exists(" + columnName + "::jsonb, '" + path + candidate + "')";
                } else {
                    if (candidateJson) {
                        Map<Object, Object> object = JSON.parseObject(objects[0], HashMap.class);
                        candidate = buildCandidate.apply(object);
                        return "jsonb_path_exists(" + columnName + "::jsonb, '" + path + candidate + "')";
                    }
                    return columnName + "::jsonb @> " + candidate;
                }
            case DM:
                if (ObjectNull.isNotNull(objectStr)) {
                    objectStr = objectStr.replaceAll("\"", "'");
                    candidate = "JSON_OBJECT(" + objectStr + ")";
                }
                return "JSON_CONTAINS(" + columnName + ", " + candidate + ", '" + path + "')";
            default:
                throw new BusinessException("数据库未支持", dbType());
        }
    }

    /**
     * JSON提取数据
     *
     * @param columnName json字段
     * @param path       路径（不包含$）
     * @return
     */
    public static String jsonExtract(String columnName, String path) {
        switch (dbType()) {
            case MYSQL:
                return "JSON_EXTRACT(" + columnName + ", '$." + path + "')";
            case KINGBASE_ES:
            case POSTGRE_SQL:
                return "JSON_EXTRACT_PATH_TEXT(" + columnName + ", '" + path + "')";
            case DM:
                return "REPLACE(JSON_EXTRACT(" + columnName + ", '$." + path + "'), '\"', '')";
            default:
                throw new BusinessException("数据库未支持", dbType());
        }
    }
}
