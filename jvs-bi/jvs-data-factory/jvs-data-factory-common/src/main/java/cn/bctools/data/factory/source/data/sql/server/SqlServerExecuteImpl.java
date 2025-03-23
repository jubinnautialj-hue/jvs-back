package cn.bctools.data.factory.source.data.sql.server;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.SqlServerClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.PublicConnectDto;
import cn.bctools.data.factory.source.dto.SqlServerConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(value = "sqlServerDataSource")
@Slf4j
public class SqlServerExecuteImpl implements DataSourceExecuteInterface {

    /**
     * 查询当前数据库下所有的表
     */
    public static final String TABLE_QUERY = "SELECT  t.name AS tableName, p.value AS description FROM sys.tables AS t LEFT JOIN sys.extended_properties AS p ON t.object_id = p.major_id AND p.minor_id = 0";

    public static final String TABLE_STRUCTURE = "SELECT COLUMN_NAME AS columnName, DATA_TYPE AS type, CHARACTER_MAXIMUM_LENGTH AS chartLength, NUMERIC_PRECISION AS \"precision\",NUMERIC_SCALE AS \"scale\" ,IS_NULLABLE AS isNullable\n" +
            "FROM INFORMATION_SCHEMA.COLUMNS\n" +
            "WHERE TABLE_NAME = '{}';";

    public static final String TABLE_STRUCTURE_DESC = "SELECT \n" +
            "    col.name AS columnName,\n" +
            "    prop.value AS description\n" +
            "FROM \n" +
            "    sys.columns col\n" +
            "INNER JOIN \n" +
            "    sys.tables obj ON col.object_id = obj.object_id\n" +
            "LEFT JOIN \n" +
            "    sys.extended_properties prop ON col.object_id = prop.major_id\n" +
            "                                AND col.column_id = prop.minor_id\n" +
            "                                AND prop.name = 'MS_Description'\n" +
            "WHERE \n" +
            "    obj.name = '{}'";

    /**
     * 查询当前数据库下所有的模式
     */
    public static final String QUERY_SCHEMAS = "SELECT schema_name FROM information_schema.schemata";

    /**
     * 查询数据 select * from 库.模式.表
     */
    public static final String DATA_QUERY = "SELECT {} FROM {}.{}.{}";

    /**
     * 分页查询数据 select * from 库.模式.表
     */
    public static final String DATA_PAGE_QUERY = "SELECT {} FROM {} ORDER BY 1 OFFSET {} ROWS FETCH NEXT {} ROWS ONLY;";


    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        SqlServerConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(SqlServerConnectDto.class);
        SqlServerClientConfig clientConfig = SqlServerClientConfig.init(publicConnectDto);
        //查询表
        List<SQLServerTable> tables = clientConfig.queryData(TABLE_QUERY).stream().map(e -> BeanUtil.toBean(e, SQLServerTable.class)).collect(Collectors.toList());


        return tables.stream().map(table -> {
            String tableDesc = StrUtil.isNotBlank(table.getDescription()) ? table.getDescription() : table.getTableName();
            DataSourceStructure sourceStructure = new DataSourceStructure()
                    .setName(table.getTableName())
                    .setExecuteName(table.getTableName())
                    .setCheckIs(Boolean.TRUE)
                    .setTableNameDesc(tableDesc);

            //查询列信息
            List<SQLServerStructure> structures = clientConfig.queryData(StrUtil.format(TABLE_STRUCTURE, table.getTableName()))
                    .stream()
                    .map(e -> BeanUtil.toBean(e, SQLServerStructure.class)).collect(Collectors.toList());
            Map<String, String> descMap = clientConfig.queryData(StrUtil.format(TABLE_STRUCTURE_DESC, table.getTableName()))
                    .stream()
                    .map(e -> BeanUtil.toBean(e, ColumnDesc.class))
                    .collect(Collectors.toMap(ColumnDesc::getColumnName, e -> Optional.ofNullable(e.getDescription()).orElse(e.getColumnName())));


            if (CollectionUtil.isNotEmpty(structures)) {
                List<DataSourceStructure.Structure> structureList = structures.stream().map(column -> {
                    String columnName = column.getColumnName();
                    DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                            .setOriginalColumnName(columnName)
                            .setLength(Optional.ofNullable(column.getChartLength()).orElse(column.getPrecision()))
                            .setPrecision(column.getScale())
                            .setColumnCount(descMap.get(columnName))
                            .setDataType(column.getType());
                    fieldTypeEnum(column.getType(), structure);
                    return structure;
                }).collect(Collectors.toList());
                sourceStructure.setStructure(structureList).setFieldCount(structureList.size());
            }
            return sourceStructure;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        SqlServerConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(SqlServerConnectDto.class);
        SqlServerClientConfig clientConfig = SqlServerClientConfig.init(publicConnectDto);
        List<Map<String, Object>> list;
        if (size > 0 && current > 0) {
            Long offset = size * (current - 1);
            list = clientConfig.queryData(StrUtil.format(DATA_PAGE_QUERY, "*",dataSourceStructure.getExecuteName(), offset, size));
        } else {
            list = clientConfig.queryData(StrUtil.format(DATA_QUERY, "*", publicConnectDto.getSourceLibraryName(), publicConnectDto.getSchema(), dataSourceStructure.getExecuteName()));
        }
        return new Page<Map<String, Object>>()
                .setTotal(clientConfig.getTotalCount(publicConnectDto, dataSourceStructure.getExecuteName()))
                .setSize(size)
                .setCurrent(current)
                .setRecords(list);
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        SqlServerConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(SqlServerConnectDto.class);
        SqlServerClientConfig clientConfig = SqlServerClientConfig.init(publicConnectDto);
        return clientConfig.getTotalCount(publicConnectDto, dataSourceStructure.getExecuteName());
    }


    @Override
    public void check(String json) {
        try {
            DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
            SqlServerConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(SqlServerConnectDto.class);
            String sql = "select 1";
            SqlServerClientConfig clientConfig = SqlServerClientConfig.init(publicConnectDto);
            clientConfig.queryData(sql);
        } catch (Exception exception) {
            log.info("校验mysql链接错误", exception);
            throw new BusinessException(exception.getMessage());
        }
    }

    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        String type = StrUtil.toString(value);
        switch (type) {
            case "date":
                structure.setFormat("yyyy-MM-dd");
                break;
            case "datetime":
            case "datetime2":
                structure.setFormat("yyyy-MM-dd HH:mm:ss");
                break;
            case "smalldatetime":
                structure.setFormat("yyyy-MM-dd HH:mm");
                break;
            default:

        }
    }

    @Override
    public Function<? extends CreateDataXJsonParameterPo, JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "sqlserverreader");
            JSONObject parameter = new JSONObject();
            parameter.put("column", column);
            //连接信息
            JSONObject connection = new JSONObject();
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);

            String url = StrUtil.format(SqlServerClientConfig.URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            if (dataSource.getSize() > 0L) {
                String columnField = String.join(",", column);
                String querySql = StrUtil.format(DATA_PAGE_QUERY, columnField, dataSource.getDataSourceStructure().getExecuteName(), 0, dataSource.getSize());
                connection.put("querySql", Collections.singletonList(querySql));
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

    /**
     * 获取某个用户下面的 所有模式
     */
    public List<String> getSchema(SqlServerConnectDto publicConnectDto) {
        try {
            //获取模式的时候不需要数据库名称
            publicConnectDto.setSourceLibraryName(null);
            SqlServerClientConfig clientConfig = SqlServerClientConfig.init(publicConnectDto);
            List<Map<String, Object>> list = clientConfig.queryData(QUERY_SCHEMAS);
            if (list.isEmpty()) {
                return new ArrayList<>();
            }
            return list.stream().map(e -> e.getOrDefault("schema_name", "").toString()).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("获取模式错误", e);
            throw new BusinessException("获取模式错误:{}", e.getMessage());
        }
    }

    @Data
    @Accessors(chain = true)
    public static class SQLServerStructure {
        /**
         * 字段名称
         */
        private String columnName;

        /**
         * 字段类型
         */
        private String type;

        /**
         * 字段长度 字符
         */
        private Integer chartLength;

        /**
         * 字段长度 数字
         */
        private Integer precision;

        /**
         * 字段小数位 数字
         */
        private Integer scale;

        /**
         * 是否为空
         */
        private String isNullable;
    }

    @Data
    @Accessors(chain = true)
    public static class ColumnDesc {
        /**
         * 字段名称
         */
        private String columnName;

        /**
         * 字段描述
         */
        private String description;

    }

    @Data
    @Accessors(chain = true)
    public static class SQLServerTable {
        private String tableName;
        private String description;
    }
}
