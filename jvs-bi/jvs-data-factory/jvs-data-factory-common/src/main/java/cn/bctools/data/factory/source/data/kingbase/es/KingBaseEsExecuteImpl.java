package cn.bctools.data.factory.source.data.kingbase.es;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.KingBaseClientConfig;
import cn.bctools.data.factory.source.data.config.PostgreSqlClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.KingBaseConnectDto;
import cn.bctools.data.factory.source.dto.PostgreSqlConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.util.FieldUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Component(value = "kingbaseDataSource")
@Slf4j
public class KingBaseEsExecuteImpl implements DataSourceExecuteInterface {

    /**
     * 获取整个模式的表名称
     */
    private static final String GET_TABLE_STRUCTURE_MYSQL = "SELECT c.relname AS table_name, obj_description(c.oid, 'pg_class') AS table_comment FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = '{}' AND c.relkind = 'r';";

    /**
     * 获取表描述
     */
    private static final String GET_TABLE_DESCRIBE = "SELECT A.attname AS COLUMN_NAME,regexp_replace(pg_catalog.format_type(A.atttypid, A.atttypmod), '\\(\\d+(,\\d+)?\\)', '') AS data_type,pd.description AS column_comment FROM pg_catalog.pg_attribute AS A JOIN pg_catalog.pg_class AS C ON A.attrelid = C.oid LEFT JOIN pg_catalog.pg_namespace AS ns ON ns.oid = C.relnamespace LEFT JOIN pg_catalog.pg_description AS pd ON (C.oid = pd.objoid AND A.attnum = pd.objsubid) WHERE C.relname = '{}' AND A.attnum > 0 AND NOT A.attisdropped ORDER BY A.attnum;";

    /**
     * 获取数据
     */
    private static final String FIND_ALL_KINGBASE = "select *  from {}";

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
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        KingBaseConnectDto kingBaseConnectDto = dataSource.getCustomJson().toJavaObject(KingBaseConnectDto.class);
        try {
            KingBaseClientConfig kingBaseClientConfig = KingBaseClientConfig.init(kingBaseConnectDto);
            String sql = StrUtil.format(FIND_ALL_KINGBASE, dataSourceStructure.getExecuteName());

            List<Map<String, Object>> list;
            //分页查询
            if (size > 0) {
                sql += " LIMIT " + size + " OFFSET  " + size * (current - 1);
            }
            list = kingBaseClientConfig.queryData(sql);


            return new Page<Map<String, Object>>()
                    .setTotal(this.getCount(dataSource, dataSourceStructure))
                    .setSize(size)
                    .setCurrent(current)
                    .setRecords(list);
        } catch (Exception exception) {
            log.info("查询数据错误", exception);
            String err = StrUtil.format("获取人大金仓数据错误【{}】", exception.getMessage());
            throw new BusinessException(err);
        }
    }

    @Override
    @SneakyThrows
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        KingBaseConnectDto connectDto = dataSource.getCustomJson().toJavaObject(KingBaseConnectDto.class);
        KingBaseClientConfig kingBaseClientConfig = KingBaseClientConfig.init(connectDto);
        String sql = StrUtil.format(FIND_ALL_KINGBASE, dataSourceStructure.getExecuteName());
        return kingBaseClientConfig.getTotalCount(sql);
    }

    @Override
    public void check(String json) {
        DataSource dataSource = com.alibaba.fastjson.JSONObject.parseObject(json, DataSource.class);
        PostgreSqlConnectDto postgreSqlConnectDto = dataSource.getCustomJson().toJavaObject(PostgreSqlConnectDto.class);
        try {
            PostgreSqlClientConfig postgreSqlClientConfig = PostgreSqlClientConfig.init(postgreSqlConnectDto, 10000);
            postgreSqlClientConfig.queryData("select 1");
        } catch (Exception e) {
            log.info("校验数据库链接报错,校验不通过:{}", e.getMessage());
            String err = StrUtil.format("校验数据库链接报错,校验不通过:{}", e.getMessage());
            throw new BusinessException(err);
        }
    }

    /**
     * 获取某个库下面的 所有模式
     */
    public List<String> getSchema(KingBaseConnectDto connectDto) {
        try {
            List<Map<String, Object>> schema = KingBaseClientConfig.getSchema(connectDto);
            return schema.stream().map(e -> e.getOrDefault("schema_name", "").toString()).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("获取模式错误", e);
            String err = StrUtil.format("获取模式错误:{}", e.getMessage());
            throw new BusinessException(err);
        }

    }

    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        String v = (String) value;
        String format = FieldUtil.subStr(v);
        String s1 = "(";
        String typeName = v.contains(s1) ? v.substring(0, v.lastIndexOf(s1)) : v;
        typeName = typeName.toLowerCase();
        switch (typeName) {
            case "timestamp":
                structure.setFormat("yyyy-MM-dd HH:mm:ss");
                break;
            case "date":
                structure.setFrom("yyyy-MM-dd");
                break;
            case "varchar":
            case "char":
            case "character":
            case "character varying":
            case "varchar2":
                if (StrUtil.isNotBlank(format)) {
                    BigDecimal multiply = new BigDecimal(format).multiply(new BigDecimal(4));
                    format = multiply.toString();
                }
                structure.setFormat(format);
                break;
            default:
                structure.setFormat(format);
        }
    }

    @Override
    public Function<? extends CreateDataXJsonParameterPo, JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "kingbasees90reader");
            JSONObject parameter = new JSONObject();
            parameter.put("column", column);
            //连接信息
            JSONObject connection = new JSONObject();
            KingBaseConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(KingBaseConnectDto.class);
            String url = StrUtil.format(KingBaseClientConfig.URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName(), publicConnectDto.getSchema());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            if (dataSource.getSize() > 0L) {
                StringBuffer sql = new StringBuffer();
                String columnField = column.stream().collect(Collectors.joining(","));
                sql.append("select ")
                        .append(columnField)
                        .append(" from ")
                        .append(dataSource.getDataSourceStructure().getExecuteName())
                        .append(" limit 0,")
                        .append(dataSource.getSize())
                        .append(";");
                connection.put("querySql", Collections.singletonList(sql));
            } else {
                connection.put("table", Collections.singletonList(dataSource.getDataSourceStructure().getExecuteName()));
            }
            parameter.put("connection", Collections.singletonList(connection));
            parameter.put("username", publicConnectDto.getSourceUserName());
            parameter.put("password", publicConnectDto.getSourcePwd());
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }
}
