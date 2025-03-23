package cn.bctools.data.factory.source.data.mysql;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.data.config.MysqlClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateSeaTunnelJsonParameterPo;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelSubmitJobDto;
import cn.bctools.data.factory.source.dto.PublicConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author xiaohui
 */
@Component(value = "mysqlDataSource")
@Slf4j
public class MysqlDataSourceExecuteImpl implements DataSourceExecuteInterface {

    /**
     * 连接信息
     */
    private static final String URL = "jdbc:mysql://{}:{}/{}?useSSL=false";
    /**
     * 获取binlog日志是否开启
     */
    private static final String GET_BIN_LOG_STATUS_SQL = "SHOW VARIABLES LIKE 'log_bin'";
    /**
     * 获取整个数据库表结构
     */
    private static final String GET_TABLE_STRUCTURE_MYSQL = "select COLUMN_NAME,TABLE_NAME,DATA_TYPE,COLUMN_COMMENT,COLUMN_TYPE,COLUMN_KEY from information_schema.`COLUMNS` where table_schema = '{}'";

    /**
     * 获取表描述
     */
    private static final String GET_TABLE_DESCRIBE = "SELECT table_name ,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = '{}'";

    /**
     * 获取数据
     */
    private static final String FIND_ALL_MYSQL = "select * from {}";


    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        try {
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            MysqlClientConfig clientConfig = MysqlClientConfig.init(publicConnectDto, 10000);
            // 执行查询并获取结果集
            return clientConfig.getTotalCount(dataSourceStructure.getExecuteName());
        } catch (Exception e) {
            log.info("查询数据错误", e);
            throw new BusinessException("获取mysql条数错误");
        }
    }

    @Override
    public void check(String json) {
        try {
            DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            String sql = "select 1";
            MysqlClientConfig clientConfig = MysqlClientConfig.init(publicConnectDto, 10000);
            clientConfig.queryData(sql);
        } catch (Exception exception) {
            log.info("校验mysql链接错误", exception);
            throw new BusinessException(exception.getCause().getMessage());
        }
    }


    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
        try {
            MysqlClientConfig clientConfig = MysqlClientConfig.init(publicConnectDto, 15000);
            String sql = StrUtil.format(FIND_ALL_MYSQL, dataSourceStructure.getExecuteName());
            List<Map<String, Object>> originTableData;
            //如果入参不为空 则拼接入参
            //分页查询
            if (size > 0) {
                long skip = size * (current - 1);
                sql += " limit " + skip + "," + size;
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
            log.info("查询数据错误", e);
            throw new BusinessException("获取mysql数据错误【{}】", e.getMessage());
        }
    }


    @Override
    public Function<? extends CreateSeaTunnelJsonParameterPo, SeaTunnelSubmitJobDto> createSeaTunnelJsonCDCFunction() {
        return (dataSource) -> {
            //源数据列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            //连接信息
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            SeaTunnelSubmitJobDto seaTunnelSubmitJobDto = new SeaTunnelSubmitJobDto();
            JSONObject sourceObject = new JSONObject();
            sourceObject.put("plugin_name", "MySQL-CDC");
            String url = StrUtil.format(URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
            sourceObject.put("base-url", url);
            sourceObject.put("username", publicConnectDto.getSourceUserName());
            sourceObject.put("password", publicConnectDto.getSourcePwd());
            sourceObject.put("startup.mode", "initial");
            String tableNames = publicConnectDto.getSourceLibraryName() + "." + dataSource.getDataSourceStructure().getExecuteName();
            sourceObject.put("table-names", Arrays.asList(tableNames));
            seaTunnelSubmitJobDto.setSource(Collections.singletonList(sourceObject));
            return seaTunnelSubmitJobDto;
        };
    }

    @Override
    public Function<? extends CreateSeaTunnelJsonParameterPo, SeaTunnelSubmitJobDto> createSeaTunnelJsonFunction() {
        return (dataSource) -> {
            //源数据列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            //连接信息
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            SeaTunnelSubmitJobDto seaTunnelSubmitJobDto = new SeaTunnelSubmitJobDto();
            JSONObject sourceObject = new JSONObject();
            sourceObject.put("plugin_name", "jdbc");
            String url = StrUtil.format(URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
            sourceObject.put("url", url);
            sourceObject.put("driver", "com.mysql.cj.jdbc.Driver");
            sourceObject.put("user", publicConnectDto.getSourceUserName());
            sourceObject.put("password", publicConnectDto.getSourcePwd());
            sourceObject.put("table_path", publicConnectDto.getSourceLibraryName());
            StringBuffer sql = new StringBuffer();
            sql.append("select ")
                    .append(column.stream().collect(Collectors.joining(",")))
                    .append(" from ")
                    .append(dataSource.getDataSourceStructure().getExecuteName());
            if (dataSource.getSize() > 0L) {
                sql.append(" limit 0,")
                        .append(dataSource.getSize())
                        .append(";");
            }
            sourceObject.put("query", sql.toString());
            seaTunnelSubmitJobDto.setSource(Collections.singletonList(sourceObject));
            return seaTunnelSubmitJobDto;
        };
    }

    @Override
    public Boolean realTimeIsOpen(DataSource dataSource) {
        try {
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            MysqlClientConfig clientConfig = MysqlClientConfig.init(publicConnectDto, 15000);
            List<Map<String, Object>> list = clientConfig.queryData(GET_BIN_LOG_STATUS_SQL);
            if (!list.isEmpty()) {
                String value = list.get(0).get("Value").toString();
                return "ON".equals(value);
            }
        } catch (Exception exception) {
            log.error("获取binlog日志状态错误", exception);
        }
        return false;
    }

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
        try {
            MysqlClientConfig clientConfig = MysqlClientConfig.init(publicConnectDto, 15000);
            //表结构
            List<Map<String, Object>> query = clientConfig.queryData(StrUtil.format(GET_TABLE_STRUCTURE_MYSQL, publicConnectDto.getSourceLibraryName()));
            //表描述
            Map<String, String> tableDescribe = clientConfig.queryData(StrUtil.format(GET_TABLE_DESCRIBE, publicConnectDto.getSourceLibraryName()))
                    .parallelStream().collect(Collectors.toMap(e -> e.get("table_name").toString(), e -> e.get("TABLE_COMMENT").toString()));
            Map<String, List<Map<String, Object>>> map = query.parallelStream()
                    .collect(Collectors.groupingBy(e -> e.get("TABLE_NAME").toString(), Collectors.toList()));
            return map.keySet().parallelStream()
                    .map(e -> {
                                String tableNameDesc = tableDescribe.get(e);
                                if (StrUtil.isBlank(tableNameDesc)) {
                                    tableNameDesc = e;
                                }
                                DataSourceStructure sourceStructure = new DataSourceStructure()
                                        .setName(e)
                                        .setExecuteName(e)
                                        .setCheckIs(Boolean.TRUE)
                                        .setTableNameDesc(tableNameDesc);
                                List<DataSourceStructure.Structure> collect = map.get(e).parallelStream()
                                        .map(v -> {
                                            String columnCount = v.get("column_comment").toString();
                                            if (StrUtil.isBlank(columnCount)) {
                                                columnCount = v.get("column_name").toString();
                                            }
                                            DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                                                    .setOriginalColumnName(v.get("column_name").toString())
                                                    .setPrimaryIs("PRI".equals(Optional.ofNullable(v.get("column_key")).orElse("")))
                                                    .setColumnCount(columnCount)
                                                    .setDataType(v.get("data_type").toString());
                                            fieldTypeEnum(v.get("column_type"), structure);
                                            return structure;
                                        })
                                        .collect(Collectors.toList());
                                return sourceStructure.setStructure(collect).setFieldCount(collect.size())
                                        .setPrimaryIs(collect.stream().anyMatch(DataSourceStructure.Structure::getPrimaryIs));
                            }
                    ).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            log.info("mysql 表结构错误", e);
            throw new BusinessException(e.getMessage());
        }
    }


    @Override
    public Function<? extends CreateDataXJsonParameterPo, JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "mysqlreader");
            JSONObject parameter = new JSONObject();
            parameter.put("column", column);
            //连接信息
            JSONObject connection = new JSONObject();
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            String url = StrUtil.format(URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            StringBuffer sql = new StringBuffer();
            String columnField = column.stream().collect(Collectors.joining(","));
            sql.append("select ")
                    .append(columnField)
                    .append(" from ")
                    .append(dataSource.getDataSourceStructure().getExecuteName());
            if (dataSource.getSize() > 0L) {
                sql.append(" limit 0,")
                        .append(dataSource.getSize())
                        .append(";");
                connection.put("querySql", Collections.singletonList(sql));
            } else {
                //判断 是否存在增量更新如果存在 就需要添加查询语句
                if (dataSource.getIncrementalMode()) {
                    sql.append(" WHERE ").append(dataSource.getIncrementalModeKey()).append(" > ");
                    if (dataSource.getIncrementalModeKeyType().equals(DataFieldTypeEnum.DATETIME)) {
                        sql.append("'").append(dataSource.getIncrementalModeKeyValue()).append("'");
                    } else {
                        sql.append(dataSource.getIncrementalModeKeyValue());
                    }
                    connection.put("querySql", Collections.singletonList(sql));
                } else {
                    connection.put("table", Collections.singletonList(dataSource.getDataSourceStructure().getExecuteName()));
                }
            }
            parameter.put("connection", Collections.singletonList(connection));
            parameter.put("username", publicConnectDto.getSourceUserName());
            parameter.put("password", publicConnectDto.getSourcePwd());
//            if (dataSource.getDataSourceStructure().getExecuteName().equals("test_100000000")){
//                parameter.put("splitPk","id");
//            }
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }


    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        String v = (String) value;
        String s1 = "(";
        String s = v.contains(s1) ? v.substring(0, v.lastIndexOf(s1)) : v;
        //设置格式
        if (v.contains(s1) && !"enum".equals(s)) {
            String format = v.substring(v.lastIndexOf(s1) + 1, v.lastIndexOf(")"));
            //因为 doris 的char  varchar长度是字节数 但是mysql 是 长度 所以 不能直接相等 这里就直接乘以最大的 4个字节
            if (Arrays.asList("char", "varchar").contains(s)) {
                int i = Integer.parseInt(format);
                //还需要注意  doris chart  最大字节为 255  如果超过 就使用varchar
                i = Integer.parseInt(format) * 3;
                if (i > 255 && i <= 65533) {
                    structure.setDataType("varchar");
                } else if (i < 255 && s.equals("char")) {
                    structure.setDataType("char");
                } else if (i > 65533) {
                    structure.setDataType("text");
                }
                structure.setLength(i);
            } else if ("decimal".contains(s)) {
                String[] split = format.split(",");
                structure.setLength(Integer.valueOf(split[0]));
                structure.setPrecision(Integer.valueOf(split[1]));
            }
        }
        List<String> dateList = Arrays.asList("date", "datetime", "timestamp", "year", "time");
        //yyyy-MM-dd HH:mm:ss.SSS
        if (dateList.contains(s)) {
            switch (s) {
                case "date":
                    structure.setFormat("yyyy-MM-dd");
                    break;
                case "datetime":
                case "timestamp":
                    structure.setFormat("yyyy-MM-dd HH:mm:ss");
                    break;
                case "year":
                    structure.setFormat("yyyy");
                    break;
                case "time":
                    structure.setFormat("HH:mm:ss");
                    break;
                default:
            }
        }
    }
}
