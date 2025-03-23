package cn.bctools.data.factory.source.data.db2;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.Db2ClientConfig;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.Db2ConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.util.FieldUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component(value = "db2DataSource")
public class Db2DataSourceExecuteImpl implements DataSourceExecuteInterface {

    private final static String BASE_SQL = "SELECT * FROM {}.{}";

    private final static String FIND_TABLE_STRUCTURE = "select tb.TABNAME, col.NAME ,col.REMARKS, col.COLTYPE from syscat.tables tb LEFT JOIN sysibm.syscolumns col ON tb.TABNAME = col.TBNAME WHERE tb.TABSCHEMA = '{}'";

    private final static String FIND_TABLE_DESC = "select TABNAME ,REMARKS  from syscat.tables WHERE TABSCHEMA = '{}'";

    private final static String GET_SCHEMA = "select SCHEMANAME from syscat.schemata WHERE OWNERTYPE = 'U'";

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        try {
            Db2ConnectDto connectDto = dataSource.getCustomJson().toJavaObject(Db2ConnectDto.class);
            Db2ClientConfig clientConfig = Db2ClientConfig.init(connectDto, 10000);
            String schema = connectDto.getSchema();
            Map<String, List<Map<String, Object>>> tableNameGroup = clientConfig.queryDataFormat(FIND_TABLE_STRUCTURE, schema).stream().collect(Collectors.groupingBy(e -> StrUtil.toString(e.get("TABNAME"))));
            Map<String, String> tableDesc = clientConfig.queryDataFormat(FIND_TABLE_DESC, schema).stream().collect(Collectors.toMap(e -> StrUtil.toString(e.get("TABNAME")), e -> {
                Object remarks = e.get("REMARKS");
                if (remarks == null) {
                    return StrUtil.toString("TABNAME");
                }
                return StrUtil.toString(remarks);
            }));
            return tableNameGroup.keySet().stream().map(e -> {
                DataSourceStructure sourceStructure = new DataSourceStructure()
                        .setName(e)
                        .setExecuteName(e)
                        .setCheckIs(Boolean.TRUE)
                        .setTableNameDesc(tableDesc.get(e));
                List<DataSourceStructure.Structure> structures = tableNameGroup.get(e).stream().map(v -> {
                    String name = StrUtil.toString(v.get("NAME"));
                    String remarks = StrUtil.toStringOrNull(v.get("REMARKS"));
                    String coltype = StrUtil.toStringOrNull(v.get("COLTYPE"));
                    DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                            .setOriginalColumnName(name)
                            .setColumnCount(Objects.isNull(remarks) ? name : remarks)
                            .setDataType(coltype);
                    fieldTypeEnum(coltype, structure);
                    return structure;
                }).collect(Collectors.toList());
                return sourceStructure.setStructure(structures).setFieldCount(structures.size());
            }).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("DB2数据库同步表结构异常【{}】", e.getMessage());
        }
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        Db2ConnectDto connectDto = dataSource.getCustomJson().toJavaObject(Db2ConnectDto.class);
        try {
            Db2ClientConfig clientConfig = Db2ClientConfig.init(connectDto, 15000);
            String sql = StrUtil.format(BASE_SQL, connectDto.getSchema(), dataSourceStructure.getExecuteName());

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
            throw new BusinessException(StrUtil.format("获取DB2数据错误【{}】", e.getMessage()));
        }
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        try {
            Db2ConnectDto connectDto = dataSource.getCustomJson().toJavaObject(Db2ConnectDto.class);
            Db2ClientConfig clientConfig = Db2ClientConfig.init(connectDto, 10000);
            return clientConfig.getTotalCount(connectDto, dataSourceStructure.getExecuteName());
        } catch (Exception exception) {
            log.info("校验Db2链接错误", exception);
            throw new BusinessException(exception.getMessage());
        }
    }

    @Override
    public void check(String json) {
        try {
            DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
            Db2ConnectDto connectDto = dataSource.getCustomJson().toJavaObject(Db2ConnectDto.class);
            String sql = "SELECT 1 FROM SYSIBM.SYSDUMMY1";
            Db2ClientConfig clientConfig = Db2ClientConfig.init(connectDto, 10000);
            clientConfig.queryData(sql);
        } catch (Exception exception) {
            log.info("校验Db2链接错误", exception);
            throw new BusinessException(exception.getMessage());
        }
    }

    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        String v = (String) value;
        String format = FieldUtil.subStr(v);
        String s = v.contains("(") ? v.substring(0, v.lastIndexOf("(")) : v;
        s = StrUtil.trim(s);
        String finalS = s;
        if ("DECIMAL".equals(s)) {
            String[] split = format.split(",");
            structure.setLength(Integer.valueOf(split[0]));
            structure.setPrecision(Integer.valueOf(split[1]));
            return;
        }
        List<String> dateList = Arrays.asList("DATE", "TIME", "TIMESTAMP");
        //yyyy-MM-dd HH:mm:ss.SSS
        if (dateList.stream().anyMatch(e -> StrUtil.equals(e, finalS))) {
            switch (s) {
                case "DATE":
                    structure.setFrom("yyyy-MM-dd");
                    break;
                case "TIMESTAMP":
                    structure.setFormat("yyyy-MM-dd HH:mm:ss");
                    break;
                default:
            }
            return;
        }
        List<String> stringList = Arrays.asList("CHAR", "VARCHAR", "LONGVARCHAR", "CLOB");
        if (stringList.stream().anyMatch(e -> StrUtil.equals(e, finalS))) {
            if (format == null) {
                //默认长度
                format = "50";
            }
            structure.setLength(Integer.parseInt(format));
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
            Db2ConnectDto db2ConnectDto = dataSource.getCustomJson().toJavaObject(Db2ConnectDto.class);

            String url = StrUtil.format(Db2ClientConfig.URL, db2ConnectDto.getSourceHost(), db2ConnectDto.getSourcePort(), db2ConnectDto.getSourceLibraryName());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            StringBuffer sql = new StringBuffer();
            String columnField = column.stream().collect(Collectors.joining("\",\""));
            columnField = "\"" + columnField + "\"";

            sql.append("select ")
                    .append(columnField)
                    .append(" from ")
                    .append(db2ConnectDto.getSchema())
                    .append(StringPool.DOT)
                    .append(dataSource.getDataSourceStructure().getExecuteName());
            if (dataSource.getSize() > 0L) {
                sql.append(" LIMIT  ").append(dataSource.getSize()).append(" OFFSET  0;");
            }
            connection.put("querySql", Collections.singletonList(sql));
            parameter.put("connection", Collections.singletonList(connection));
            parameter.put("username", db2ConnectDto.getSourceUserName());
            parameter.put("password", db2ConnectDto.getSourcePwd());
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }

    /**
     * 获取某个用户下面的 所有模式
     */
    public List<String> getSchema(Db2ConnectDto db2ConnectDto) {
        try {
            Db2ClientConfig clientConfig = Db2ClientConfig.init(db2ConnectDto, 10000);
            List<Map<String, Object>> list = clientConfig.queryData(GET_SCHEMA);
            if (list.isEmpty()) {
                return new ArrayList<>();
            }
            return list.stream().map(e -> e.getOrDefault("SCHEMANAME", "").toString()).map(StrUtil::trim).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            String format = StrUtil.format("获取模式错误:{}", e.getMessage());
            throw new BusinessException(format);
        }
    }
}
