package cn.bctools.data.factory.config;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.constant.RedisKey;
import cn.bctools.data.factory.dto.DataFactoryMqttViewDto;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.SysFunction;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.enums.DataSyncPluginEnums;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.service.SysFunctionService;
import cn.bctools.data.factory.source.data.mqtt.MqttClientFactory;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.util.DorisJarUtil;
import cn.bctools.data.factory.util.DorisUtil;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 系统启动时执行的类 公共执行类
 *
 * @author xiaohui
 */
@Component
@Slf4j
public class CommonApplicationRunner implements ApplicationRunner {
    private final static String CREATE_DATABASE_SQL = "CREATE DATABASE IF NOT EXISTS {};";


    /**
     * doris ods数据库创建语句
     */
    private final static String URL = "jdbc:mysql://{}:{}/{}";
    /**
     * datax json的基本路径
     */
    private final static String DATAX_JSON = "{\"job\":{\"content\":[{\"reader\":{readerValue},\"writer\":{\"name\":\"doriswriter\",\"parameter\":{\"loadUrl\":[\"{dorisHttpPort}\"],\"loadProps\":{},\"column\":{dorisColumn},\"username\":\"{dorisUserName}\",\"password\":\"{dorisPassword}\",\"flushInterval\":30000,\"connection\":[{\"jdbcUrl\":\"{dorisUrl}\",\"selectedDatabase\":\"{libraryName}\",\"table\":[\"{tableName}\"]}],\"loadProps\":{\"format\":\"json\",\"strip_outer_array\":true}}}}],\"setting\":{\"speed\":{\"channel\":\"{channel}\"}}}}";
    /**
     * 获取自定义函数
     */
    private final static String GET_DORIS_FUNCTION_SQL = "show  full  functions IN {}";
    /**
     * 删除自定义函数
     */
    private final static String DORIS_DROP_FUNCTION_SQL = "DROP FUNCTION {}";
    /**
     * 创建自定义函数
     */
    private final static String DORIS_CREATE_FUNCTION_SQL = "CREATE FUNCTION {}({}) RETURNS {} PROPERTIES (\n" + "    \"file\"=\"{}\",\n" + "    \"symbol\"=\"{}\",\n" + "    \"always_nullable\"=\"true\",\n" + "    \"type\"=\"JAVA_UDF\"\n" + ");";
    @Autowired
    DorisConfig dorisConfig;
    @Autowired
    DorisJdbcTemplate dorisJdbcTemplate;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    SysFunctionService sysFunctionService;
    @Autowired
    CommonConfig commonConfig;
    @Autowired
    JvsDataFactoryService jvsDataFactoryService;
    @Autowired
    MqttClientFactory mqttClientFactory;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    DataSourceStructureService dataSourceStructureService;

    @Override
    public void run(ApplicationArguments args) {
        initDataSync();
        initDataxSetting();
        initConsanguinityViewTable();
        initSeaTunnelJson();
        startRealTimeTask();
        //支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        //启动
        CronUtil.start();
    }

    /**
     * 初始化seaTunnel基础同步json
     */
    private void initSeaTunnelJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("plugin_name", "Doris");
        jsonObject.put("fenodes", dorisConfig.getIp() + ":" + dorisConfig.getHttpPort());
        jsonObject.put("username", dorisConfig.getUserName());
        jsonObject.put("password", dorisConfig.getPassWord());
        jsonObject.put("database", dorisConfig.getLibraryName());
        jsonObject.put("sink.enable-2pc", true);
        jsonObject.put("sink.enable-delete", true);
        jsonObject.put("sink.label-prefix", "bi-json");
        JSONObject config = new JSONObject();
        config.put("format", "json");
        config.put("read_json_by_line", true);
        jsonObject.put("doris.config", config);
        Boolean isOk = redisUtils.set(Constant.SEA_TUNNEL_JSON_KEY, jsonObject.toString());
        if (!isOk) {
            log.info("---------初始化datax同步json错误,系统停止运行！！！！-------------");
            System.exit(1);
        }
    }

    /**
     * 服务重启后需要重启实时计算逻辑
     */
    private void startRealTimeTask() {
        TenantContextHolder.clear();
        List<JvsDataFactory> dataFactoryList = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>()
                .in(JvsDataFactory::getTaskType, Arrays.asList(TaskTypeEnum.mqtt, TaskTypeEnum.api))
                .eq(JvsDataFactory::getEnable, Boolean.TRUE));
        dataFactoryList.forEach(e -> {
            if (e.getTaskType().equals(TaskTypeEnum.mqtt)) {
                DataFactoryMqttViewDto mqttViewDto = com.alibaba.fastjson.JSONObject.parseObject(e.getViewJson(), DataFactoryMqttViewDto.class);
                //开启监听
                DataSource dataSource = dataSourceService.getById(mqttViewDto.getInputDataSource().getDataSourceId());
                DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(mqttViewDto.getInputDataSource().getDataSourceStructureId());
                MqttProperties mqttProperties = dataSource.getCustomJson().toJavaObject(MqttProperties.class);
                mqttProperties.setSubTopic(dataSourceStructure.getExecuteName());
                mqttClientFactory.openMonitor(mqttProperties, e.getId(), mqttViewDto.getMaxNumber());
                CronUtil.schedule(e.getId(), e.getTask().getCron(), () -> jvsDataFactoryService.execMqttTimedTask(e.getId()));
            }else {
                CronUtil.schedule(e.getId(), e.getTask().getCron(), () -> jvsDataFactoryService.execApiTimedTask(e.getId()));
            }
        });
    }

    /**
     * 数据同步 插件初始化
     */
    private void initDataSync() {
        List<DataSyncPluginEnums> plugin = new ArrayList<>();
        //datx 是否配置
        boolean dataxPlugin = StrUtil.isNotBlank(commonConfig.getDataxPath());
        //SeaTunnel 是否配置
        boolean seaTunnelPlugin = StrUtil.isNotBlank(commonConfig.getSeaTunnelIpPort());

        if (!(dataxPlugin || seaTunnelPlugin)) {
            log.warn("================数据同步插件未初始化,系统将停止运行=====================");
            System.exit(1);
        }
        if (dataxPlugin) {
            plugin.add(DataSyncPluginEnums.DATAX_PLUGIN);
        }
        if (seaTunnelPlugin) {
            //判断接口是否可以请求通
            plugin.add(DataSyncPluginEnums.SEA_TUNNEL_PLUGIN);
        }
        redisUtils.set(RedisKey.getDataFactoryDataSyncPlugin(), JSONObject.toJSONString(plugin));
    }

    /**
     * 初始化函数
     */
    private void initDorisFunction() {
        //查询doris 已经存在的函数
        String sql = StrUtil.format(GET_DORIS_FUNCTION_SQL, dorisConfig.getLibraryName());
        List<Map<String, Object>> maps = dorisJdbcTemplate.execSql(sql);
        Map<String, Map<String, Object>> dorisFunctionMap = new HashMap<>();
        //获取函数名称并转为小写 函数对比统一使用小写 因为doris 函数是不区分大小写的
        if (!maps.isEmpty()) {
            dorisFunctionMap = maps.stream().collect(Collectors.toMap(e -> {
                String functionName = e.get("Signature").toString();
                functionName = functionName.substring(0, functionName.indexOf("("));
                return functionName.toUpperCase();
            }, Function.identity()));
        }
        Boolean createMd5 = Boolean.FALSE;
        List<SysFunction> createFunction = new ArrayList<>();
        //初始化md5防止没有值
        try {
            createFunction = sysFunctionService.createMd5();
            createMd5 = Boolean.TRUE;
        } catch (BusinessException businessException) {
            log.info(businessException.getMessage());
        } catch (Exception exception) {
            log.info("生成md5错误", exception);
        }
        if (!createMd5) {
            log.info("初始化md5值错误,系统终止运行");
            System.exit(1);
        }
        List<SysFunction> list = sysFunctionService.list(new LambdaQueryWrapper<SysFunction>().eq(SysFunction::getIsDorisFunction, Boolean.FALSE));
        //判断md5值是否变更 变更后需要重新添加
        //todo 这里有个问题就是重新打包 什么都没有修改  md5也会不同
        List<SysFunction> functions = list.stream().filter(e -> !Objects.equals(DorisJarUtil.jarUrlToMd5(e.getJarUrl()), e.getJarMd5())).map(e -> e.setJarMd5(DorisJarUtil.jarUrlToMd5(e.getJarUrl()))).collect(Collectors.toList());
        if (!functions.isEmpty()) {
            createFunction.addAll(functions);
            sysFunctionService.updateBatchById(functions);
        }
        try {
            if (!createFunction.isEmpty()) {
                createDorisFunction(createFunction, dorisFunctionMap);
            }
        } catch (Exception exception) {
            log.info("添加函数失败，系统终止运行", exception);
            System.exit(1);
        }
    }


    /**
     * 创建doris 自定义函数
     *
     * @param dorisFunctionMap 原有的函数列表
     * @param list             函数列表
     */
    private void createDorisFunction(List<SysFunction> list, Map<String, Map<String, Object>> dorisFunctionMap) {
        //获取已经存在的函数  需要先删除
        dorisFunctionMap.forEach((key, value) -> {
            long count = list.stream().filter(v -> v.getName().toUpperCase().contains(key)).count();
            if (count > 0) {
                String sql = StrUtil.format(DORIS_DROP_FUNCTION_SQL, value.get("Signature"));
                dorisJdbcTemplate.execute(sql);
            }
        });
//        //新增函数
//        list.forEach(e -> {
//            String functionName = e.getName().toUpperCase();
//            String inParameter = String.join(",", e.getDorisInParameter());
//            String sql = StrUtil.format(DORIS_CREATE_FUNCTION_SQL, functionName, inParameter, e.getDorisReturnType(), e.getJarUrl(), e.getClassPath());
//            dorisJdbcTemplate.execute(sql);
//        });
    }


    /**
     * 初始化datax基础同步json
     */
    private void initDataxSetting() {
        //初始化数据库
        JdbcTemplate jdbc = DorisUtil.getJdbc(Boolean.TRUE);
        String execSql = StrUtil.format(CREATE_DATABASE_SQL, dorisConfig.getLibraryName());
        jdbc.execute(execSql);
        //初始化datax 同步json文件
        //替换https端口
        String json = StrUtil.replace(DATAX_JSON, "{dorisHttpPort}", dorisConfig.getIp() + ":" + dorisConfig.getHttpPort());
        //替換{dorisUserName}
        json = StrUtil.replace(json, "{dorisUserName}", dorisConfig.getUserName());
        json = StrUtil.replace(json, "{dorisPassword}", dorisConfig.getPassWord());
        json = StrUtil.replace(json, "{dorisUrl}", StrUtil.format(URL, dorisConfig.getIp(), dorisConfig.getQueryPort(), dorisConfig.getLibraryName()));
        json = StrUtil.replace(json, "{libraryName}", dorisConfig.getLibraryName());
        Boolean isOk = redisUtils.set(Constant.DATAX_JSON_KEY, json);
        if (!isOk) {
            log.info("---------初始化datax同步json错误,系统停止运行！！！！-------------");
            System.exit(1);
        }
    }

    /**
     * 创建血缘视图表
     */
    private void initConsanguinityViewTable() {
        Integer replicationNum = SpringContextUtil.getBean(CommonConfig.class).getReplicationNum();
        //血缘视图sql
        String createTableSql = "CREATE TABLE IF NOT EXISTS consanguinity_view\n" +
                "(\n" + "    `id` VARCHAR(200) NOT NULL COMMENT \"唯一id\",\n" +
                "    `title` VARCHAR(200) COMMENT \"名称 设计名称\",\n" +
                "    `designId` VARCHAR(200) COMMENT \"设计id\",\n" +
                "    `subordinateTile`  VARCHAR(200) COMMENT \"下级名称\",\n" +
                "    `subordinateId`  VARCHAR(200) COMMENT \"下级id\",\n" +
                "    `tenantId` VARCHAR(50) COMMENT \"租户id\",\n" +
                "    `groupId` VARCHAR(200) COMMENT \"组id-用于确认是否为同一个视图(其实就是本身id)\",\n" +
                "    `viewType` VARCHAR(100) COMMENT \"类型-大屏报表等等\",\n" +
                "\t\t`type` VARCHAR(100) COMMENT \"类型-大屏报表等等\",\n" +
                "\t\t`createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \"创建时间\"\n" + ")\n" +
                "UNIQUE KEY(`id`)\n" + "DISTRIBUTED BY HASH(`id`) BUCKETS 1\n" +
                "PROPERTIES (\n" + "\"replication_allocation\" = \"tag.location.default: " + replicationNum + "\",\n" +
                "\"enable_unique_key_merge_on_write\" = \"true\"\n" + ");";
        dorisJdbcTemplate.execute(createTableSql);
    }
}
