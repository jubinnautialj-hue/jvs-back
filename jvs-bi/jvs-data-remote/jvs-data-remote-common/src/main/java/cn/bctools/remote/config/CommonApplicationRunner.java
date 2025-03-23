package cn.bctools.remote.config;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


/**
 * 系统启动时执行的类 公共执行类
 *
 * @author xiaohui
 */
@Component
@Slf4j
public class CommonApplicationRunner implements ApplicationRunner {
    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;


    @Override
    public void run(ApplicationArguments args) {
        initConsanguinityViewTable();

    }


    /**
     * 创建血缘视图表
     */
    private void initConsanguinityViewTable() {
        //血缘视图sql
        String createTableSql = "CREATE TABLE IF NOT EXISTS data_remote_log\n" +
                "(\n" + "    `id` VARCHAR(200) NOT NULL COMMENT \"唯一id\",\n" +
                "    `serverId` VARCHAR(200) COMMENT \"服务id\",\n" +
                "    `serverName` VARCHAR(200) COMMENT \"服务名称\",\n" +
                "    `secret`  VARCHAR(200) COMMENT \"调用时使用的凭证\",\n" +
                "    `secretRemark`  VARCHAR(200) COMMENT \"凭证说明\",\n" +
                "    `serverAttr` VARCHAR(50) COMMENT \"服务详情\",\n" +
                "    `invoker` VARCHAR(200) COMMENT \"调用人\",\n" +
                "    `callStatus` Boolean COMMENT \"调用状态\",\n" +
                "    `dataGetStatus` Boolean COMMENT \"数据获取状态\",\n" +
                "\t\t`ip` VARCHAR(100) COMMENT \"调用ip\",\n" +
                "\t\t`callDate` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \"创建时间\"\n" + ")\n" +
                "UNIQUE KEY(`id`)\n" + "DISTRIBUTED BY HASH(`id`) BUCKETS 1\n" +
                "PROPERTIES (\n" + "\"replication_allocation\" = \"tag.location.default: 3\",\n" +
                "\"enable_unique_key_merge_on_write\" = \"true\"\n" + ");";
        dorisJdbcTemplate.execute(createTableSql);
    }
}
