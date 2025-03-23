package cn.bctools.data.factory.util;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.CommonConfig;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * doris工具类
 * 1.生成表结构
 *
 * @author xiaohui
 */
@Slf4j
public class DorisUtil {
    private static final String URL = "jdbc:mysql://{}:{}";


    /**
     * 获取doris 创建表的sql语句
     * sql 举例
     * CREATE TABLE IF NOT EXISTS example_db.example_tbl_duplicate_without_keys_by_default
     * (
     * `timestamp` DATETIME NOT NULL COMMENT "日志时间",
     * `type` INT NOT NULL COMMENT "日志类型",
     * `error_code` INT COMMENT "错误码",
     * `error_msg` VARCHAR(1024) COMMENT "错误详细信息",
     * `op_id` BIGINT COMMENT "负责人id",
     * `op_time` DATETIME COMMENT "处理时间"
     * )
     * DISTRIBUTED BY HASH(`type`) BUCKETS 1
     * PROPERTIES (
     * "enable_duplicate_without_keys_by_default" = "true"
     * );
     *
     * @param dataSourceFields 数据源的字段结构
     * @param tableName        表名称
     * @param isUnique         是否创建 unique模型表 此模型目前用于条件分组节点 因为doris 目前只有 此模型支持update 语句
     * @param uniqueKey        主键模型的 主键key
     */
    public static String getTableSql(String tableName, List<DataSourceField> dataSourceFields, Boolean isUnique, List<DataSourceField> uniqueKey) {
        Integer replicationNum = SpringContextUtil.getBean(CommonConfig.class).getReplicationNum();
        StringBuilder createTableSql = new StringBuilder();
        //获取库名
        String libraryName = SpringContextUtil.getBean(DorisConfig.class).getLibraryName();
        createTableSql.append("CREATE TABLE IF NOT EXISTS ")
                .append(libraryName)
                .append(".");
        //添加表名称
        createTableSql
                .append(tableName)
                .append("(\n");
        String dorisUniqueFieldKey = Constant.DORIS_UNIQUE_FIELD_KEY;
        if (isUnique && uniqueKey.isEmpty()) {
            createTableSql.append("`").append(dorisUniqueFieldKey).append("` VARCHAR(50) NOT NULL COMMENT \"模型唯一key\" \n")
                    .append(",");
        }
        //添加字段
        for (int i = 0; i < dataSourceFields.size(); i++) {
            DataSourceField e = dataSourceFields.get(i);
            createTableSql.append("`")
                    //字段名称
                    .append(e.getFieldKey())
                    .append("` ");
            //字段类型 默认为 STRING
            String field = e.getDorisType();

            int length = Optional.ofNullable(e.getLength()).orElse(0);
            Integer precision = Optional.ofNullable(e.getPrecision()).orElse(0);
            field = String.format(field, length, precision);
            //todo 最好添加一个原表的解释
            createTableSql.append(field)
                    .append(" COMMENT ")
                    .append("\"")
                    //解释
                    .append(e.getFieldName())
                    .append("\",")
                    .append("\n");
        }
        //添加自动分桶字段 sys_ods_bucket
        createTableSql.append("`").append(Constant.DORIS_ODS_KEY).append("` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \"插入时间-用于分桶策略\" \n");
        //添加结束
        createTableSql.append(")");
        //添加模型关键字
        if (isUnique) {
            createTableSql.append("UNIQUE KEY( `");
            if (uniqueKey.isEmpty()) {
                createTableSql.append(dorisUniqueFieldKey);
            } else {
                String string = uniqueKey.stream().map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
                createTableSql.append(string);
                dorisUniqueFieldKey = string;
            }
            createTableSql.append("`) \n");
            createTableSql.append("DISTRIBUTED BY HASH( `");
            createTableSql.append(dorisUniqueFieldKey)
                    //todo 注意 这里的分桶目前为10  后续需要优化
                    .append("`) BUCKETS 10 \n");
            createTableSql.append("PROPERTIES (\n")
                    .append("\"replication_allocation\" = \"tag.location.default: ")
                    .append(replicationNum)
                    .append("\"\n")
                    .append(");");
        } else {
            //添加不需要排序 与分桶 ods 分区暂时不需要 分桶目前用doris 自动分桶模式 doris自动推算分桶数量
            createTableSql.append("DISTRIBUTED BY HASH( `");
            createTableSql.append(Constant.DORIS_ODS_KEY)
                    .append("`) BUCKETS 10 \n");
            createTableSql.append("PROPERTIES (\n")
                    .append("\"enable_duplicate_without_keys_by_default\" = \"true\",\"replication_num\" = \"")
                    .append(replicationNum)
                    .append("\"\n")
                    .append(");");
        }
        log.info("表sql文件为:{}", createTableSql);
        return createTableSql.toString();
    }


    /**
     * 获取doris jdbc
     *
     * @param isUse 是否需要指定数据库
     * @return jdbc对象
     */
    public static JdbcTemplate getJdbc(Boolean isUse) {
        DorisConfig dorisConfig = SpringContextUtil.getBean(DorisConfig.class);
        String url = StrUtil.format(URL, dorisConfig.getIp(), dorisConfig.getQueryPort());
        if (!isUse) {
            url += "/" + dorisConfig.getLibraryName();
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername(dorisConfig.getUserName());
        dataSource.setPassword(dorisConfig.getPassWord());
        return new JdbcTemplate(dataSource);
    }


    /**
     * 所有需要生成零时表的地方 都统一调用这个方法 方便统一管理  除了 数据集正式运行时的输出节点
     * 通过节点id与数据集id 是否为正式运行 生成表名称
     *
     * @param formal        是否为正式运行
     * @param dataFactoryId 数据集id
     * @param nodeId        节点id
     * @return 表名称
     */
    public static String createTableName(String dataFactoryId, String nodeId, Boolean formal) {
        return "ods_" + dataFactoryId + "_" + nodeId + "_" + formal;
    }

}
