package cn.bctools.data.factory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaohui
 */
@Getter
@AllArgsConstructor
public enum RequestTypeEnum {

    /**
     * 来源或者去向的对象
     */
    chart("图表"),
    report("报表"),
    screen("大屏"),
    risk("规则"),
    dataFactory("数据集"),
    mysql("mysql数据库"),
    mongodb("mongodb数据库"),
    pgsql("pgsql数据源"),
    dataModel("数据模型"),
    mariadb("mariadb数据库"),
    oracle("oracle数据库"),
    api("api数据源"),
    remote("数据服务");

    private final String desc;
}
