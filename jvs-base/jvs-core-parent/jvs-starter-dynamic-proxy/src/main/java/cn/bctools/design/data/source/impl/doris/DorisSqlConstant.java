package cn.bctools.design.data.source.impl.doris;

/**
 * @Author: ZhuXiaoKang
 * @Description: doris数据库sql常量
 */
public class DorisSqlConstant {
    /**
     *  创建表
     *  - light_schema_change：1.2.0版本后增加的配置，新增删除列是同步处理的。以前的版本是异步的(需要SHOW ALTER TABLE COLUMN查看进度)
     *  - 默认创建倒排索引
     */
    protected static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `%s` (\n" +
            "\t`id` VARCHAR (40) NOT NULL COMMENT '主键',\n" +
            "\t`dataId` VARCHAR(40) NOT NULL COMMENT '数据id',\n" +
            "\t`createTime` DATETIME NOT NULL COMMENT '创建时间',\n" +
            "\tINDEX idx_dataId (`dataId`) USING INVERTED,\n" +
            "\tINDEX idx_createTime (`createTime`) USING INVERTED\n" +
            ") \n" +
            "UNIQUE KEY ( `id` ) \n" +
            "DISTRIBUTED BY HASH ( `id` ) BUCKETS 1 \n" +
            "PROPERTIES ( \n" +
            "\"replication_allocation\" = \"tag.location.default: 1\",\n" +
            "\"enable_unique_key_merge_on_write\" = \"true\",\n" +
            "\"light_schema_change\" = \"true\"\n" +
            ")";

    /**
     *  创建日志表
     */
    protected static final String CREATE_LOG_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `%s` (\n" +
            "\t`id` VARCHAR (40) NOT NULL COMMENT '主键',\n" +
            "\t`dataId` VARCHAR(40) NOT NULL COMMENT '数据id',\n" +
            "\t`version` INT NOT NULL COMMENT '数据版本',\n" +
            "\t`content` JSONB  COMMENT '数据内容',\n" +
            "\t`dataChange` JSONB COMMENT '变更记录',\n" +
            "\t`updateTime` DATETIME  COMMENT '修改时间',\n" +
            "\t`updateById` STRING  COMMENT '修改人id',\n" +
            "\t`operator` STRING  COMMENT '操作记录',\n" +
            "\t`remark` STRING COMMENT '备注',\n" +
            "\tINDEX idx_dataId (`dataId`) USING INVERTED\n" +
            ") \n" +
            "UNIQUE KEY ( `id` ) \n" +
            "DISTRIBUTED BY HASH ( `id` ) BUCKETS 1 \n" +
            "PROPERTIES ( \n" +
            "\"replication_allocation\" = \"tag.location.default: 1\",\n" +
            "\"enable_unique_key_merge_on_write\" = \"true\",\n" +
            "\"light_schema_change\" = \"true\"\n" +
            ")";

    /**
     * 增加倒排索引
     */
    protected static final String ADD_INVERTED_INDEX = "ALTER TABLE `%s` ADD INDEX idx_%s(`%s`) USING INVERTED";

    /**
     * 删除索引
     */
    protected static final String DROP_INDEX = "ALTER TABLE `%s` DROP INDEX idx_%s";

    /**
     * 修改表名
     */
    protected static final String RENAME_TABLE = "ALTER TABLE %s RENAME %s";

    /**
     * 获取指定表的所有列
     */
    protected static final String TABLE_COLUMN = "SHOW FULL COLUMNS FROM %s";

    /**
     * 增加列
     */
    protected static final String ADD_COLUMN = "ALTER TABLE %s ADD COLUMN %s %s";

    /**
     * 统计表数据占用空间
     */
    protected static final String TABLE_DATA_SIZE = "SHOW DATA FROM %s";
}
