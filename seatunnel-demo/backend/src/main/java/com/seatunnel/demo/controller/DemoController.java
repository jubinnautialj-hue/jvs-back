package com.seatunnel.demo.controller;

import com.alibaba.fastjson2.JSONObject;
import com.seatunnel.demo.dto.Result;
import com.seatunnel.demo.dto.SeaTunnelSubmitJobDto;
import com.seatunnel.demo.service.SeaTunnelApiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/demo")
@AllArgsConstructor
public class DemoController {

    private final SeaTunnelApiService seaTunnelApiService;

    @GetMapping("/connectors")
    public Result<List<Map<String, Object>>> getConnectors() {
        List<Map<String, Object>> connectors = new ArrayList<>();

        Map<String, Object> mysql = new HashMap<>();
        mysql.put("name", "MySQL");
        mysql.put("type", "both");
        mysql.put("icon", "🐬");
        mysql.put("description", "关系型数据库，支持 CDC 实时同步，既可作为数据源也可作为数据目标");
        List<String> mysqlFeatures = new ArrayList<>();
        mysqlFeatures.add("全量读取/写入");
        mysqlFeatures.add("CDC 实时同步");
        mysqlFeatures.add("Binlog 解析");
        mysqlFeatures.add("增量同步");
        mysql.put("features", mysqlFeatures);
        connectors.add(mysql);

        Map<String, Object> postgresql = new HashMap<>();
        postgresql.put("name", "PostgreSQL");
        postgresql.put("type", "both");
        postgresql.put("icon", "🐘");
        postgresql.put("description", "开源对象关系型数据库，支持 WAL 日志 CDC");
        List<String> pgFeatures = new ArrayList<>();
        pgFeatures.add("全量读取/写入");
        pgFeatures.add("WAL CDC");
        pgFeatures.add("逻辑复制");
        pgFeatures.add("JSON 支持");
        postgresql.put("features", pgFeatures);
        connectors.add(postgresql);

        Map<String, Object> oracle = new HashMap<>();
        oracle.put("name", "Oracle");
        oracle.put("type", "both");
        oracle.put("icon", "🔶");
        oracle.put("description", "企业级关系型数据库，支持 LogMiner 日志解析");
        List<String> oracleFeatures = new ArrayList<>();
        oracleFeatures.add("全量读取/写入");
        oracleFeatures.add("LogMiner");
        oracleFeatures.add("RAC 支持");
        oracleFeatures.add("事务支持");
        oracle.put("features", oracleFeatures);
        connectors.add(oracle);

        Map<String, Object> doris = new HashMap<>();
        doris.put("name", "Doris");
        doris.put("type", "both");
        doris.put("icon", "🐚");
        doris.put("description", "实时分析型数据库，MPP 架构，高性能 OLAP");
        List<String> dorisFeatures = new ArrayList<>();
        dorisFeatures.add("批量读取/写入");
        dorisFeatures.add("实时写入");
        dorisFeatures.add("主键模型");
        dorisFeatures.add("聚合模型");
        doris.put("features", dorisFeatures);
        connectors.add(doris);

        Map<String, Object> clickhouse = new HashMap<>();
        clickhouse.put("name", "ClickHouse");
        clickhouse.put("type", "both");
        clickhouse.put("icon", "🏠");
        clickhouse.put("description", "列式存储分析数据库，极高的查询性能");
        List<String> chFeatures = new ArrayList<>();
        chFeatures.add("批量读取/写入");
        chFeatures.add("分区支持");
        chFeatures.add("物化视图");
        chFeatures.add("低延迟");
        clickhouse.put("features", chFeatures);
        connectors.add(clickhouse);

        Map<String, Object> hive = new HashMap<>();
        hive.put("name", "Hive");
        hive.put("type", "both");
        hive.put("icon", "🐝");
        hive.put("description", "基于 Hadoop 的数据仓库基础设施");
        List<String> hiveFeatures = new ArrayList<>();
        hiveFeatures.add("批量读取/写入");
        hiveFeatures.add("分区表");
        hiveFeatures.add("ORC/Parquet");
        hiveFeatures.add("ACID 支持");
        hive.put("features", hiveFeatures);
        connectors.add(hive);

        Map<String, Object> elasticsearch = new HashMap<>();
        elasticsearch.put("name", "Elasticsearch");
        elasticsearch.put("type", "both");
        elasticsearch.put("icon", "🔍");
        elasticsearch.put("description", "分布式搜索和分析引擎");
        List<String> esFeatures = new ArrayList<>();
        esFeatures.add("批量读取/写入");
        esFeatures.add("实时索引");
        esFeatures.add("文档更新");
        esFeatures.add("聚合查询");
        elasticsearch.put("features", esFeatures);
        connectors.add(elasticsearch);

        Map<String, Object> kafka = new HashMap<>();
        kafka.put("name", "Kafka");
        kafka.put("type", "both");
        kafka.put("icon", "📨");
        kafka.put("description", "分布式消息队列，流数据处理的核心");
        List<String> kafkaFeatures = new ArrayList<>();
        kafkaFeatures.add("消息消费/生产");
        kafkaFeatures.add("Exactly-Once");
        kafkaFeatures.add("分区消费");
        kafkaFeatures.add("流处理");
        kafka.put("features", kafkaFeatures);
        connectors.add(kafka);

        Map<String, Object> redis = new HashMap<>();
        redis.put("name", "Redis");
        redis.put("type", "both");
        redis.put("icon", "🗄️");
        redis.put("description", "高性能内存数据库，支持多种数据结构");
        List<String> redisFeatures = new ArrayList<>();
        redisFeatures.add("批量读取/写入");
        redisFeatures.add("Key/Value");
        redisFeatures.add("Hash/Set/ZSet");
        redisFeatures.add("Pipeline 支持");
        redis.put("features", redisFeatures);
        connectors.add(redis);

        return Result.success(connectors);
    }

    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> getJobTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();

        Map<String, Object> mysqlToDoris = new HashMap<>();
        mysqlToDoris.put("name", "MySQL → Doris 批量同步");
        mysqlToDoris.put("source", "MySQL");
        mysqlToDoris.put("sink", "Doris");
        mysqlToDoris.put("mode", "batch");
        mysqlToDoris.put("description", "将 MySQL 业务数据批量同步到 Doris 数据仓库进行 OLAP 分析");
        mysqlToDoris.put("category", "业务库→数仓");
        templates.add(mysqlToDoris);

        Map<String, Object> mysqlToDorisCDC = new HashMap<>();
        mysqlToDorisCDC.put("name", "MySQL → Doris 实时同步 (CDC)");
        mysqlToDorisCDC.put("source", "MySQL");
        mysqlToDorisCDC.put("sink", "Doris");
        mysqlToDorisCDC.put("mode", "streaming");
        mysqlToDorisCDC.put("description", "通过 Binlog CDC 实时捕获 MySQL 数据变更，毫秒级同步到 Doris");
        mysqlToDorisCDC.put("category", "业务库→数仓");
        templates.add(mysqlToDorisCDC);

        Map<String, Object> mysqlToClickhouse = new HashMap<>();
        mysqlToClickhouse.put("name", "MySQL → ClickHouse 批量同步");
        mysqlToClickhouse.put("source", "MySQL");
        mysqlToClickhouse.put("sink", "ClickHouse");
        mysqlToClickhouse.put("mode", "batch");
        mysqlToClickhouse.put("description", "将 MySQL 数据批量同步到 ClickHouse 列式分析数据库");
        mysqlToClickhouse.put("category", "业务库→数仓");
        templates.add(mysqlToClickhouse);

        Map<String, Object> postgresToClickhouse = new HashMap<>();
        postgresToClickhouse.put("name", "PostgreSQL → ClickHouse");
        postgresToClickhouse.put("source", "PostgreSQL");
        postgresToClickhouse.put("sink", "ClickHouse");
        postgresToClickhouse.put("mode", "batch");
        postgresToClickhouse.put("description", "PostgreSQL 业务数据同步到 ClickHouse 分析引擎");
        postgresToClickhouse.put("category", "业务库→数仓");
        templates.add(postgresToClickhouse);

        Map<String, Object> oracleToHive = new HashMap<>();
        oracleToHive.put("name", "Oracle → Hive 批量同步");
        oracleToHive.put("source", "Oracle");
        oracleToHive.put("sink", "Hive");
        oracleToHive.put("mode", "batch");
        oracleToHive.put("description", "将 Oracle 企业级数据库数据同步到 Hive 数据仓库");
        oracleToHive.put("category", "业务库→数仓");
        templates.add(oracleToHive);

        Map<String, Object> dorisToMysql = new HashMap<>();
        dorisToMysql.put("name", "Doris → MySQL 反向同步");
        dorisToMysql.put("source", "Doris");
        dorisToMysql.put("sink", "MySQL");
        dorisToMysql.put("mode", "batch");
        dorisToMysql.put("description", "将 Doris 分析结果反向同步回 MySQL 业务库，用于报表展示");
        dorisToMysql.put("category", "数仓→业务库");
        templates.add(dorisToMysql);

        Map<String, Object> clickhouseToMysql = new HashMap<>();
        clickhouseToMysql.put("name", "ClickHouse → MySQL 反向同步");
        clickhouseToMysql.put("source", "ClickHouse");
        clickhouseToMysql.put("sink", "MySQL");
        clickhouseToMysql.put("mode", "batch");
        clickhouseToMysql.put("description", "将 ClickHouse 聚合分析结果同步回 MySQL 业务系统");
        clickhouseToMysql.put("category", "数仓→业务库");
        templates.add(clickhouseToMysql);

        Map<String, Object> hiveToDoris = new HashMap<>();
        hiveToDoris.put("name", "Hive → Doris 数据同步");
        hiveToDoris.put("source", "Hive");
        hiveToDoris.put("sink", "Doris");
        hiveToDoris.put("mode", "batch");
        hiveToDoris.put("description", "将 Hive 离线数仓数据同步到 Doris 进行实时分析");
        hiveToDoris.put("category", "数仓→数仓");
        templates.add(hiveToDoris);

        Map<String, Object> dorisToClickhouse = new HashMap<>();
        dorisToClickhouse.put("name", "Doris → ClickHouse 数据同步");
        dorisToClickhouse.put("source", "Doris");
        dorisToClickhouse.put("sink", "ClickHouse");
        dorisToClickhouse.put("mode", "batch");
        dorisToClickhouse.put("description", "Doris 和 ClickHouse 之间的数据迁移和同步");
        dorisToClickhouse.put("category", "数仓→数仓");
        templates.add(dorisToClickhouse);

        Map<String, Object> mysqlToEs = new HashMap<>();
        mysqlToEs.put("name", "MySQL → Elasticsearch 同步");
        mysqlToEs.put("source", "MySQL");
        mysqlToEs.put("sink", "Elasticsearch");
        mysqlToEs.put("mode", "batch");
        mysqlToEs.put("description", "将 MySQL 业务数据同步到 Elasticsearch 进行全文检索");
        mysqlToEs.put("category", "业务库→搜索引擎");
        templates.add(mysqlToEs);

        Map<String, Object> mysqlToRedis = new HashMap<>();
        mysqlToRedis.put("name", "MySQL → Redis 缓存同步");
        mysqlToRedis.put("source", "MySQL");
        mysqlToRedis.put("sink", "Redis");
        mysqlToRedis.put("mode", "batch");
        mysqlToRedis.put("description", "将 MySQL 热点数据同步到 Redis 缓存，加速查询");
        mysqlToRedis.put("category", "业务库→缓存");
        templates.add(mysqlToRedis);

        Map<String, Object> kafkaToDoris = new HashMap<>();
        kafkaToDoris.put("name", "Kafka → Doris 流处理");
        kafkaToDoris.put("source", "Kafka");
        kafkaToDoris.put("sink", "Doris");
        kafkaToDoris.put("mode", "streaming");
        kafkaToDoris.put("description", "消费 Kafka 消息队列数据，实时写入 Doris 进行分析");
        kafkaToDoris.put("category", "消息队列→数仓");
        templates.add(kafkaToDoris);

        return Result.success(templates);
    }

    private Map<String, TemplateConfig> getTemplateConfigMap() {
        Map<String, TemplateConfig> configMap = new HashMap<>();

        configMap.put("mysql-to-doris", new TemplateConfig(
                "MySQL_To_Doris_Batch",
                "MySQL 到 Doris 批量同步任务",
                "batch",
                createSourceConfig("MySQL", "jdbc:mysql://localhost:3306/demo", "users"),
                createSinkConfig("Doris", "jdbc:mysql://localhost:9030/demo", "users_ods")
        ));

        configMap.put("mysql-to-doris-cdc", new TemplateConfig(
                "MySQL_To_Doris_CDC",
                "MySQL 到 Doris 实时 CDC 同步任务",
                "streaming",
                createSourceConfig("MySQL", "jdbc:mysql://localhost:3306/demo", "users"),
                createSinkConfig("Doris", "jdbc:mysql://localhost:9030/demo", "users_ods")
        ));

        configMap.put("mysql-to-clickhouse", new TemplateConfig(
                "MySQL_To_ClickHouse",
                "MySQL 到 ClickHouse 批量同步任务",
                "batch",
                createSourceConfig("MySQL", "jdbc:mysql://localhost:3306/demo", "users"),
                createSinkConfig("ClickHouse", "jdbc:clickhouse://localhost:8123/default", "users_ods")
        ));

        configMap.put("postgres-to-clickhouse", new TemplateConfig(
                "PostgreSQL_To_ClickHouse",
                "PostgreSQL 到 ClickHouse 同步任务",
                "batch",
                createSourceConfig("PostgreSQL", "jdbc:postgresql://localhost:5432/demo", "orders"),
                createSinkConfig("ClickHouse", "jdbc:clickhouse://localhost:8123/default", "orders_ods")
        ));

        configMap.put("oracle-to-hive", new TemplateConfig(
                "Oracle_To_Hive",
                "Oracle 到 Hive 批量同步任务",
                "batch",
                createSourceConfig("Oracle", "jdbc:oracle:thin:@localhost:1521:ORCL", "employees"),
                createSinkConfig("Hive", "thrift://localhost:9083", "employees_ods")
        ));

        configMap.put("doris-to-mysql", new TemplateConfig(
                "Doris_To_MySQL",
                "Doris 到 MySQL 反向同步任务（分析结果回写业务库）",
                "batch",
                createSourceConfig("Doris", "jdbc:mysql://localhost:9030/demo", "user_analysis"),
                createSinkConfig("MySQL", "jdbc:mysql://localhost:3306/demo", "user_analysis_result")
        ));

        configMap.put("clickhouse-to-mysql", new TemplateConfig(
                "ClickHouse_To_MySQL",
                "ClickHouse 到 MySQL 反向同步任务",
                "batch",
                createSourceConfig("ClickHouse", "jdbc:clickhouse://localhost:8123/default", "order_analysis"),
                createSinkConfig("MySQL", "jdbc:mysql://localhost:3306/demo", "order_analysis_result")
        ));

        configMap.put("hive-to-doris", new TemplateConfig(
                "Hive_To_Doris",
                "Hive 到 Doris 数据同步任务（离线数仓到实时分析）",
                "batch",
                createSourceConfig("Hive", "thrift://localhost:9083", "user_profile"),
                createSinkConfig("Doris", "jdbc:mysql://localhost:9030/demo", "user_profile_dw")
        ));

        configMap.put("doris-to-clickhouse", new TemplateConfig(
                "Doris_To_ClickHouse",
                "Doris 到 ClickHouse 数据同步任务",
                "batch",
                createSourceConfig("Doris", "jdbc:mysql://localhost:9030/demo", "sales_report"),
                createSinkConfig("ClickHouse", "jdbc:clickhouse://localhost:8123/default", "sales_report")
        ));

        configMap.put("mysql-to-elasticsearch", new TemplateConfig(
                "MySQL_To_Elasticsearch",
                "MySQL 到 Elasticsearch 全文检索同步任务",
                "batch",
                createSourceConfig("MySQL", "jdbc:mysql://localhost:3306/demo", "products"),
                createSinkConfig("Elasticsearch", "http://localhost:9200", "products_index")
        ));

        configMap.put("mysql-to-redis", new TemplateConfig(
                "MySQL_To_Redis",
                "MySQL 到 Redis 缓存同步任务",
                "batch",
                createSourceConfig("MySQL", "jdbc:mysql://localhost:3306/demo", "hot_items"),
                createSinkConfig("Redis", "redis://localhost:6379", "hot_items_cache")
        ));

        configMap.put("kafka-to-doris", new TemplateConfig(
                "Kafka_To_Doris",
                "Kafka 到 Doris 流处理任务",
                "streaming",
                createSourceConfig("Kafka", "localhost:9092", "user_events"),
                createSinkConfig("Doris", "jdbc:mysql://localhost:9030/demo", "user_events_dwd")
        ));

        return configMap;
    }

    private JSONObject createSourceConfig(String pluginName, String url, String table) {
        JSONObject config = new JSONObject();
        config.put("plugin_name", pluginName);
        config.put("url", url);
        config.put("table", table);
        return config;
    }

    private JSONObject createSinkConfig(String pluginName, String url, String table) {
        JSONObject config = new JSONObject();
        config.put("plugin_name", pluginName);
        config.put("url", url);
        config.put("table", table);
        return config;
    }

    @PostMapping("/quick-start/{templateKey}")
    public Result<String> quickStart(@PathVariable String templateKey) {
        Map<String, TemplateConfig> configMap = getTemplateConfigMap();
        TemplateConfig config = configMap.get(templateKey.toLowerCase());

        if (config == null) {
            return Result.error("Unknown template: " + templateKey);
        }

        SeaTunnelSubmitJobDto jobDto = new SeaTunnelSubmitJobDto();

        JSONObject env = new JSONObject();
        env.put("job.mode", "streaming".equals(config.mode) ? "STREAMING" : "batch");
        env.put("parallelism", 5);
        jobDto.setEnv(env);

        jobDto.setJobName(config.jobName);
        jobDto.setDescription(config.description);

        List<JSONObject> sources = new ArrayList<>();
        sources.add(config.sourceConfig);
        jobDto.setSource(sources);

        List<JSONObject> sinks = new ArrayList<>();
        sinks.add(config.sinkConfig);
        jobDto.setSink(sinks);

        String jobId = seaTunnelApiService.submitJob(jobDto);
        return Result.success(jobId);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class TemplateConfig {
        private String jobName;
        private String description;
        private String mode;
        private JSONObject sourceConfig;
        private JSONObject sinkConfig;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        List<Map<String, Object>> stats = new ArrayList<>();

        Map<String, Object> totalJobs = new HashMap<>();
        totalJobs.put("label", "总任务数");
        totalJobs.put("value", seaTunnelApiService.listAllJobs().size());
        totalJobs.put("icon", "📊");
        stats.add(totalJobs);

        Map<String, Object> runningJobs = new HashMap<>();
        runningJobs.put("label", "运行中");
        runningJobs.put("value", seaTunnelApiService.listAllJobs().stream()
                .filter(j -> "RUNNING".equals(j.getJobStatus().getCode())).count());
        runningJobs.put("icon", "▶️");
        stats.add(runningJobs);

        Map<String, Object> finishedJobs = new HashMap<>();
        finishedJobs.put("label", "已完成");
        finishedJobs.put("value", seaTunnelApiService.listAllJobs().stream()
                .filter(j -> "FINISHED".equals(j.getJobStatus().getCode())).count());
        finishedJobs.put("icon", "✅");
        stats.add(finishedJobs);

        overview.put("stats", stats);

        Map<String, Object> features = new HashMap<>();
        features.put("title", "SeaTunnel 核心特性");
        List<Map<String, String>> featureList = new ArrayList<>();

        Map<String, String> f1 = new HashMap<>();
        f1.put("name", "统一配置");
        f1.put("desc", "一套配置适配多种数据源，降低学习成本");
        featureList.add(f1);

        Map<String, String> f2 = new HashMap<>();
        f2.put("name", "实时 CDC");
        f2.put("desc", "支持 MySQL Binlog、PostgreSQL WAL 等实时同步");
        featureList.add(f2);

        Map<String, String> f3 = new HashMap<>();
        f3.put("name", "分布式执行");
        f3.put("desc", "基于 Spark/Flink 引擎，支持大规模数据并行处理");
        featureList.add(f3);

        Map<String, String> f4 = new HashMap<>();
        f4.put("name", "可视化监控");
        f4.put("desc", "实时查看任务状态、数据读取/写入统计");
        featureList.add(f4);

        features.put("list", featureList);
        overview.put("features", features);

        return Result.success(overview);
    }
}
