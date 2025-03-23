package cn.bctools.data.factory.source.data.presto;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.ClickHouseClientConfig;
import cn.bctools.data.factory.source.data.config.PrestoClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.PrestoConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.util.FieldUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component("prestoDataBase")
public class PrestoDBExecuteImpl implements DataSourceExecuteInterface {

    private static final String TABLE_QUERY = "SELECT table_name FROM information_schema.tables WHERE tables.table_schema = '{}'";

    private static final String COLUMN_QUERY = "SELECT table_name ,column_name ,data_type ,comment  FROM  information_schema.columns where table_catalog ='{}' AND  table_schema = '{}'";

    private static final String DATA_QUERY = "SELECT {} FROM {}";

    private static final String DATA_PAGE_QUERY = "SELECT {} FROM {} LIMIT {}";
    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        PrestoConnectDto prestoConnectDto = dataSource.getCustomJson().toJavaObject(PrestoConnectDto.class);
        try {
            PrestoClientConfig clientConfig = PrestoClientConfig.init(prestoConnectDto, 15000);
            //表描述
            Map<String, List<ColumnInfo>> columnMap = clientConfig.queryData(StrUtil.format(COLUMN_QUERY, prestoConnectDto.getCatalog(), prestoConnectDto.getSchema())).stream().map(e -> BeanUtil.toBean(e, ColumnInfo.class)).collect(Collectors.groupingBy(ColumnInfo::getTable_name));
            return columnMap.keySet().stream()
                    .map(tableName -> {
                                DataSourceStructure sourceStructure = new DataSourceStructure()
                                        .setName(tableName)
                                        .setExecuteName(tableName)
                                        .setCheckIs(Boolean.TRUE)
                                        .setTableNameDesc(tableName);

                                List<DataSourceStructure.Structure> collect = columnMap.get(tableName).stream()
                                        .map(v -> {
                                            DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                                                    .setOriginalColumnName(v.getColumn_name())
                                                    .setColumnCount(StrUtil.isNotBlank(v.getComment())?v.getComment():v.getColumn_name())
                                                    .setDataType(v.getData_type());
                                            fieldTypeEnum(v.getData_type(), structure);
                                            return structure;
                                        })
                                        .collect(Collectors.toList());
                                return sourceStructure.setStructure(collect).setFieldCount(collect.size());
                            }
                    ).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            log.info("mysql 表结构错误", e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        PrestoConnectDto prestoConnectDto = dataSource.getCustomJson().toJavaObject(PrestoConnectDto.class);
        try {
            PrestoClientConfig clientConfig = PrestoClientConfig.init(prestoConnectDto, 15000);


            String sql = StrUtil.format(DATA_QUERY,"*",dataSourceStructure.getExecuteName());
            //如果入参不为空 则拼接入参
            //分页查询
            if (size > 0) {
                sql = StrUtil.format(DATA_PAGE_QUERY,"*",dataSourceStructure.getExecuteName(),size);
            }
            List<Map<String, Object>> originTableData = clientConfig.queryData(sql);
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
                    .setTotal(clientConfig.getTotalCount(dataSourceStructure.getExecuteName()))
                    .setSize(size)
                    .setCurrent(current)
                    .setRecords(list);
        } catch (Exception e) {
            log.info("查询数据错误", e);
            throw new BusinessException(StrUtil.format("获取presto数据错误【{}】", e.getMessage()));
        }
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        try {
            PrestoConnectDto prestoConnectDto = dataSource.getCustomJson().toJavaObject(PrestoConnectDto.class);
            PrestoClientConfig clientConfig = PrestoClientConfig.init(prestoConnectDto, 10000);
            // 执行查询并获取结果集
            return clientConfig.getTotalCount(dataSourceStructure.getExecuteName());
        } catch (Exception e) {
            log.info("查询数据错误", e);
            throw new BusinessException("获取条数错误");
        }
    }

    @Override
    public void check(String json) {
        try {
            DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
            PrestoConnectDto prestoConnectDto = dataSource.getCustomJson().toJavaObject(PrestoConnectDto.class);
            PrestoClientConfig.check(prestoConnectDto);
        } catch (Exception exception) {
            log.info("校验presto链接错误", exception);
            throw new BusinessException(exception.getMessage());
        }
    }

    public List<String> getSchema(PrestoConnectDto prestoConnectDto) {
        try {
            return PrestoClientConfig.getSchema(prestoConnectDto);
        } catch (Exception e) {
            log.info("获取模式错误", e);
            throw new BusinessException(StrUtil.format("获取模式错误:{}", e.getMessage()));
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
                structure.setFormat("yyyy-MM-dd HH:mm:ss");
                break;
            case "date":
                structure.setFrom("yyyy-MM-dd");
                break;
            case "varchar":
            case "char":
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
    public Function<? extends CreateDataXJsonParameterPo, JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<String> column = dataSource.getDataSourceStructure().getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "rdbmsreader");
            JSONObject parameter = new JSONObject();
            parameter.put("column", column);
            //连接信息
            JSONObject connection = new JSONObject();
            PrestoConnectDto prestoConnectDto = dataSource.getCustomJson().toJavaObject(PrestoConnectDto.class);
            String url = StrUtil.format(ClickHouseClientConfig.URL, prestoConnectDto.getSourceHost(), prestoConnectDto.getSourcePort(), prestoConnectDto.getCatalog(),prestoConnectDto.getSchema());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            if (dataSource.getSize() > 0L) {
                String columnField = String.join(",", column);
                String sql = StrUtil.format(DATA_PAGE_QUERY,columnField,dataSource.getDataSourceStructure().getExecuteName(),dataSource.getSize());
                connection.put("querySql", Collections.singletonList(sql));
            } else {
                connection.put("table", Collections.singletonList(dataSource.getDataSourceStructure().getExecuteName()));
            }
            parameter.put("connection", Collections.singletonList(connection));
            parameter.put("username", prestoConnectDto.getSourceUserName());
            parameter.put("password", prestoConnectDto.getSourcePwd());
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }

    @Data
    @Accessors(chain = true)
    public static class ColumnInfo{
        private String table_name;
        private String column_name;
        private String data_type;
        private String comment;
    }
}
