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
        mysql.put("type", "source");
        mysql.put("icon", "🐬");
        mysql.put("description", "关系型数据库，支持 CDC 实时同步");
        connectors.add(mysql);

        Map<String, Object> postgresql = new HashMap<>();
        postgresql.put("name", "PostgreSQL");
        postgresql.put("type", "source");
        postgresql.put("icon", "🐘");
        postgresql.put("description", "开源对象关系型数据库");
        connectors.add(postgresql);

        Map<String, Object> oracle = new HashMap<>();
        oracle.put("name", "Oracle");
        oracle.put("type", "source");
        oracle.put("icon", "🔶");
        oracle.put("description", "企业级关系型数据库");
        connectors.add(oracle);

        Map<String, Object> doris = new HashMap<>();
        doris.put("name", "Doris");
        doris.put("type", "sink");
        doris.put("icon", "🐚");
        doris.put("description", "实时分析型数据库");
        connectors.add(doris);

        Map<String, Object> clickhouse = new HashMap<>();
        clickhouse.put("name", "ClickHouse");
        clickhouse.put("type", "sink");
        clickhouse.put("icon", "🏠");
        clickhouse.put("description", "列式存储分析数据库");
        connectors.add(clickhouse);

        Map<String, Object> hive = new HashMap<>();
        hive.put("name", "Hive");
        hive.put("type", "sink");
        hive.put("icon", "🐝");
        hive.put("description", "数据仓库基础设施");
        connectors.add(hive);

        Map<String, Object> elasticsearch = new HashMap<>();
        elasticsearch.put("name", "Elasticsearch");
        elasticsearch.put("type", "sink");
        elasticsearch.put("icon", "🔍");
        elasticsearch.put("description", "分布式搜索和分析引擎");
        connectors.add(elasticsearch);

        return Result.success(connectors);
    }

    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> getJobTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();

        Map<String, Object> mysqlToDoris = new HashMap<>();
        mysqlToDoris.put("name", "MySQL 到 Doris 批量同步");
        mysqlToDoris.put("source", "MySQL");
        mysqlToDoris.put("sink", "Doris");
        mysqlToDoris.put("mode", "batch");
        mysqlToDoris.put("description", "将 MySQL 数据批量同步到 Doris 数据仓库");
        templates.add(mysqlToDoris);

        Map<String, Object> mysqlToDorisCDC = new HashMap<>();
        mysqlToDorisCDC.put("name", "MySQL 到 Doris 实时同步 (CDC)");
        mysqlToDorisCDC.put("source", "MySQL");
        mysqlToDorisCDC.put("sink", "Doris");
        mysqlToDorisCDC.put("mode", "streaming");
        mysqlToDorisCDC.put("description", "通过 CDC 实时捕获 MySQL 变更同步到 Doris");
        templates.add(mysqlToDorisCDC);

        Map<String, Object> postgresToClickhouse = new HashMap<>();
        postgresToClickhouse.put("name", "PostgreSQL 到 ClickHouse");
        postgresToClickhouse.put("source", "PostgreSQL");
        postgresToClickhouse.put("sink", "ClickHouse");
        postgresToClickhouse.put("mode", "batch");
        postgresToClickhouse.put("description", "PostgreSQL 数据同步到 ClickHouse 分析引擎");
        templates.add(postgresToClickhouse);

        return Result.success(templates);
    }

    @PostMapping("/quick-start/{templateName}")
    public Result<String> quickStart(@PathVariable String templateName) {
        SeaTunnelSubmitJobDto jobDto = new SeaTunnelSubmitJobDto();

        JSONObject env = new JSONObject();
        env.put("job.mode", "batch");
        env.put("parallelism", 5);
        jobDto.setEnv(env);

        List<JSONObject> sources = new ArrayList<>();
        List<JSONObject> sinks = new ArrayList<>();

        switch (templateName.toLowerCase()) {
            case "mysql-to-doris":
                jobDto.setJobName("MySQL_To_Doris_Quick_Start");
                jobDto.setDescription("MySQL 到 Doris 快速启动任务");

                JSONObject mysqlSource = new JSONObject();
                mysqlSource.put("plugin_name", "MySQL");
                mysqlSource.put("url", "jdbc:mysql://localhost:3306/demo");
                mysqlSource.put("user", "root");
                mysqlSource.put("table", "users");
                sources.add(mysqlSource);

                JSONObject dorisSink = new JSONObject();
                dorisSink.put("plugin_name", "Doris");
                dorisSink.put("url", "jdbc:mysql://localhost:9030/demo");
                dorisSink.put("table", "users_ods");
                sinks.add(dorisSink);
                break;

            case "postgres-to-clickhouse":
                jobDto.setJobName("PostgreSQL_To_ClickHouse_Quick_Start");
                jobDto.setDescription("PostgreSQL 到 ClickHouse 快速启动任务");

                JSONObject pgSource = new JSONObject();
                pgSource.put("plugin_name", "PostgreSQL");
                pgSource.put("url", "jdbc:postgresql://localhost:5432/demo");
                pgSource.put("user", "postgres");
                pgSource.put("table", "orders");
                sources.add(pgSource);

                JSONObject chSink = new JSONObject();
                chSink.put("plugin_name", "ClickHouse");
                chSink.put("url", "jdbc:clickhouse://localhost:8123/default");
                chSink.put("table", "orders_ods");
                sinks.add(chSink);
                break;

            default:
                return Result.error("Unknown template: " + templateName);
        }

        jobDto.setSource(sources);
        jobDto.setSink(sinks);

        String jobId = seaTunnelApiService.submitJob(jobDto);
        return Result.success(jobId);
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
