package cn.bctools.data.factory.source.data.clickhouse;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.data.factory.source.data.config.ClickHouseClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.PublicConnectDto;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(value = "clickHouseDataSource")
@Slf4j
public class ClickHouseExecuteImpl implements DataSourceExecuteInterface {

    private static final String TABLE_QUERY = "SELECT name,comment FROM system.tables where database = '{}'";

    private final static String COLUMN_QUERY = "SELECT table,name,type,comment FROM system.columns where database = '{}'";

    private final static String DATA_QUERY = "SELECT {} FROM {}";

    private final static String DATA_PAGE_QUERY = "SELECT {} FROM '{}' LIMIT {} OFFSET {}";

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
        ClickHouseClientConfig init = ClickHouseClientConfig.init(publicConnectDto, 10000);
        List<Map<String, Object>> list = init.queryData(StrUtil.format(TABLE_QUERY,publicConnectDto.getSourceLibraryName()));
        List<ClickHouseTable> tables = BeanCopyUtil.copys(list, ClickHouseTable.class);

        Map<String, List<ClickHouseColumn>> columnMap = init.queryData(StrUtil.format(COLUMN_QUERY, publicConnectDto.getSourceLibraryName())).stream().map(e -> BeanUtil.toBean(e, ClickHouseColumn.class)).collect(Collectors.groupingBy(ClickHouseColumn::getTable));

        return tables.stream().map(table ->{
            DataSourceStructure sourceStructure = new DataSourceStructure()
                    .setName(table.getName())
                    .setExecuteName(table.getName())
                    .setCheckIs(Boolean.TRUE)
                    .setTableNameDesc(StrUtil.isBlank(table.getComment())?table.getName():table.getComment());
            List<ClickHouseColumn> columns = columnMap.get(table.getName());
            if(columns==null){
                return sourceStructure;
            }
            List<DataSourceStructure.Structure> structureList = columns.stream().map(e -> {
                DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                        .setOriginalColumnName(e.getName())
                        .setColumnCount(StrUtil.isNotBlank(e.getComment()) ? e.getComment() : e.getName())
                        .setDataType(e.getType());
                fieldTypeEnum(e.getType(), structure);
                return structure;
            }).collect(Collectors.toList());

            return sourceStructure.setStructure(structureList).setFieldCount(structureList.size());
        }).collect(Collectors.toList());
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {

        PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
        try {
            ClickHouseClientConfig clientConfig = ClickHouseClientConfig.init(publicConnectDto, 15000);

            String sql = StrUtil.format(DATA_QUERY,"*",dataSourceStructure.getExecuteName());

            List<Map<String, Object>> originTableData;
            //如果入参不为空 则拼接入参
            //分页查询
            if (size > 0) {
                current = current<=0L?1L:current;
                long offset = size * (current - 1);
                sql = StrUtil.format(DATA_PAGE_QUERY,"*",dataSourceStructure.getExecuteName(),size,offset);

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
                    .setTotal(clientConfig.getTotalCount( dataSourceStructure.getExecuteName()))
                    .setSize(size)
                    .setCurrent(current)
                    .setRecords(list);
        } catch (Exception e) {
            log.info("查询数据错误", e);
            throw new BusinessException("获取click house数据错误【{}】", e.getMessage());
        }
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        try {
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            ClickHouseClientConfig clientConfig = ClickHouseClientConfig.init(publicConnectDto, 10000);
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
            ClickHouseClientConfig clientConfig = ClickHouseClientConfig.init(publicConnectDto, 10000);
            clientConfig.queryData(sql);
        } catch (Exception exception) {
            log.info("校验click house链接错误", exception);
            throw new BusinessException(exception.getMessage());
        }
    }

    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        String v = (String) value;

        String format = FieldUtil.subStr(v);

        String s1 = "(";
        String type = v.contains(s1) ? v.substring(0, v.lastIndexOf(s1)) : v;
        switch (type.toUpperCase()){
            case "UINT8":
            case "UINT16":
            case "UINT32":
            case "UINT64":
            case "INT8":
            case "INT16":
            case "INT32":
            case "INT64":
            case "FLOAT32":
            case "FLOAT64":
                structure.setFormat(format);
                break;
            case "DATE":
                structure.setFormat("yyyy-MM-dd");
                break;
            case "DATETIME":
            case "DATETIME64":
                structure.setFormat("yyyy-MM-dd HH:mm:ss");
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
            jsonObject.put("name", "clickhousereader");
            JSONObject parameter = new JSONObject();
            parameter.put("column", column);
            //连接信息
            JSONObject connection = new JSONObject();
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            String url = StrUtil.format(ClickHouseClientConfig.URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            if (dataSource.getSize() > 0L) {
                String columnField = column.stream().map(e -> StrUtil.format("`{}`", e)).collect(Collectors.joining(","));
                String sql = StrUtil.format(DATA_PAGE_QUERY,columnField,dataSource.getDataSourceStructure().getExecuteName(),dataSource.getSize(),0);
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

    @Data
    @Accessors(chain = true)
    public static class ClickHouseTable{
        private String name;

        private String comment;
    }
    @Data
    @Accessors(chain = true)
    public static class ClickHouseColumn{
        private String table;
        private String name;
        private String type;
        private String comment;
    }
}
