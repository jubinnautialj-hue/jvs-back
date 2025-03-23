package cn.bctools.data.factory.source.data.mariadb;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.MysqlClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.PublicConnectDto;
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
@Component(value = "mariadbDataSource")
@Slf4j
public class MariadbDataSourceExecuteImpl implements DataSourceExecuteInterface {
    /**
     * 连接信息
     */
    private static final String URL = "jdbc:mysql://{}:{}/{}";

    /**
     * 获取整个数据库表结构
     */
    private static final String GET_TABLE_STRUCTURE_MYSQL = "select COLUMN_NAME,TABLE_NAME,DATA_TYPE,COLUMN_COMMENT,COLUMN_TYPE from information_schema.`COLUMNS` where table_schema = '{}'";

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
            log.info("校验链接错误", exception);
            throw new BusinessException(exception.getMessage());
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
                                                    .setColumnCount(columnCount)
                                                    .setDataType(v.get("data_type").toString());
                                            fieldTypeEnum(v.get("column_type"), structure);
                                            return structure;
                                        })
                                        .collect(Collectors.toList());
                                return sourceStructure.setStructure(collect).setFieldCount(collect.size());
                            }
                    ).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("mysql 表结构错误", e);
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
            case "datetime":
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

    @Override
    public Function<? extends CreateDataXJsonParameterPo, com.alibaba.fastjson2.JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            com.alibaba.fastjson2.JSONObject jsonObject = new com.alibaba.fastjson2.JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "mysqlreader");
            com.alibaba.fastjson2.JSONObject parameter = new com.alibaba.fastjson2.JSONObject();
            parameter.put("column", column);
            //连接信息
            com.alibaba.fastjson2.JSONObject connection = new com.alibaba.fastjson2.JSONObject();
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            String url = StrUtil.format(URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
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
