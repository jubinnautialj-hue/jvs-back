package cn.bctools.data.factory.source.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author admin
 */

@Getter
@AllArgsConstructor
public enum DataSourceTypeEnum {
    mysqlDataSource("mysqlDataSource", "mysql数据库", Boolean.TRUE,Boolean.TRUE),
    mongoDataSource("mongoDataSource", "mongodb数据库", Boolean.FALSE,Boolean.FALSE),
    mqttDataSource("mqttDataSource", "mqtt", Boolean.FALSE,Boolean.FALSE),
    dataFactoryDataSource("dataFactoryDataSource", "数据集", Boolean.FALSE,Boolean.FALSE),
    pgsqlDataSource("pgsqlDataSource", "pgsql数据源", Boolean.FALSE,Boolean.FALSE),
    dataModel("dataModel", "数据模型", Boolean.FALSE,Boolean.FALSE),
    mariadbDataSource("mariadbDataSource", "mariadb数据库", Boolean.FALSE,Boolean.FALSE),
    oracleDataSource("oracleDataSource", "oracle数据库", Boolean.TRUE,Boolean.FALSE),
    dMDataSource("dMDataSource", "达梦数据库", Boolean.FALSE,Boolean.FALSE),
    db2DataSource("db2DataSource", "db2数据库", Boolean.FALSE,Boolean.FALSE),
    excelDataSource("excelDataSource", "excel数据源", Boolean.FALSE,Boolean.FALSE),
    kingbaseDataSource("kingbaseDataSource", "人大金仓", Boolean.FALSE,Boolean.FALSE),
    sqlServerDataSource("sqlServerDataSource", "sqlServer数据库", Boolean.FALSE,Boolean.FALSE),
    tidbDataSource("tidbDataSource", "TiDB数据库", Boolean.FALSE,Boolean.FALSE),
    starRocksDataSource("starRocksDataSource", "starRocks数据库", Boolean.FALSE,Boolean.FALSE),
    dorisDataSource("dorisDataSource", "doris数据库", Boolean.FALSE,Boolean.FALSE),
    hiveDateSource("hiveDateSource", "Apache Hive", Boolean.FALSE,Boolean.FALSE),
    clickHouseDataSource("clickHouseDataSource", "Click House", Boolean.FALSE,Boolean.FALSE),
    prestoDataBase("prestoDataBase", "prestoDB", Boolean.FALSE,Boolean.FALSE),
    apiDataSource("apiDataSource", "api数据源", Boolean.FALSE,Boolean.FALSE);

    @EnumValue
    public final String value;
    public final String desc;
    /**
     * sql条件
     */
    public final Boolean execSql;
    /**
     * 实时抽取是否支持
     */
    public final Boolean realTime;
}
