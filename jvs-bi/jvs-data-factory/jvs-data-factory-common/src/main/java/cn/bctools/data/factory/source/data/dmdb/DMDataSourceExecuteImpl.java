package cn.bctools.data.factory.source.data.dmdb;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.DMClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.DMConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.util.FieldUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component(value = "dMDataSource")
public class DMDataSourceExecuteImpl implements DataSourceExecuteInterface {

    private static final String GET_ALL_TABLE_STRUCTURE_COMMENTS = "SELECT TABLE_NAME,COLUMN_NAME,COMMENTS FROM SYS.ALL_COL_COMMENTS WHERE SCHEMA_NAME = '{}'";

    private static final String GET_ALL_TABLE_COMMENTS = "SELECT TABLE_NAME,COMMENTS FROM SYS.ALL_TAB_COMMENTS WHERE OWNER = '{}'";

    private static final String GET_ALL_TABLE_STRUCTURE = "SELECT T.TABLE_NAME,C.COLUMN_NAME,C.DATA_TYPE,C.DATA_LENGTH,C.DATA_SCALE,C.NULLABLE FROM SYS.ALL_TAB_COLUMNS C JOIN SYS.ALL_TABLES T ON C.TABLE_NAME = T.TABLE_NAME WHERE T.OWNER = '{}'";

    private static final String GET_SCHEMA = "SELECT DISTINCT object_name FROM SYS.ALL_OBJECTS WHERE OBJECT_TYPE = 'SCH'";

    private static final String FIND_ALL = "SELECT * FROM {}";
    /**
     * 连接信息
     */
    private static final String URL = "jdbc:dm://{}:{}/{}";


    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        try {
            DMConnectDto connectDto = dataSource.getCustomJson().toJavaObject(DMConnectDto.class);
            DMClientConfig clientConfig = DMClientConfig.init(connectDto, 10000);
            //获取所有的表注释
            Map<String, String> tableComments = clientConfig.queryDataFormat(GET_ALL_TABLE_COMMENTS, connectDto.getSchema()).stream().collect(Collectors.toMap(e -> StrUtil.toString(e.get("TABLE_NAME")), e -> {
                Object comments = e.get("COMMENTS");
                if (ObjectUtil.isNull(comments)) {
                    comments = e.get("TABLE_NAME");
                }
                return StrUtil.toStringOrNull(comments);
            }));
            //获取表结构
            Map<String, List<Map<String, Object>>> tableStructure = clientConfig.queryDataFormat(GET_ALL_TABLE_STRUCTURE, connectDto.getSchema()).stream().collect(Collectors.groupingBy(e -> StrUtil.toString(e.get("TABLE_NAME"))));

            //获取字段解释
            Map<String, String> columnComments = clientConfig.queryDataFormat(GET_ALL_TABLE_STRUCTURE_COMMENTS, connectDto.getSchema())
                    .stream()
                    .collect(Collectors.toMap(e -> StrUtil.toString(e.get("TABLE_NAME")) + e.get("COLUMN_NAME"), e -> {
                        Object comments = e.get("COMMENTS");
                        if (ObjectUtil.isNull(comments)) {
                            comments = e.get("COLUMN_NAME");
                        }
                        return StrUtil.toStringOrNull(comments);
                    }));

            return tableStructure.keySet().stream().map(e -> {

                DataSourceStructure sourceStructure = new DataSourceStructure()
                        .setName(e)
                        .setExecuteName(e)
                        .setCheckIs(Boolean.TRUE)
                        .setTableNameDesc(tableComments.get(e));

                List<DataSourceStructure.Structure> collect = tableStructure.get(e)
                        .parallelStream()
                        .map(v -> {
                            String columnName = StrUtil.toString(v.get("COLUMN_NAME"));
                            String columnComment = columnComments.get(e + columnName);
                            String dataType = StrUtil.toString(v.get("DATA_TYPE"));
                            DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                                    .setOriginalColumnName(columnName)
                                    .setColumnCount(columnComment)
                                    .setDataType(dataType);
                            fieldTypeEnum(dataType, structure);
                            //设置格式
                            this.setFormat(structure, v.get("DATA_LENGTH").toString(), v.get("DATA_SCALE").toString());
                            return structure;
                        })
                        .collect(Collectors.toList());
                return sourceStructure.setStructure(collect).setFieldCount(collect.size());
            }).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("达梦数据库同步表结构异常【{}】", e.getMessage());
        }
    }

    /**
     * 设置格式
     *
     * @param structure  结构数据
     * @param dataLength 长度
     * @param dataScale  小数位数 或者时间毫秒的精度
     */
    private void setFormat(DataSourceStructure.Structure structure, String dataLength, String dataScale) {
        String format = null;
        switch (structure.getDataType()) {
            case "NUMERIC":
            case "DEC":
            case "DECIMAL":
                structure.setLength(Integer.valueOf(dataLength))
                        .setPrecision(Integer.valueOf(dataScale));
                break;
            case "TIMESTAMP":
            case "DATETIME WITH TIME ZONE":
                format = "yyyy-MM-dd HH:mm:ss";
                int integer = Integer.parseInt(dataScale);
                if (integer > 0) {
                    format = format + ".";
                    for (int i = 0; i < integer; i++) {
                        format = format + "S";
                    }
                }
                structure.setLength(integer);
                break;
            case "TIMESTAMP WITH LOCAL TIME ZONE":
                format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
                structure.setLength(6);
                break;
            case "TIME":
            case "TIME WITH TIME ZONE":
                format = "HH:mm:ss";
                int dataScaleInt = Integer.parseInt(dataScale);
                if (dataScaleInt > 0) {
                    format = format + ".";
                    for (int i = 0; i < dataScaleInt; i++) {
                        format = format + "S";
                    }
                }
                break;
            default:
        }
        structure.setFormat(format);
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        DMConnectDto connectDto = dataSource.getCustomJson().toJavaObject(DMConnectDto.class);
        try {
            DMClientConfig clientConfig = DMClientConfig.init(connectDto, 10000);

            List<Map<String, Object>> originTableData;
            String sql = StrUtil.format(FIND_ALL, dataSourceStructure.getExecuteName());
            //分页查询
            if (size > 0) {
                long skip = size * (current - 1);
                sql += " LIMIT " + skip + "," + size;
            }
            originTableData = clientConfig.queryData(sql);
            List<Map<String, Object>> list = originTableData.stream()
                    .map(e -> {
                        //处理时间戳
                        Map<String, Object> map = new HashMap<>();
                        e.keySet().forEach(v -> {
                            Object value = e.get(v);
                            map.put(v, value);
                        });
                        return map;
                    }).collect(Collectors.toList());
            return new Page<Map<String, Object>>()
                    .setTotal(this.getCount(dataSource, dataSourceStructure))
                    .setSize(size)
                    .setCurrent(current)
                    .setRecords(list);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("获取达梦数据库数据错误【{}】", e.getMessage());
        }
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        try {
            DMConnectDto connectDto = dataSource.getCustomJson().toJavaObject(DMConnectDto.class);
            DMClientConfig clientConfig = DMClientConfig.init(connectDto, 10000);
            // 执行查询并获取结果集
            return clientConfig.getTotalCount(dataSourceStructure.getExecuteName());
        } catch (Exception e) {
            log.info("查询数据错误", e);
            throw new BusinessException("获取达梦条数错误");
        }
    }

    @Override
    public Function<? extends CreateDataXJsonParameterPo, com.alibaba.fastjson2.JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            com.alibaba.fastjson2.JSONObject jsonObject = new com.alibaba.fastjson2.JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "rdbmsreader");
            com.alibaba.fastjson2.JSONObject parameter = new com.alibaba.fastjson2.JSONObject();
            parameter.put("column", column);
            //连接信息
            com.alibaba.fastjson2.JSONObject connection = new com.alibaba.fastjson2.JSONObject();
            DMConnectDto dmConnectDto = dataSource.getCustomJson().toJavaObject(DMConnectDto.class);
            String url = StrUtil.format(URL, dmConnectDto.getSourceHost(), dmConnectDto.getSourcePort(), dmConnectDto.getSchema());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            StringBuffer sql = new StringBuffer();
            String columnField = column.stream().collect(Collectors.joining("\",\""));
            columnField = "\"" + columnField + "\"";

            sql.append("select ")
                    .append(columnField)
                    .append(" from ")
                    .append("\"")
                    .append(dmConnectDto.getSchema())
                    .append("\"")
                    .append(StringPool.DOT)
                    .append("\"")
                    .append(dataSource.getDataSourceStructure().getExecuteName())
                    .append("\"");
            if (dataSource.getSize() > 0L) {
                sql.append(" LIMIT  ").append(dataSource.getSize()).append(" OFFSET  0;");
            }
            connection.put("querySql", Collections.singletonList(sql));
            parameter.put("connection", Collections.singletonList(connection));
            parameter.put("username", dmConnectDto.getSourceUserName());
            parameter.put("password", dmConnectDto.getSourcePwd());
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }

    @Override
    public void check(String json) {
        try {
            DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
            DMConnectDto connectDto = dataSource.getCustomJson().toJavaObject(DMConnectDto.class);
            String sql = "select 1";
            DMClientConfig clientConfig = DMClientConfig.init(connectDto, 10000);
            clientConfig.queryData(sql);
        } catch (Exception exception) {
            log.info("校验达梦链接错误", exception);
            throw new BusinessException(exception.getMessage());
        }
    }

    /**
     * 获取某个用户下面的 所有模式
     */
    public List<String> getSchema(DMConnectDto dmConnectDto) {
        try {
            DMClientConfig clientConfig = DMClientConfig.init(dmConnectDto, 10000);
            List<Map<String, Object>> list = clientConfig.queryData(GET_SCHEMA);
            if (list.isEmpty()) {
                return new ArrayList<>();
            }
            return list.stream().map(e -> e.getOrDefault("OBJECT_NAME", "").toString()).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("获取模式错误:{}", e.getMessage());
        }
    }

    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        String v = (String) value;
        String format = FieldUtil.subStr(v);
        String s1 = "(";
        String type = v.contains(s1) ? v.substring(0, v.lastIndexOf(s1)) : v;
        type = type.toUpperCase();
        switch (type) {
            case "FLOAT":
            case "DOUBLE":
            case "REAL":
            case "SMALLINT":
            case "NUMERIC":
            case "DECIMAL":
            case "INTEGER":
            case "INT":
            case "BIGINT":
            case "TINYINT":
            case "MEDIUMINT":
            case "YEAR":
                structure.setFormat(format);
                break;
            case "DATE":
                structure.setFrom("yyyy-MM-dd");
                break;
            case "DATETIME":
            case "TIMESTAMP":
                structure.setFormat("yyyy-MM-dd HH:mm:ss");
                break;
            case "CHAR":
            case "VARCHAR":
                if (StrUtil.isNotBlank(format)) {
                    BigDecimal multiply = new BigDecimal(format).multiply(new BigDecimal(3));
                    format = multiply.toString();
                }
                structure.setFormat(format);
            default:
                structure.setFormat(format);
        }

    }
}
