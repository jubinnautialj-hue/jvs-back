package cn.bctools.data.factory.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author xiaohui
 */
@Getter
@AllArgsConstructor
public enum ConsanguinityViewTypeEnum {

    /**
     * 来源或者去向的对象
     */
    chart("图表"),
    report("报表"),
    screen("大屏"),
    risk("规则"),
    mysqlDataSource("mysql数据库"),
    mongoDataSource("mongodb数据库"),
    dataFactoryDataSource("数据集"),
    /**
     * 此类型目前没有入数据库 主要用于
     * {@link cn.bctools.data.factory.controller.DataSourceFieldController#getSelectTable}
     * */
    dataFactorySqlDataSource("sql数据集"),
    dataFactoryMqttDataSource("mqtt数据集"),
    pgsqlDataSource("pgsql数据源"),
    dataModel("数据模型"),
    mariadbDataSource("mariadb数据库"),
    oracleDataSource("oracle数据库"),
    dMDataSource("达梦数据库"),
    mqttDataSource("mqtt数据源"),
    db2DataSource("db2数据库"),
    excelDataSource("excel数据源"),
    kingbaseDataSource("人大金仓"),
    sqlServerDataSource("sqlServer数据库"),
    tidbDataSource("TiDB数据库"),
    starRocksDataSource("starRocks数据库"),
    dorisDataSource("doris数据库"),
    hiveDateSource("Apache Hive"),
    clickHouseDataSource("Click House"),
    prestoDataBase("prestoDB"),
    apiDataSource("api数据源"),
    remote("数据服务");

    private final String desc;
}
