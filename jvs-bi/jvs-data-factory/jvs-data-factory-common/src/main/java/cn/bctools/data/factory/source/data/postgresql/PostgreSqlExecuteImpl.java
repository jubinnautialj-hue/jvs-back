package cn.bctools.data.factory.source.data.postgresql;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.PostgreSqlClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.PostgreSqlConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.util.FieldUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * pgsql 实现类
 *
 * @author admin
 */
@Component(value = "pgsqlDataSource")
@Slf4j
public class PostgreSqlExecuteImpl implements DataSourceExecuteInterface {
    /**
     * 连接信息
     */
    private static final String URL = "jdbc:postgresql://{}:{}/{}?currentSchema={}";

    /**
     * 获取整个模式的表名称
     */
    private static final String GET_TABLE_STRUCTURE_MYSQL = "SELECT c.relname AS table_name, obj_description(c.oid, 'pg_class') AS table_comment FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = '{}' AND c.relkind = 'r';";

    /**
     * 获取整个库的所有模式  会排查默认的模式
     * pg_toast 模式包含了 PostgreSQL 表中存储大对象的相关数据。
     * pg_catalog 模式包含了 PostgreSQL 系统目录中的元数据信息，例如系统表、系统视图和系统函数等。
     * information_schema 模式包含了关于数据库对象（表、列、视图等）的标准化元数据信息。
     */
    private static final String GET_SCHEMA = "SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT IN ('pg_toast', 'pg_catalog', 'information_schema');";

    /**
     * 获取表描述
     */
    private static final String GET_TABLE_DESCRIBE = "SELECT\tattname AS COLUMN_NAME,\tformat_type ( atttypid, atttypmod ) AS data_type,\tcol_description ( attrelid, attnum ) AS column_comment FROM pg_attribute WHERE attrelid = '{}' :: REGCLASS AND attnum > 0 AND NOT attisdropped;";

    /**
     * 获取数据
     */
    private static final String FIND_ALL_MYSQL = "select *  from {}";

    @Override
    @SneakyThrows
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        PostgreSqlConnectDto postgreSqlConnectDto = dataSource.getCustomJson().toJavaObject(PostgreSqlConnectDto.class);
        //初始化客户端
        PostgreSqlClientConfig postgreSqlClientConfig = PostgreSqlClientConfig.init(postgreSqlConnectDto, 15000);
        //获取所有表
        String tableSql = StrUtil.format(GET_TABLE_STRUCTURE_MYSQL, postgreSqlConnectDto.getSchema());
        List<Map<String, Object>> tableNameList = postgreSqlClientConfig.queryData(tableSql);
        //获取表的信息
        List<DataSourceStructure> dataSourceStructures = tableNameList.stream().map(e -> {
            String tableName = e.get("table_name").toString();
            //获取表信息
            String tableDescribeSql = StrUtil.format(GET_TABLE_DESCRIBE, tableName);
            List<Map<String, Object>> list = postgreSqlClientConfig.queryData(tableDescribeSql);
            Object tableComment = e.get("table_comment");
            tableComment = ObjectUtil.isNull(tableComment) ? tableName : tableComment;
            DataSourceStructure sourceStructure = new DataSourceStructure()
                    .setName(tableName)
                    .setExecuteName(tableName)
                    .setCheckIs(Boolean.TRUE)
                    .setTableNameDesc(StrUtil.toString(tableComment));
            List<DataSourceStructure.Structure> structures = list.stream().map(v -> {
                String columnCount = StrUtil.toStringOrNull(v.get("column_comment"));
                if (StrUtil.isBlank(columnCount)) {
                    columnCount = v.get("column_name").toString();
                }
                DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                        .setOriginalColumnName(v.get("column_name").toString())
                        .setColumnCount(columnCount)
                        .setDataType(v.get("data_type").toString());
                fieldTypeEnum(v.get("data_type"), structure);
                return structure;
            }).collect(Collectors.toList());
            sourceStructure.setStructure(structures).setFieldCount(structures.size());
            return sourceStructure;
        }).collect(Collectors.toList());
        return dataSourceStructures;
    }

    @Override
    public Function<? extends CreateDataXJsonParameterPo, com.alibaba.fastjson2.JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            com.alibaba.fastjson2.JSONObject jsonObject = new com.alibaba.fastjson2.JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "postgresqlreader");
            com.alibaba.fastjson2.JSONObject parameter = new com.alibaba.fastjson2.JSONObject();
            //连接信息
            com.alibaba.fastjson2.JSONObject connection = new com.alibaba.fastjson2.JSONObject();
            PostgreSqlConnectDto postgreSqlConnectDto = dataSource.getCustomJson().toJavaObject(PostgreSqlConnectDto.class);
            String url = StrUtil.format(URL, postgreSqlConnectDto.getSourceHost(), postgreSqlConnectDto.getSourcePort(), postgreSqlConnectDto.getSourceLibraryName(), postgreSqlConnectDto.getSchema());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            StringBuffer sql = new StringBuffer();
            String columnField = column.stream().collect(Collectors.joining(","));
            sql.append("select ")
                    .append(columnField)
                    .append(" from ")
                    .append(dataSource.getDataSourceStructure().getExecuteName());
            if (dataSource.getSize() > 0L) {
                sql.append(" LIMIT 10000")
                        .append(" OFFSET 0 ")
                        .append(";");
            }
            connection.put("querySql", Collections.singletonList(sql));
            parameter.put("connection", Collections.singletonList(connection));
            parameter.put("username", postgreSqlConnectDto.getSourceUserName());
            parameter.put("password", postgreSqlConnectDto.getSourcePwd());
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        PostgreSqlConnectDto postgreSqlConnectDto = dataSource.getCustomJson().toJavaObject(PostgreSqlConnectDto.class);
        try {
            PostgreSqlClientConfig postgreSqlClientConfig = PostgreSqlClientConfig.init(postgreSqlConnectDto, 15000);
            String sql = StrUtil.format(FIND_ALL_MYSQL, dataSourceStructure.getExecuteName());
            List<Map<String, Object>> list;
            //如果入参不为空 则拼接入参
            //分页查询
            if (size > 0) {
                sql += " LIMIT " + size + " OFFSET  " + size * (current - 1);
            }
            list = postgreSqlClientConfig.queryData(sql);

            return new Page<Map<String, Object>>()
                    .setTotal(this.getCount(dataSource, dataSourceStructure))
                    .setSize(size)
                    .setCurrent(current)
                    .setRecords(list);
        } catch (Exception exception) {
            log.info("查询数据错误", exception);
            throw new BusinessException("获取pgsql数据错误【{}】", exception.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        PostgreSqlConnectDto postgreSqlConnectDto = dataSource.getCustomJson().toJavaObject(PostgreSqlConnectDto.class);
        PostgreSqlClientConfig postgreSqlClientConfig = PostgreSqlClientConfig.init(postgreSqlConnectDto, 15000);
        String sql = StrUtil.format(FIND_ALL_MYSQL, dataSourceStructure.getExecuteName());
        return postgreSqlClientConfig.getTotalCount(sql);
    }

    @Override
    public void check(String json) {
        DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
        PostgreSqlConnectDto postgreSqlConnectDto = dataSource.getCustomJson().toJavaObject(PostgreSqlConnectDto.class);
        try {
            PostgreSqlClientConfig postgreSqlClientConfig = PostgreSqlClientConfig.init(postgreSqlConnectDto, 10000);
            postgreSqlClientConfig.queryData("select 1");
        } catch (Exception e) {
            log.info("校验数据库链接报错,校验不通过:{}", e.getMessage());
            throw new BusinessException("校验数据库链接报错,校验不通过:{}", e.getMessage());
        }
    }


    /**
     * 获取某个库下面的 所有模式
     */
    public List<String> getSchema(PostgreSqlConnectDto postgreSqlConnectDto) {
        try {
            PostgreSqlClientConfig postgreSqlClientConfig = PostgreSqlClientConfig.init(postgreSqlConnectDto, 10000);
            String sql = StrUtil.format(GET_SCHEMA, postgreSqlConnectDto.getSourceLibraryName());
            List<Map<String, Object>> list = postgreSqlClientConfig.queryData(sql);
            if (list.isEmpty()) {
                return new ArrayList<>();
            }
            return list.stream().map(e -> e.getOrDefault("schema_name", "").toString()).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("获取模式错误", e);
            throw new BusinessException("获取模式错误:{}", e.getMessage());
        }

    }

    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        String v = (String) value;
        String format = FieldUtil.subStr(v);
        String s1 = "(";
        String typeName = v.contains(s1) ? v.substring(0, v.lastIndexOf(s1)) : v;
        typeName = typeName.toLowerCase();
        structure.setDataType(typeName);
        switch (typeName) {
            case "time without time zone":
            case "time with time zone":
                structure.setLength(16);
                break;
            case "timestamp without time zone":
            case "timestamp with time zone":
                structure.setFormat("yyyy-MM-dd HH:mm:ss.SSSSSS")
                        .setLength(6);
                break;
            case "timestamp":
            case "timestamptz":
                structure.setFormat("yyyy-MM-dd HH:mm:ss.SSSSSS")
                        .setLength(6);
                break;
            case "numeric":
                String[] split = {"18", "5"};
                if (StrUtil.isNotBlank(format)) {
                    split = format.split(",");
                }
                structure.setLength(Integer.valueOf(split[0]));
                structure.setPrecision(Integer.valueOf(split[1]));
                break;
            case "date":
                structure.setFrom("yyyy-MM-dd");
                break;
            case "varchar":
            case "character varying":
                Integer i = 300;
                if (format != null) {
                    i = Integer.valueOf(format) * 3;
                }
                if (i <= 65533) {
                    structure.setLength(i);
                } else {
                    structure.setDataType("text");
                }
                break;
            case "char":
            case "character":
                Integer i1 = Integer.valueOf(format) * 3;
                structure.setLength(i1);
                if (i1 > 255) {
                    structure.setDataType("varchar");
                }
                break;
            default:
                structure.setFormat(format);
        }
    }
}
