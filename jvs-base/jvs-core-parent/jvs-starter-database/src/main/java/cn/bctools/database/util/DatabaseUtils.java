package cn.bctools.database.util;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.database.entity.DatabaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: GuoZi
 */
@Slf4j
public class DatabaseUtils {

    private DatabaseUtils() {
    }

    public static DatabaseInfo parseUrl(String url) {
        if (url.startsWith("jdbc:dm://")) {
            return dmParseUrl(url);
        }
        if (url.startsWith("jdbc:postgresql://")) {
            return pgParseUrl(url);
        }
        return defaultParseUrl(url);
    }

    private static DatabaseInfo defaultParseUrl(String url) {
        DatabaseInfo info = new DatabaseInfo();
        int limit = 4;
        String[] subUrl = url.split("/", limit);
        if (subUrl.length != limit) {
            log.error("数据源连接url格式异常: {}", url);
            throw new BusinessException("数据源连接url格式异常");
        }
        int i = 2;
        String[] split = subUrl[i].split(":");
        if (split.length == 1) {
            // 域名
            info.setIp(split[0]);
        } else if (split.length == i) {
            // ip:port
            info.setIp(split[0]);
            info.setPort(split[1]);
        } else {
            log.error("数据源连接url格式异常: {}", url);
            throw new BusinessException("数据源连接url格式异常");
        }
        // 数据库名称
        String databaseName = subUrl[3].split("\\?")[0];
        if (StringUtils.isBlank(databaseName)) {
            log.error("数据源连接url格式异常: {}", url);
            throw new BusinessException("数据源连接url格式异常");
        }
        info.setDatabaseName(databaseName);
        return info;
    }

    private static DatabaseInfo dmParseUrl(String url) {
        DatabaseInfo info = new DatabaseInfo();
        url = url.substring(10);
        String[] subUrl = url.split("\\?");

        String[] split = subUrl[0].split(":");
        if (split.length == 1) {
            // 域名
            info.setIp(split[0]);
        } else if (split.length == 2) {
            // ip:port
            info.setIp(split[0]);
            info.setPort(split[1]);
        } else {
            log.error("数据源连接url格式异常: {}", url);
            throw new BusinessException("数据源连接url格式异常");
        }

        // 数据库名称
        String schema = "schema=";
        String databaseName = subUrl[1].substring(subUrl[1].indexOf(schema) + schema.length()).split("&")[0];
        if (StringUtils.isBlank(databaseName)) {
            log.error("数据源连接url格式异常: {}", url);
            throw new BusinessException("数据源连接url格式异常");
        }
        info.setDatabaseName(databaseName);
        return info;
    }

    private static DatabaseInfo pgParseUrl(String url) {
        DatabaseInfo info = new DatabaseInfo();
        url = url.substring(18);

        String[] subUrl = url.split("/");
        String[] split = subUrl[0].split(":");
        if (split.length == 1) {
            // 域名
            info.setIp(split[0]);
        } else if (split.length == 2) {
            // ip:port
            info.setIp(split[0]);
            info.setPort(split[1]);
        } else {
            log.error("数据源连接url格式异常: {}", url);
            throw new BusinessException("数据源连接url格式异常");
        }

        // 数据库名称
        String schema = "currentSchema=";
        String databaseName = subUrl[1].split(schema)[1].split("&")[0];
        if (StringUtils.isBlank(databaseName)) {
            log.error("数据源连接url格式异常: {}", url);
            throw new BusinessException("数据源连接url格式异常");
        }
        info.setDatabaseName(databaseName);
        return info;
    }
}
