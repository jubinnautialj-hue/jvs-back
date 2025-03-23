package cn.bctools.data.factory.source.data.hive;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.data.config.HiveClientConfig;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Component(value = "hiveDateSource")
@Slf4j
public class HiveExecuteImpl implements DataSourceExecuteInterface {

    /**
     * 查询所有的表
     */
    private static final String TABLE_QUERY = "show tables";

    /**
     * 查询表中的字段
     */
    private static final String COLUMN_QUERY = "desc {}";

    /**
     * 查询表详细信息
     */
    private static final String TABLE_DESC_QUERY = "DESCRIBE FORMATTED {}";

    /**
     * 数据查询 分页
     */
    private static final String DATA_PAGE_QUERY = "SELECT * FROM {} LIMIT {} OFFSET {}";

    private static final String DATA_DYNAMIC_COLUMN_PAGE_QUERY = "SELECT {} FROM {} LIMIT {} OFFSET {}";

    /**
     * 数据查询
     */
    private static final String DATA_QUERY = "SELECT * FROM {}";

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
        try {
            HiveClientConfig client = HiveClientConfig.init(publicConnectDto);

            List<Map<String, Object>> tables = client.queryData(TABLE_QUERY);
            return tables.stream().map(e -> StrUtil.toString(e.get("tab_name"))).map(e -> {

                //查询表描述
                String tableNameDesc = client.queryData(StrUtil.format(TABLE_DESC_QUERY, e))
                        .stream()
                        .map(v -> BeanUtil.toBean(v, DetailInfo.class))
                        .filter(v -> "comment".equals(v.getData_type()))
                        .map(DetailInfo::getComment).findFirst().orElse(e);

                //查询字段
                List<DataSourceStructure.Structure> structureList = client.queryData(StrUtil.format(COLUMN_QUERY, e))
                        .stream()
                        .map(v -> BeanUtil.toBean(v, DetailInfo.class))
                        .map(v -> {
                            DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                                    .setOriginalColumnName(e+"."+v.getCol_name())
                                    .setColumnCount(StrUtil.isNotBlank(v.getComment()) ? v.getComment() : v.getCol_name())
                                    .setDataType(v.getData_type());
                            fieldTypeEnum(v.getData_type(), structure);
                            return structure;
                        }).collect(Collectors.toList());
                return new DataSourceStructure()
                        .setName(e)
                        .setExecuteName(e)
                        .setCheckIs(Boolean.TRUE)
                        .setTableNameDesc(tableNameDesc)
                        .setStructure(structureList)
                        .setFieldCount(structureList.size());
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            log.info("apache hive 表结构错误", e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
        try {
            HiveClientConfig clientConfig = HiveClientConfig.init(publicConnectDto);

            String sql = StrUtil.format(DATA_QUERY,dataSourceStructure.getExecuteName());
            //分页查询
            if (size > 0) {
                current = current<1L?1L:current;
                long offset = size * (current - 1);
                sql = StrUtil.format(DATA_PAGE_QUERY,dataSourceStructure.getExecuteName(),size,offset);
            }
            List<Map<String, Object>> list = clientConfig.queryData(sql);

            return new Page<Map<String, Object>>()
                    .setTotal(clientConfig.getTotalCount(dataSourceStructure.getExecuteName()))
                    .setSize(size)
                    .setCurrent(current)
                    .setRecords(list);
        } catch (Exception e) {
            log.info("查询数据错误", e);
            throw new BusinessException("获取apache hive数据错误【{}】", e.getMessage());
        }
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
        try {
            HiveClientConfig clientConfig = HiveClientConfig.init(publicConnectDto);
            return clientConfig.getTotalCount(dataSourceStructure.getExecuteName());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("apache hive 获取总数异常");
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
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            String url = StrUtil.format(HiveClientConfig.URL, publicConnectDto.getSourceHost(), publicConnectDto.getSourcePort(), publicConnectDto.getSourceLibraryName());
            connection.put("jdbcUrl", Collections.singletonList(url));
            //判断是否存在限制的条数 如果存在就生成sql执行
            if (dataSource.getSize() > 0L) {
                String columnField = column.stream().collect(Collectors.joining(","));
                String sql = StrUtil.format(DATA_DYNAMIC_COLUMN_PAGE_QUERY, columnField, dataSource.getDataSourceStructure().getExecuteName(), dataSource.getSize(), 0);
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

    @Override
    public void check(String json) {
        try {
            DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
            PublicConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(PublicConnectDto.class);
            String sql = "select 1";
            HiveClientConfig clientConfig = HiveClientConfig.init(publicConnectDto);
            clientConfig.queryData(sql);
        } catch (Exception exception) {
            log.info("校验apache hive链接错误", exception);
            throw new BusinessException(exception.getMessage());
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

    @Data
    @Accessors(chain = true)
    public static class DetailInfo{
        private String col_name;
        private String data_type;
        private String comment;
    }
}
