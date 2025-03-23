package cn.bctools.data.factory.source.data.oracle;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.OracleClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.OracleConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.util.FieldUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author admin
 */
@Component(value = "oracleDataSource")
@Slf4j
public class OracleDataSourceExecuteImpl implements DataSourceExecuteInterface {
    /**
     * 连接信息
     */
    private static final String URL = "jdbc:oracle:thin:@{}:{}:{}";

    /**
     * 获取整个模式数据表名称与解释
     */
    private static final String GET_TABLE = "SELECT all_tables.table_name as table_name, all_tab_comments.comments as comments FROM all_tables LEFT JOIN all_tab_comments ON all_tables.owner = all_tab_comments.owner AND all_tables.table_name = all_tab_comments.table_name WHERE all_tables.owner = '{}'";

    /**
     * 获取表描述
     */
    private static final String GET_TABLE_DESCRIBE = "SELECT col.*, com.comments " +
            "FROM all_tab_columns col LEFT JOIN ( SELECT owner, table_name, column_name, comments FROM all_col_comments WHERE comments IS NOT NULL" +
            ") com ON col.owner = com.owner AND col.table_name = com.table_name AND col.column_name = com.column_name WHERE col.table_name = '{}' ORDER BY col.column_id";

    /**
     * 获取数据
     */
    private static final String FIND_ALL_ORACLE = "select * from \"{}\".\"{}\"";

    /**
     * 获取数据分页
     */
    private static final String FIND_ALL_ORACLE_PAGE = "select a.* from ( select t.*,rownum from \"{}\".\"{}\" t where rownum <= {} ) a where rownum >= {}";
    /**
     * datax querySql 语句
     */
    private static final String DATA_X_QUERY_PAGE_SQL = "select {} from ( select t.*,rownum from \"{}\".\"{}\" t where rownum <= {} ) a where rownum >= {}";
    /**
     * datax querySql 语句 查询所有数据
     */
    private static final String DATA_X_QUERY_SQL = " select {} from \"{}\".\"{}\" ";
    /**
     * 获取可以操作的模式
     */
    private static final String GET_SCHEMA = "SELECT username FROM all_users";


    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        try {
            OracleConnectDto oracleConnectDto = dataSource.getCustomJson().toJavaObject(OracleConnectDto.class);
            OracleClientConfig oracleClientConfig = OracleClientConfig.init(oracleConnectDto, 15000);
            return oracleClientConfig.getTotalCount(dataSourceStructure.getExecuteName(), oracleConnectDto.getSchema());
        } catch (Exception e) {
            log.info("查询数据错误", e);
            throw new BusinessException("获取mysql条数错误");
        }
    }

    @Override
    public Function<? extends CreateDataXJsonParameterPo, com.alibaba.fastjson2.JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            com.alibaba.fastjson2.JSONObject jsonObject = new com.alibaba.fastjson2.JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "oraclereader");
            com.alibaba.fastjson2.JSONObject parameter = new com.alibaba.fastjson2.JSONObject();
            parameter.put("column", column);
            //连接信息
            com.alibaba.fastjson2.JSONObject connection = new com.alibaba.fastjson2.JSONObject();
            OracleConnectDto oracleConnectDto = dataSource.getCustomJson().toJavaObject(OracleConnectDto.class);
            String serverName = oracleConnectDto.getServerName();
            if (StrUtil.isBlank(serverName)) {
                serverName = oracleConnectDto.getSid();
            }
            String url = StrUtil.format(URL, oracleConnectDto.getSourceHost(), oracleConnectDto.getSourcePort(), serverName);
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            if (dataSource.getSize() > 0L) {
                StringBuffer columnBuffer = new StringBuffer();
                column.forEach(e -> columnBuffer.append("a.\"").append(e).append("\","));
                //删除多余的逗号
                columnBuffer.delete(columnBuffer.length() - 1, columnBuffer.length());
                String sql = StrUtil.format(DATA_X_QUERY_PAGE_SQL, columnBuffer.toString(), oracleConnectDto.getSchema(), dataSource.getDataSourceStructure().getExecuteName(), 10000, 0);
                connection.put("querySql", Collections.singletonList(sql));
            } else {
                //todo  这里因为服务名里面存在多个数据库实例 导致datax 报错
                String columnField = String.join("\",\"", column);
                columnField = "\"" + columnField + "\"";
                String sql = StrUtil.format(DATA_X_QUERY_SQL, columnField, oracleConnectDto.getSchema(), dataSource.getDataSourceStructure().getExecuteName());
                connection.put("querySql", Collections.singletonList(sql));
            }
            parameter.put("connection", Collections.singletonList(connection));
            parameter.put("username", oracleConnectDto.getSourceUserName());
            parameter.put("password", oracleConnectDto.getSourcePwd());
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }

    @Override
    public void check(String json) {
        try {
            DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
            OracleConnectDto oracleConnectDto = dataSource.getCustomJson().toJavaObject(OracleConnectDto.class);
            String sql = "SELECT version FROM v$instance";
            OracleClientConfig clientConfig = OracleClientConfig.init(oracleConnectDto, 10000);
            clientConfig.queryData(sql);
        } catch (Exception exception) {
            throw new BusinessException(exception.getMessage());
        }
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        try {
            OracleConnectDto oracleConnectDto = dataSource.getCustomJson().toJavaObject(OracleConnectDto.class);
            OracleClientConfig oracleClientConfig = OracleClientConfig.init(oracleConnectDto, 20000);
            String sql = StrUtil.format(FIND_ALL_ORACLE, oracleConnectDto.getSchema(), dataSourceStructure.getExecuteName());

            List<Map<String, Object>> list;

            //分页查询
            if (size > 0 && current > 0) {
                long skip = size * (current - 1);
                long start = skip * size;
                long end = current * size;
                sql = StrUtil.format(FIND_ALL_ORACLE_PAGE, oracleConnectDto.getSchema(), dataSourceStructure.getExecuteName(), end, start);
            }
            list = oracleClientConfig.queryData(sql);


            return new Page<Map<String, Object>>()
                    .setTotal(this.getCount(dataSource, dataSourceStructure))
                    .setSize(size)
                    .setCurrent(current)
                    .setRecords(list)
                    ;
        } catch (Exception e) {
            log.info("查询数据错误", e);
            throw new BusinessException("获取oracle数据错误【{}】", e.getMessage());
        }
    }


    /**
     * 获取某个用户下面的 所有模式
     */
    public List<String> getSchema(OracleConnectDto oracleConnectDto) {
        try {
            OracleClientConfig oracleClientConfig = OracleClientConfig.init(oracleConnectDto, 10000);
            List<Map<String, Object>> list = oracleClientConfig.queryData(GET_SCHEMA);
            if (list.isEmpty()) {
                return new ArrayList<>();
            }
            return list.stream().map(e -> e.getOrDefault("USERNAME", "").toString()).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("获取模式错误", e);
            throw new BusinessException("获取模式错误:{}", e.getMessage());
        }
    }

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        try {
            OracleConnectDto oracleConnectDto = dataSource.getCustomJson().toJavaObject(OracleConnectDto.class);
            OracleClientConfig oracleClientConfig = OracleClientConfig.init(oracleConnectDto, 15000);
            //获取所有表
            String tableSql = StrUtil.format(GET_TABLE, oracleConnectDto.getSchema());
            List<Map<String, Object>> tableNameList = oracleClientConfig.queryData(tableSql);
            //获取表的信息
            return tableNameList.stream().map(e -> {
                String tableName = e.get("TABLE_NAME").toString();
                //获取表信息
                String tableDescribeSql = StrUtil.format(GET_TABLE_DESCRIBE, tableName);
                List<Map<String, Object>> list = oracleClientConfig.queryData(tableDescribeSql);

                String comments = Optional.ofNullable(e.get("COMMENTS")).map(StrUtil::toString).orElse(tableName);
                DataSourceStructure sourceStructure = new DataSourceStructure()
                        .setName(tableName)
                        .setExecuteName(tableName)
                        .setCheckIs(Boolean.TRUE)
                        .setTableNameDesc(comments);
                List<DataSourceStructure.Structure> structures = list.stream().map(v -> {
                    String columnCount = StrUtil.toStringOrNull(v.get("COMMENTS"));
                    if (StrUtil.isBlank(columnCount)) {
                        columnCount = StrUtil.toString(v.get("COLUMN_NAME"));
                    }
                    String dataType = StrUtil.toString(v.get("DATA_TYPE"));
                    DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                            .setOriginalColumnName(StrUtil.toString(v.get("COLUMN_NAME")))
                            .setColumnCount(columnCount)
                            .setDataType(dataType);
                    //获取长度与精度
                    if (v.get("DATA_PRECISION") != null && v.get("DATA_SCALE") != null) {
                        String format = v.get("DATA_PRECISION") + "," + v.get("DATA_SCALE");
                        structure.setFormat(format);
                    } else if ("NUMBER".equals(dataType)) {
                        structure.setFormat("18,0");
                    }
                    fieldTypeEnum(v.get("DATA_TYPE"), structure);
                    return structure;
                }).collect(Collectors.toList());
                sourceStructure.setStructure(structures);
                return sourceStructure;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("ORACLE 表结构错误", e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        String v = (String) value;
        String format = FieldUtil.subStr(v);
        String s1 = "(";
        String typeName = v.contains(s1) ? v.substring(0, v.lastIndexOf(s1)) : v;
        typeName = typeName.toLowerCase();
        switch (typeName){
            case "timestamp":
            case "timestamp with time zone":
            case "timestamp with local time zone":
                structure.setFormat("yyyy-MM-dd HH:mm:ss");
                break;
            case "date":
                structure.setFrom("yyyy-MM-dd");
                break;
            case "varchar2":
            case "char":
            case "nchar":
            case "nvarchar2":
                if(StrUtil.isNotBlank(format)){
                    BigDecimal multiply = new BigDecimal(format).multiply(new BigDecimal(4));
                    format = multiply.toString();
                }
                structure.setFormat(format);
                break;
            default:
                structure.setFormat(format);
        }
    }
}
