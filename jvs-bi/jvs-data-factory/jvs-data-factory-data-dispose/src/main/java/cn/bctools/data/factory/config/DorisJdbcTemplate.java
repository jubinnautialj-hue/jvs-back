package cn.bctools.data.factory.config;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.dto.OrderField;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.database.util.IdGenerator;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author doris jdbc
 */
@Slf4j
public class DorisJdbcTemplate extends JdbcTemplate {
    private final static String INSERT_SQL = "INSERT INTO ";

    private final static String COUNT_SQL = "SELECT COUNT(*) FROM {}.{} AS A";

    private final static String COUNT_SQL_TO_SQL = "SELECT COUNT(*) FROM {}";
    private final static String GET_ALL_SQL = "SELECT * FROM {}";

    private final static String DROP_SQL_FORCE = "DROP TABLE IF EXISTS {} FORCE";

    private final static String DROP_SQL = "DROP TABLE IF EXISTS {}";

    private final static String SHOW_TABLES = "SHOW TABLES";

    private final static String SHOW_COLUMNS = "SHOW FULL COLUMNS FROM {}";

    public DorisJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 获取所有数据 自动格式化时间
     *
     * @param tableName 表名称
     */
    public List<Map<String, Object>> findAllToFormat(String tableName, List<DataSourceField> headers, List<JSONObject> jsonArray) {
        //过滤不需要展示的字段
        headers = headers.stream().filter(DataSourceField::getIsShow).collect(Collectors.toList());
        StringBuilder sql = new StringBuilder();
        String column = headers.stream().map(e -> {
            //判断是否为时间类型如果是时间类型需要格式化
            DataFieldTypeEnum fieldType = e.getFieldType();
            if (DataFieldTypeEnum.isNormalDate(fieldType) || fieldType.equals(DataFieldTypeEnum.BIGINT)) {
                return "CAST(`" + e.getFieldKey() + "` AS STRING) as `" + e.getFieldKey() + "`";
            }
            return "`" + e.getFieldKey() + "`";
        }).collect(Collectors.joining(","));
        sql.append("SELECT ").append(column).append(" FROM ").append(tableName);
        //添加排序
        if (jsonArray != null && !jsonArray.isEmpty()) {
            sql.append(" order by ");
            String sortSql = jsonArray.stream().map(e -> {
                String fieldKey = e.getString("fieldKey");
                String sortType = e.getString("sortType");
                return "`" + fieldKey + "` " + sortType;
            }).collect(Collectors.joining(","));
            sql.append(sortSql);
        }
        String execSql = sql.toString();
        return this.queryForList(execSql);
    }

    /**
     * 获取总条数
     *
     * @param tableName 表名称
     */
    public Long getCount(String tableName) {
        DorisConfig dorisConfig = SpringContextUtil.getBean(DorisConfig.class);
        String countSql = StrUtil.format(COUNT_SQL, dorisConfig.getLibraryName(), tableName);
        boolean b = ifNotExistsTable(tableName);
        if (b) {
            return this.queryForObject(countSql, Long.class);
        }
        return 0L;
    }

    /**
     * 基于sql获取
     *
     * @param sql sql 语句
     */
    public long getCountSql(String sql) {
        DorisConfig dorisConfig = SpringContextUtil.getBean(DorisConfig.class);
        String countSql = StrUtil.format(COUNT_SQL_TO_SQL, sql);
        return this.queryForObject(countSql, Long.class);
    }

    /**
     * 获取总条数
     *
     * @param tableName 表名称
     * @param whereSql  查询条件
     * @param objects   入参
     */
    public Long getCount(String tableName, String whereSql, List<Object> objects) {
        DorisConfig dorisConfig = SpringContextUtil.getBean(DorisConfig.class);
        String countSql = StrUtil.format(COUNT_SQL, dorisConfig.getLibraryName(), tableName);
        if (StrUtil.isNotBlank(whereSql)) {
            countSql = countSql + " where " + whereSql;
        }
        boolean b = ifNotExistsTable(tableName);
        if (b) {
            return this.queryForObject(countSql, Long.class, objects.toArray());
        }
        return 0L;
    }


    /**
     * 获取全量数据
     *
     * @param sql sql语句
     */
    public List<Map<String, Object>> getData(String sql, List<Object> objects) {
        log.info("执行的sql为:{}", sql);
        if (objects.isEmpty()) {
            return this.queryForList(sql);
        }
        log.info("入参为:{}", JSONObject.toJSONString(objects));
        return this.queryForList(sql, objects.toArray());
    }

    /**
     * 分页获取数据 自动生成sql语句
     *
     * @param size      每页数量
     * @param current   当前页码
     * @param tableName 表名称
     */
    public List<Map<String, Object>> page(long size, long current, String tableName) {
        String sql = StrUtil.format(GET_ALL_SQL, tableName);
        boolean b = ifNotExistsTable(tableName);
        if (b) {
            //随机一个字段用于排序
            sql = addOderBy(sql, tableName);
            sql = sql + " LIMIT " + size * (current - 1) + "," + size;
            return this.queryForList(sql);
        }
        return new ArrayList<>();
    }

    /**
     * 分页获取数据
     *
     * @param size      每页数量
     * @param current   当前页码
     * @param tableName 表名称
     * @param headers   查询的字段
     * @param objects   入参
     * @param whereSql  查询条件
     */
    public List<Map<String, Object>> page(long size, long current, String tableName, List<DataSourceField> headers, String whereSql, List<Object> objects) {
        headers = headers.stream().filter(DataSourceField::getIsShow).collect(Collectors.toList());
        StringBuilder sql = new StringBuilder();
        String column = headers.stream().map(e -> {
            //判断是否为时间类型如果是时间类型需要格式化
            DataFieldTypeEnum fieldType = e.getFieldType();
            if (DataFieldTypeEnum.isNormalDate(fieldType)) {
                return "CAST(`" + e.getFieldKey() + "` AS STRING) as `" + e.getFieldKey() + "`";
            }
            return "`" + e.getFieldKey() + "`";
        }).collect(Collectors.joining(","));
        sql.append("SELECT ").append(column).append(" FROM ").append(tableName);
        boolean b = ifNotExistsTable(tableName);
        if (b) {
            if (StrUtil.isNotBlank(whereSql)) {
                sql.append(" where ").append(whereSql);
            }
            //随机一个字段用于排序
            String execSql = addOderBy(sql.toString(), tableName);
            execSql = execSql + " LIMIT " + size * (current - 1) + "," + size;
            return this.queryForList(execSql, objects.toArray());
        }
        return new ArrayList<>();
    }

    public List<Map<String, Object>> page(long size, long current, String tableName, List<DataSourceField> headers) {
        headers = headers.stream().filter(DataSourceField::getIsShow).collect(Collectors.toList());
        StringBuilder sql = new StringBuilder();
        String column = headers.stream().map(e -> {
            //判断是否为时间类型如果是时间类型需要格式化
            DataFieldTypeEnum fieldType = e.getFieldType();
            if (DataFieldTypeEnum.isNormalDate(fieldType)) {
                return "CAST(`" + e.getFieldKey() + "` AS STRING) as `" + e.getFieldKey() + "`";
            }
            return "`" + e.getFieldKey() + "`";
        }).collect(Collectors.joining(","));
        sql.append("SELECT ").append(column).append(" FROM ").append(tableName);
        boolean b = ifNotExistsTable(tableName);
        if (b) {
            //随机一个字段用于排序
            String execSql = addOderBy(sql.toString(), tableName);
            execSql = execSql + " LIMIT " + size * (current - 1) + "," + size;
            return this.queryForList(execSql);
        }
        return new ArrayList<>();
    }

    /**
     * 获取某张表的全量数据  谨慎使用此方法  可能会导致 系统oom
     *
     * @param tableName 表名称
     */
    public List<Map<String, Object>> getData(String tableName) {
        String sql = StrUtil.format(GET_ALL_SQL, tableName);
        log.info("执行的sql 为:{}", sql);
        boolean b = ifNotExistsTable(tableName);
        if (b) {
            return this.queryForList(sql);
        }
        return new ArrayList<>();
    }

    /**
     * 获取某张表的全量数据  谨慎使用此方法  可能会导致 系统oom
     *
     * @param tableName 表名称
     * @param headers   字段
     * @param whereSql  查询条件
     * @param objects   入参
     */
    public List<Map<String, Object>> getData(String tableName, List<DataSourceField> headers, String whereSql, List<Object> objects) {
        headers = headers.stream().filter(DataSourceField::getIsShow).collect(Collectors.toList());
        StringBuilder sql = new StringBuilder();
        String column = headers.stream().map(e -> {
            //判断是否为时间类型如果是时间类型需要格式化
            DataFieldTypeEnum fieldType = e.getFieldType();
            if (DataFieldTypeEnum.isNormalDate(fieldType)) {
                return "CAST(`" + e.getFieldKey() + "` AS STRING) as `" + e.getFieldKey() + "`";
            }
            return "`" + e.getFieldKey() + "`";
        }).collect(Collectors.joining(","));
        sql.append("SELECT ").append(column).append(" FROM ").append(tableName);
        boolean b = ifNotExistsTable(tableName);
        if (b) {
            if (StrUtil.isNotBlank(whereSql)) {
                sql.append(" where ").append(whereSql);
            }
            return this.queryForList(sql.toString(), objects.toArray());
        }
        return new ArrayList<>();
    }

    /**
     * 分页获取数据 可以执行sql
     * 分页必须传入一个排序字段
     *
     * @param size       每页数量
     * @param current    页码
     * @param sql        需要执行的sql
     * @param orderField 排序字段
     */
    public List<Map<String, Object>> getDataPage(long size, long current, String sql, List<Object> objects, List<OrderField> orderField) {
        if (current <= 0 && orderField.size() == 0) {
            throw new BusinessException("排序字段不能为空");
        }
        if (!orderField.isEmpty()) {
            StringBuilder oderBy = new StringBuilder(" order by ");
            String string = orderField.stream().map(e -> "`" + e.getFieldKey() + "` " + e.getSortType()).collect(Collectors.joining(","));
            oderBy.append(string);
            sql += oderBy;
        }
        if (size > 0) {
            sql = sql + " LIMIT  ?,?";
            objects.add(size * (current - 1));
            objects.add(size);
        }
        log.info("执行的sql 语句为:{},入参为:{}", sql, JSONObject.toJSONString(objects));
        if (!objects.isEmpty()) {
            return this.queryForList(sql, objects.toArray());
        } else {
            return this.queryForList(sql);
        }
    }

    /**
     * 分页获取数据 可以执行sql
     * 分页必须传入一个排序字段
     *
     * @param size    每页数量
     * @param current 页码
     * @param sql     需要执行的sql
     */
    public Page<Map<String, Object>> getDataPage(long size, long current, String sql) {
        List<Object> objects = new ArrayList<>(2);
        String pageSql = "select * from(" + sql + ") as `" + IdGenerator.getIdStr() + "` LIMIT  ?,?";
        objects.add(size * (current - 1));
        objects.add(size);
        log.info("执行的sql 语句为:{},入参为:{}", sql, JSONObject.toJSONString(objects));
        List<Map<String, Object>> list = this.queryForList(pageSql, objects.toArray());
        //获取总条数
        String countSql = "select count(*) from (" + sql + ") as `" + IdGenerator.getIdStr() + "`";
        Long count = this.queryForObject(countSql, Long.class);
        Page<Map<String, Object>> page = new Page<>();
        page.setRecords(list);
        page.setTotal(Optional.ofNullable(count).orElse(0L));
        page.setCurrent(current);
        page.setSize(size);
        return page;
    }


    /**
     * 执行sql
     *
     * @param sql sql语句
     */
    public List<Map<String, Object>> execSql(String sql) {
        return this.queryForList(sql);
    }

    /**
     * 添加排序字段
     *
     * @param sql 执行的sql
     */
    private String addOderBy(String sql, String tableName) {
        List<Map<String, Object>> list = this.queryForList(StrUtil.format(SHOW_COLUMNS, tableName));
        if (list.isEmpty()) {
            sql = sql + " ORDER BY " +
                    "`" + Constant.DORIS_ODS_KEY + "` desc";
        } else {
            sql = sql + " ORDER BY " +
                    "`" + list.get(0).get("Field") + "` desc";
        }
        return sql;
    }


    /**
     * 删除表 不会检查该表是否存在未完成的事务，表将直接被删除并且不能被恢复
     *
     * @param documentName 表名称
     */
    public void dropForce(String documentName) {
        this.execute(StrUtil.format(DROP_SQL_FORCE, documentName));
    }


    /**
     * 删除表 一段时间内，可以通过 RECOVER 语句恢复被删除的表
     *
     * @param documentName 表名称
     */
    public void drop(String documentName) {
        this.execute(StrUtil.format(DROP_SQL, documentName));
    }


    /**
     * 执行sql 添加了一个可以提供入参的方法
     * 此方法只能用于 算子节点 统一添加时间戳的自动写入功能
     *
     * @param sql  sql
     * @param args 入参
     */
    public void executeCustomSql(String sql, final Object... args) {
        log.info("执行的sql语句为:{},入参为:{}", sql, JSONObject.toJSONString(args));
        this.update(sql, args);
    }


    /**
     * 判断某张表是否存在
     *
     * @param tableName 表名称
     * @return 是否存在
     */
    public boolean ifNotExistsTable(String tableName) {
        String sql = SHOW_TABLES + " like '" + tableName + "'";
        List<Map<String, Object>> list = this.queryForList(sql);
        return !list.isEmpty();
    }


    /**
     * 通过表名称关键字 模糊查询 所有表名称
     *
     * @param tableName 表名称
     * @return 是否存在
     */
    public List<String> getTableName(String tableName) {
        String sql = SHOW_TABLES + " like '%" + tableName + "%'";
        List<String> query = this.queryForList(sql, String.class);
        return query;
    }

    /**
     * 插入数据
     *
     * @param fieldList 字段属性
     * @param list      数据
     */
    public void insert(List<Map<String, Object>> list, List<DataSourceField> fieldList, String documentName) {
        StringBuffer insertSql = new StringBuffer();
        DorisConfig dorisConfig = SpringContextUtil.getBean(DorisConfig.class);
        insertSql.append(INSERT_SQL).append(dorisConfig.getLibraryName()).append(".").append(documentName);
        insertSql.append(" ( ");
        //插入字段定义
        fieldList.forEach(e -> insertSql.append("`").append(e.getFieldKey()).append("`").append(" ,"));
        //删除多余的逗号
        insertSql.delete(insertSql.length() - 1, insertSql.length());
        insertSql.append(") VALUES ");
        //插入值语句
        list.forEach(e -> {
            insertSql.append("(");
            fieldList.forEach(x -> {
                Object value = e.get(x.getFieldKey());
                //如果是一个字符串需要把特殊符号转换一下
                if (value instanceof String) {
                    value = value.toString().replace("\"", "\\\"");
                }
                switch (x.getFieldType()) {
                    case STRING:
                    case CHAR:
                    case VARCHAR:
                    case DATETIME:
                    case DATE:
                        //old
//                        insertSql.append("\"").append(value).append("\"");
                        //new
                        insertSql.append("\"").append(Optional.ofNullable(value).orElse(StringPool.EMPTY)).append("\"");
                        break;
                    case JSON:
                        insertSql.append(JSONObject.toJSONString(value));
                        break;
                    case INT:
                    case FLOAT:
                    case BIGINT:
                    case DOUBLE:
                    case BOOLEAN:
                    case DECIMAL:
                    case TINYINT:
                    case LARGEINT:
                    case SMALLINT:
                        insertSql.append(value);
                        break;
                    default:
                        throw new BusinessException("未知类型");
                }
                insertSql.append(",");
            });
            //删除最后的逗号
            insertSql.delete(insertSql.length() - 1, insertSql.length());
            insertSql.append("),");
        });
        /**
         * 删除最后的逗号
         * */
        insertSql.delete(insertSql.length() - 1, insertSql.length());
        insertSql.append(";");
        super.execute(insertSql.toString());
    }

    /**
     * 按批次号进行删除
     *
     * @param documentName
     * @param lotNo
     */
    public void delByLotNo(String documentName, String key, String lotNo) {
        StringBuilder buffer = new StringBuilder();
        DorisConfig dorisConfig = SpringContextUtil.getBean(DorisConfig.class);
        buffer.append("DELETE FROM ")
                .append(dorisConfig.getLibraryName())
                .append(StringPool.DOT)
                .append(documentName).append(" WHERE ")
                .append("`")
                .append(key)
                .append("`")
                .append("=")
                .append(lotNo)
                .append(";");
        String delSql = buffer.toString();
        super.execute(delSql);
    }


}
